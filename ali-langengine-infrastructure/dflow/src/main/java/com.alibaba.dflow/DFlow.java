package com.alibaba.dflow;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.reactivex.functions.Function;

import com.alibaba.dflow.InitEntry.Entry;
import com.alibaba.dflow.InitEntry.RequestResender;
import com.alibaba.dflow.config.GlobalStoreInterface;
import com.alibaba.dflow.config.RetryProtectedContentStoreInterface;
import com.alibaba.dflow.config.RetryProtectedGlobalStoreInterface;
import com.alibaba.dflow.config.StepHandler;
import com.alibaba.dflow.func.AbstractClosureEnabledFunction;
import com.alibaba.dflow.func.AbstractClosureEnabledFunction.InvalidClosureFunctionException;
import com.alibaba.dflow.func.ClosureEnabledFunction;
import com.alibaba.dflow.func.ValidClosure;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.ContextStack.ContextNode;
import com.alibaba.dflow.internal.DFlowCall;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.dflow.internal.DFlowDelay;
import com.alibaba.dflow.internal.DFlowDelay.DFlowDelayManager;
import com.alibaba.dflow.internal.DFlowFlatmap;
import com.alibaba.dflow.internal.DFlowJust;
import com.alibaba.dflow.internal.DFlowMap;
import com.alibaba.dflow.internal.DFlowMultiCall;
import com.alibaba.dflow.internal.DFlowMultiCall.InnerList;
import com.alibaba.dflow.internal.DFlowOr;
import com.alibaba.dflow.internal.DFlowOr.OrResult;
import com.alibaba.dflow.internal.DFlowZip;
import com.alibaba.dflow.internal.InternalHelper;
import com.alibaba.fastjson.JSON;

import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.SubscribableChannel;

import com.alibaba.dflow.config.ContextStoreInterface;
import com.alibaba.fastjson.TypeReference;

import static com.alibaba.dflow.internal.ContextStack.STATUS_BEGIN;

/**
 * 每个DFlow在init之后确定上下游节点，只处理自己的输入与输出到全局，并在结束后触发下一级
 * 每个FlatMap中的内嵌新流需要初始化，如初始化次数没有和父流一致，则不能走分布式过程。
 *
 * @param <T>
 */
public abstract class DFlow<T> implements ValidClosure {
    static Logger logger = LoggerFactory.getLogger(DFlow.class);

    private static List<Pattern> g_pattern = new ArrayList<>();
    protected static boolean g_strictMode = true;
    public static boolean g_testMode = false;

    public static boolean g_debugLog = false;

    /**
     * ===================== ==========APIs=========== =====================
     */
    public static void setStrictMode(boolean strictMode) {
        g_strictMode = strictMode;
    }

    public static List<String> addOmitTaskPattern(String regexPatten) {
        Pattern pattern = Pattern.compile(regexPatten);
        g_pattern.add(pattern);
        return g_pattern.stream().map(p -> p.pattern()).collect(Collectors.toList());
    }

    public static List<String> queryPattern() {
        return g_pattern.stream().map(p -> p.pattern()).collect(Collectors.toList());
    }

    public static void replayStep(String traceId, String stepName, boolean onlyThisStep) throws UserException {
        ContextStack c = getStorage().getContext(traceId);
        c.getStack();
        while (!c.getStack().pop().getName().equals(stepName)) {
            ;
        }
        DFlow thisNode = DFlow.STEPS.get(stepName);
        InternalHelper.rebuildNewStack(c, thisNode.getIDName(), thisNode.debugName);

        if (onlyThisStep) {
            c.setNoStorage();
        } else {
            //持久化
            getStorage().putContext(traceId, c);
        }
        thisNode.callAfterStackBuild(c);
    }

    public static void clearOmitPattern(String pattern) {
        g_pattern.removeIf(p -> p.pattern().equals(pattern));
    }

    public static ContextNode checkStatus(String traceId, String debugName) {
        ContextStack s = getStorage().getContext(traceId);
        if (s != null) {
            ContextNode r = s.findNodeByDebugName(debugName);
            if (r != null) {
                return r;
            }

            for (ContextNode n : s.getStack()) {
                if (n.getChildTask() != null) {
                    for (String ctraceId : n.getChildTask()) {
                        ContextNode cn = checkStatus(ctraceId, debugName);
                        if (cn != null) {
                            return cn;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static ContextNode getNodeByName(String traceId, String name) {
        ContextStack s = getStorage().getContext(traceId);
        if (s != null) {
            for (ContextNode r : s.getStack()) {
                if (StringUtils.equals(r.getName(), name)) {
                    return r;
                }
            }

            for (ContextNode n : s.getStack()) {
                if (n.getChildTask() != null) {
                    for (String ctraceId : n.getChildTask()) {
                        ContextNode cn = getNodeByName(ctraceId, name);
                        if (cn != null) {
                            return cn;
                        }
                    }
                }
            }
        }
        return null;
    }

    public PipeLineInfo init(SubscribableChannel customChannel) throws DFlowConstructionException {
        return init(getCallingPosition(), customChannel);
    }

    public PipeLineInfo init() throws DFlowConstructionException {
        return init(getCallingPosition());
    }

    public PipeLineInfo init(String pipelineName) throws DFlowConstructionException {
        return init(pipelineName, null);
    }

    public PipeLineInfo init(String pipelineName, SubscribableChannel customChannel) throws DFlowConstructionException {
        //设置特定pipeline的channel
        if (customChannel != null) {
            initMessageChannel(customChannel);
            g_customChannel.put(pipelineName, customChannel);
        }

        //使用init调用的位置作为最外层的parent，初始化使得parent和当前次数一样
        PipeLineInfo result = init(null, pipelineName, 0, null);
        result.setPipelineName(pipelineName);
        return result;
    }

    private String id;
    private static HashMap<String, String> idAndLines = new HashMap<>();

    public DFlow<T> id(String id) throws DFlowConstructionException {
        if (id == null) {
            return this;
        }
        if (this.debugName == null) {
            debugName = id;
        }
        this.id = id;

        //非内部节点检查id有没有重名。 内部节点由于getCallPosition 无法取到正确的位置，不计在内
        if (!internalNode) {
            String currentLine = getCallingPosition();
            if (idAndLines.get(id) == null) {
                idAndLines.put(id, currentLine);
            } else if (
                !(this instanceof DFlowJust) &&
                    !currentLine.equals(idAndLines.get(id))) {
                throw new DFlowConstructionException(
                    "Id: " + id + " already used!@" + currentLine + " |conflict with： " + idAndLines.get(id));
            }
        }
        return this;
    }

    public DFlow<T> name(String name) {
        this.debugName = name;
        return this;
    }

    /**
     * mock function, return null as do not mock
     *
     * @param mockFunc
     * @return
     */
    public DFlow<T> mock(Function<ContextStack, T> mockFunc) {
        this.mockFunc = mockFunc;
        return this;
    }

    /**
     * mock as value
     *
     * @param mockvalue
     * @return
     */
    public DFlow<T> mock(T mockvalue) {
        mockFunc = (c) -> mockvalue;
        return this;
    }

    public DFlow<T> onErrorReturn(Function<ContextStack, T> handler) throws InvalidClosureFunctionException {
        errorHandler = new ClosureEnabledFunction<>(handler, this, "error");
        return this;
    }

    public static <T> DFlow<T> just(T item) throws DFlowConstructionException {
        DFlowJust<T> s = new DFlowJust<T>(item);
        return s;
    }

    public static <T> DFlow<T> delay(T item, long delayMillis) throws DFlowConstructionException {
        DFlowDelay<T> s = new DFlowDelay<T>(delayMillis, item);
        return s;
    }

    public final <R> DFlow<R> flatMap(Function<? super T, ? extends DFlow<? extends R>> mapper, Class<R> clazz)
        throws DFlowConstructionException {
        DFlow<R> r = new DFlowFlatmap<T, R>(this, mapper, clazz);
        return r;
    }

    public final <R> DFlow<R> flatMap(Function<? super T, ? extends DFlow<? extends R>> mapper, TypeReference<R> clazz)
        throws DFlowConstructionException {
        DFlow<R> r = new DFlowFlatmap<T, R>(this, mapper, clazz.getType());
        return r;
    }

    public final <R> DFlow<String> flatMap(Function<? super T, ? extends DFlow<String>> mapper)
        throws DFlowConstructionException {
        return flatMap(mapper, String.class);
    }

    public final <R> DFlow<R> flatMap(BiFunction<ContextStack, ? super T, ? extends DFlow<? extends R>> mapper,
        Class<R> clazz)
        throws DFlowConstructionException {
        DFlow<R> r = new DFlowFlatmap<T, R>(this, mapper, clazz);
        return r;
    }

    public final <R> DFlow<R> flatMap(BiFunction<ContextStack, ? super T, ? extends DFlow<? extends R>> mapper,
        TypeReference<R> clazz)
        throws DFlowConstructionException {
        DFlow<R> r = new DFlowFlatmap<T, R>(this, mapper, clazz.getType());
        return r;
    }

    public final <R> DFlow<String> flatMap(BiFunction<ContextStack, ? super T, ? extends DFlow<String>> mapper)
        throws DFlowConstructionException {
        return flatMap(mapper, String.class);
    }

    public final <R> DFlow<R> map(Function<? super T, ? extends R> mapper, Class<R> clazz)
        throws DFlowConstructionException {
        DFlow<R> r = new DFlowMap<T, R>(this, mapper, clazz);
        return r;
    }

    public final <R> DFlow<R> map(Function<? super T, ? extends R> mapper, TypeReference<R> clazz)
        throws DFlowConstructionException {
        DFlow<R> r = new DFlowMap<T, R>(this, mapper, clazz.getType());
        return r;
    }

    public final <R> DFlow<R> map(BiFunction<ContextStack, ? super T, ? extends R> mapper, Class<R> clazz)
        throws DFlowConstructionException {
        DFlow<R> r = new DFlowMap<T, R>(this, mapper, clazz);
        return r;
    }

    public final <R> DFlow<R> map(BiFunction<ContextStack, ? super T, ? extends R> mapper, TypeReference<R> clazz)
        throws DFlowConstructionException {
        DFlow<R> r = new DFlowMap<T, R>(this, mapper, clazz.getType());
        return r;
    }

    public final <R> DFlow<String> map(Function<? super T, String> mapper) throws DFlowConstructionException {
        return map(mapper, String.class);
    }

    public final <R> DFlow<String> map(BiFunction<ContextStack, ? super T, String> mapper)
        throws DFlowConstructionException {
        return map(mapper, String.class);
    }

    public boolean isInternalNode() {
        return internalNode;
    }

    public static class Param {
        private DFlow<String> flow;
        private Consumer<ContextStack> trigger;

        public static Param pack(DFlow<String> flow, Consumer<ContextStack> trigger) {
            Param p = new Param();
            p.flow = flow;
            p.trigger = trigger;
            return p;
        }

        public static Param pack(DFlow<String> flow) {
            Param p = new Param();
            p.flow = flow;
            p.trigger = null;
            return p;
        }
    }

    public static <T> DFlowZip<T> zip(BiFunction<ContextStack, String[], T> zipper, Class<T> clazz, Param... params)
        throws DFlowConstructionException {
        DFlow<String>[] flows = new DFlow[params.length];
        Consumer<ContextStack>[] triggers = new Consumer[params.length];
        for (int i = 0; i < params.length; i++) {
            flows[i] = params[i].flow;
            triggers[i] = params[i].trigger;
        }
        return new DFlowZip<T>(flows,
            triggers,
            zipper, clazz);
    }

    public static <T> DFlowZip<T> zip(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        BiFunction<ContextStack, String[], T> zipper, Class<T> clazz) throws DFlowConstructionException {
        return new DFlowZip<T>(new DFlow[] {flow1, flow2},
            new Consumer[] {trigger1, trigger2},
            zipper, clazz);
    }

    public static <T> DFlowZip<T> zip(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        DFlow<String> flow3, Consumer<ContextStack> trigger3,
        BiFunction<ContextStack, String[], T> zipper, Class<T> clazz) throws DFlowConstructionException {
        return new DFlowZip<T>(new DFlow[] {flow1, flow2, flow3},
            new Consumer[] {trigger1, trigger2, trigger3},
            zipper, clazz);
    }

    public static <T> DFlowZip<T> zip(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        DFlow<String> flow3, Consumer<ContextStack> trigger3,
        DFlow<String> flow4, Consumer<ContextStack> trigger4,
        BiFunction<ContextStack, String[], T> zipper, Class<T> clazz) throws DFlowConstructionException {
        return new DFlowZip<T>(new DFlow[] {flow1, flow2, flow3, flow4},
            new Consumer[] {trigger1, trigger2, trigger3, trigger4},
            zipper, clazz);
    }

    public static <T> DFlowZip<T> zip(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        BiFunction<ContextStack, String[], T> zipper, TypeReference<T> clazz) throws DFlowConstructionException {
        return new DFlowZip<T>(new DFlow[] {flow1, flow2},
            new Consumer[] {trigger1, trigger2},
            zipper, clazz.getType());
    }

    public static <T> DFlowZip<T> zip(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        DFlow<String> flow3, Consumer<ContextStack> trigger3,
        BiFunction<ContextStack, String[], T> zipper, TypeReference<T> clazz) throws DFlowConstructionException {
        return new DFlowZip<T>(new DFlow[] {flow1, flow2, flow3},
            new Consumer[] {trigger1, trigger2, trigger3},
            zipper, clazz.getType());
    }

    public static <T> DFlowZip<T> zip(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        DFlow<String> flow3, Consumer<ContextStack> trigger3,
        DFlow<String> flow4, Consumer<ContextStack> trigger4,
        BiFunction<ContextStack, String[], T> zipper, TypeReference<T> clazz) throws DFlowConstructionException {
        return new DFlowZip<T>(new DFlow[] {flow1, flow2, flow3, flow4},
            new Consumer[] {trigger1, trigger2, trigger3, trigger4},
            zipper, clazz.getType());
    }

    public static <T> DFlowZip<String> zip(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        BiFunction<ContextStack, String[], String> zipper) throws DFlowConstructionException {
        return zip(flow1, trigger1, flow2, trigger2, zipper, String.class);
    }

    public static <T> DFlowZip<String> zip(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        DFlow<String> flow3, Consumer<ContextStack> trigger3,
        BiFunction<ContextStack, String[], String> zipper) throws DFlowConstructionException {
        return zip(flow1, trigger1, flow2, trigger2, flow3, trigger3, zipper, String.class);

    }

    public static <T> DFlowZip<String> zip(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        DFlow<String> flow3, Consumer<ContextStack> trigger3,
        DFlow<String> flow4, Consumer<ContextStack> trigger4,
        BiFunction<ContextStack, String[], String> zipper) throws DFlowConstructionException {
        return zip(flow1, trigger1, flow2, trigger2, flow3, trigger3, flow4, trigger4, zipper, String.class);
    }

    public static <T> DFlowZip<T> zip(Collection<DFlow<String>> flows, Collection<Consumer<ContextStack>> triggers,
        BiFunction<ContextStack, String[], T> zipper, TypeReference<T> clazz) throws DFlowConstructionException {
        if (triggers == null) {
            triggers = new ArrayList<>(flows.size());
        }
        if (flows.size() != triggers.size()) {
            throw new DFlowConstructionException("flows and triggers count should be same");
        }
        DFlow<String>[] flowsa = new DFlow[flows.size()];
        Consumer<ContextStack>[] triggersa = new Consumer[flows.size()];
        return new DFlowZip<T>(flows.toArray(flowsa)
            , triggers.toArray(triggersa),
            zipper, clazz.getType());
    }

    public static <T> DFlowZip<T> zip(DFlow<String>[] flows, Consumer<ContextStack>[] triggers,
        BiFunction<ContextStack, String[], T> zipper, TypeReference<T> clazz) throws DFlowConstructionException {
        if (triggers == null) {
            triggers = new Consumer[flows.length];
        }
        if (flows.length != triggers.length) {
            throw new DFlowConstructionException("flows and triggers count should be same");
        }
        return new DFlowZip<T>(flows, triggers,
            zipper, clazz.getType());
    }

    public static <T> DFlowOr<T> any(BiFunction<ContextStack, OrResult, T> orper, Class<T> clazz, Param... params)
        throws DFlowConstructionException {
        DFlow<String>[] flows = new DFlow[params.length];
        Consumer<ContextStack>[] triggers = new Consumer[params.length];
        for (int i = 0; i < params.length; i++) {
            flows[i] = params[i].flow;
            triggers[i] = params[i].trigger;
        }
        return new DFlowOr<T>(flows,
            triggers,
            orper, clazz);
    }

    public static <T> DFlowOr<T> any(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        BiFunction<ContextStack, OrResult, T> orper, Class<T> clazz) throws DFlowConstructionException {
        return new DFlowOr<T>(new DFlow[] {flow1, flow2},
            new Consumer[] {trigger1, trigger2},
            orper, clazz);
    }

    public static <T> DFlowOr<T> any(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        DFlow<String> flow3, Consumer<ContextStack> trigger3,
        BiFunction<ContextStack, OrResult, T> orper, Class<T> clazz) throws DFlowConstructionException {
        return new DFlowOr<T>(new DFlow[] {flow1, flow2, flow3},
            new Consumer[] {trigger1, trigger2, trigger3},
            orper, clazz);
    }

    public static <T> DFlowOr<T> any(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        DFlow<String> flow3, Consumer<ContextStack> trigger3,
        DFlow<String> flow4, Consumer<ContextStack> trigger4,
        BiFunction<ContextStack, OrResult, T> orper, Class<T> clazz) throws DFlowConstructionException {
        return new DFlowOr<T>(new DFlow[] {flow1, flow2, flow3, flow4},
            new Consumer[] {trigger1, trigger2, trigger3, trigger4},
            orper, clazz);
    }

    public static <T> DFlowOr<T> any(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        BiFunction<ContextStack, OrResult, T> orper, TypeReference<T> clazz) throws DFlowConstructionException {
        return new DFlowOr<T>(new DFlow[] {flow1, flow2},
            new Consumer[] {trigger1, trigger2},
            orper, clazz.getType());
    }

    public static <T> DFlowOr<T> any(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        DFlow<String> flow3, Consumer<ContextStack> trigger3,
        BiFunction<ContextStack, OrResult, T> orper, TypeReference<T> clazz) throws DFlowConstructionException {
        return new DFlowOr<T>(new DFlow[] {flow1, flow2, flow3},
            new Consumer[] {trigger1, trigger2, trigger3},
            orper, clazz.getType());
    }

    public static <T> DFlowOr<T> any(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        DFlow<String> flow3, Consumer<ContextStack> trigger3,
        DFlow<String> flow4, Consumer<ContextStack> trigger4,
        BiFunction<ContextStack, OrResult, T> orper, TypeReference<T> clazz) throws DFlowConstructionException {
        return new DFlowOr<T>(new DFlow[] {flow1, flow2, flow3, flow4},
            new Consumer[] {trigger1, trigger2, trigger3, trigger4},
            orper, clazz.getType());
    }

    public static <T> DFlowOr<String> any(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        BiFunction<ContextStack, OrResult, String> orper) throws DFlowConstructionException {
        return any(flow1, trigger1, flow2, trigger2, orper, String.class);
    }

    public static <T> DFlowOr<String> any(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        DFlow<String> flow3, Consumer<ContextStack> trigger3,
        BiFunction<ContextStack, OrResult, String> orper) throws DFlowConstructionException {
        return any(flow1, trigger1, flow2, trigger2, flow3, trigger3, orper, String.class);

    }

    public static <T> DFlowOr<String> any(DFlow<String> flow1, Consumer<ContextStack> trigger1,
        DFlow<String> flow2, Consumer<ContextStack> trigger2,
        DFlow<String> flow3, Consumer<ContextStack> trigger3,
        DFlow<String> flow4, Consumer<ContextStack> trigger4,
        BiFunction<ContextStack, OrResult, String> orper) throws DFlowConstructionException {
        return any(flow1, trigger1, flow2, trigger2, flow3, trigger3, flow4, trigger4, orper, String.class);
    }

    public static <T> DFlowOr<T> any(DFlow<String>[] flows, Consumer<ContextStack>[] triggers,
        BiFunction<ContextStack, OrResult, T> orper, TypeReference<T> clazz) throws DFlowConstructionException {
        if (triggers == null) {
            triggers = new Consumer[flows.length];
        }
        if (flows.length != triggers.length) {
            throw new DFlowConstructionException("flows and triggers count should be same");
        }
        return new DFlowOr<T>(flows, triggers,
            orper, clazz.getType());
    }

    public static <T> DFlowOr<T> any(Collection<DFlow<String>> flows, Collection<Consumer<ContextStack>> triggers,
        BiFunction<ContextStack, OrResult, T> orper, TypeReference<T> clazz) throws DFlowConstructionException {
        if (triggers == null) {
            triggers = new ArrayList<>(flows.size());
        }
        if (flows.size() != triggers.size()) {
            throw new DFlowConstructionException("flows and triggers count should be same");
        }
        DFlow<String>[] flowsa = new DFlow[flows.size()];
        Consumer<ContextStack>[] triggersa = new Consumer[flows.size()];
        return new DFlowOr<T>(flows.toArray(flowsa)
            , triggers.toArray(triggersa),
            orper, clazz.getType());
    }

    /**
     * @param callType 一个callType只能用在一个DFlow内
     * @param
     * @return
     * @throws DFlowConstructionException
     */
    public static final DFlow<String> fromCall(String callType) throws DFlowConstructionException {
        return new DFlowCall(callType);
    }

    public static final DFlow<String> fromCall(Entry callType) throws DFlowConstructionException {
        return new DFlowCall(callType.getCallType());
    }

    public static final DFlow<InnerList> fromCall(String... initEntrys) throws DFlowConstructionException {
        return new DFlowMultiCall(initEntrys);
    }
    public static final DFlow<InnerList> fromCall(Entry... initEntrys) throws DFlowConstructionException {
        return new DFlowMultiCall(Arrays.stream(initEntrys).map(Entry::getCallType).toArray(String[]::new));
    }

    public static class InitParam {
        private String param;
        private String traceId;

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }

        public String getTraceId() {
            return traceId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }
        public static InitParam of(String param,String traceId){
            InitParam i = new InitParam();
            i.param = param;
            i.traceId = traceId;
            return i;
        }
        public static InitParam of(String param){
            InitParam i = new InitParam();
            i.param = param;
            i.traceId = UUID.randomUUID().toString();
            return i;
        }
    }
    public interface FunctionValid extends Function<InitParam,DFlow<String>>, ValidClosure{
    }

    public static final void directRun(String param, FunctionValid init) throws Exception {
        directRun(InitParam.of(param), (FunctionValid)init);
    }
    public static final void directRun(InitParam param, FunctionValid init) throws Exception {
        if(!AbstractClosureEnabledFunction.valid(init)){
            throw new DFlowConstructionException("function should not have closure");
        }
        DFlow.fromCall("_DFlow_inner_init").id("_DFlow_inner_init")
                    .flatMap(x->init.apply(JSON.parseObject(x, InitParam.class))).id("_DFlow_inner_wrap_inner")
            .init().getEntry("_DFlow_inner_init").call(JSON.toJSONString(param) ,param.getTraceId());
    }

    public static final DFlow<String> fromCall(Consumer<ContextStack> action, String callType)
        throws DFlowConstructionException {
        return new DFlowCall(callType, action);
    }

    public static final DFlow<InnerList> fromCall(Consumer<ContextStack> action, String... initEntrys)
        throws DFlowConstructionException {
        String[] in = initEntrys;
        return new DFlowMultiCall(in, action);
    }

    public static final void call(Entry callEntry, String param, String traceId) throws Exception {
        if (callEntry.getDflowname() == null) {
            ContextStack currentState = getStorage().getContext(traceId);
            if (currentState == null) {
                throw new Exception(
                    "Init call must use Entry get from PipeLineInfo(created by DFlow.init()):" + traceId);
            }
            //if(currentState.getStack().peek().getChildTask() != null){
            //    throw new RuntimeException("Middle call should use child taskIDs:"+JSON.toJSONString(currentState
            // .getStack().peek().getChildTask()));
            //}
            if (currentState.isFinished()) {
                logger.error("Flow already finished:" + currentState.getId());
                return;
            }
            String callType = callEntry.getCallType() + currentState.getStack().peek().getName();

            InitEntry.call(callType, param, traceId);
        } else {
            InitEntry.call(callEntry.toInitEntryID(), param, traceId);
        }
    }

    public static final String call(Entry callType, String param) throws Exception {
        String traceId = UUID.randomUUID().toString();
        call(callType, param, traceId);
        return traceId;
    }

    public static String getCount(String IDName) {
        return getGlobalStorage().get("Static-" + IDName);
    }

    public static final void retry(String traceId, String flowStepId) {
        ContextNode c = getNodeByName(traceId, flowStepId);
        DFlow flow = STEPS.get(flowStepId);

    }

    /**
     * ===================== ==========Settings=========== =====================
     */

    private static ThreadLocal<Boolean> isLocal = new ThreadLocal<>();

    public static void setLocal() { isLocal.set(true); }

    private static ThreadLocal<String> g_pipelineName = new ThreadLocal<>();

    public static void setPipelineName(String name) { g_pipelineName.set(name); }

    private static ThreadLocal<String> traceId = new ThreadLocal<>();

    public static String getCurrentTraceId() {return traceId.get();}

    public static void setRequestResender(RequestResender requestResender) {
        InitEntry.setRequestResender(requestResender);
        InitEntry.setDflowInnerTransferCall(new BiFunction<String, String, Boolean>() {
            @Override
            public Boolean apply(String name, String traceId) throws Exception {
                String msgId = traceId + "_" + UUID.randomUUID().toString();
                if(g_debugLog) {
                    logger.info("DFLow trigger nextMsg resended:" + msgId);
                }
                SimpleMessage message = new SimpleMessage(
                    msgId,
                    name);
                //此处没有先尝试找当前设置的消息。偷懒了，由于仅仅发生在内部转换以调用，几率少，少隔离一下问题不是特别大
                return getMessageChannel().send(message);
            }
        });
    }

    private static SubscribableChannel messageChannel = null;

    public static SubscribableChannel getMessageChannel() { return messageChannel; }
    //
    //public static CustomDFlowFactory setCustomFactory(String type,CustomDFlowFactory factory){
    //    CustomDFlowFactory old = (customFactorys.get(type));
    //    customFactorys.put(type,factory);
    //    return old;
    //}

    public static void setSubscribableChannel(SubscribableChannel messageChannel) {
        DFlow.messageChannel = messageChannel;
        initMessageChannel(messageChannel);
    }

    private static void initMessageChannel(SubscribableChannel messageChannel) {
        messageChannel.subscribe(new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                //如果不小心分发到了未初始化的机器，重发
                try {
                    if (g_debugLog) {
                        logger.info("DFlow step:" + message.toString());
                    }
                    callReceived(message.getPayload().toString());
                } catch (PipeLineNotInitedOnMachineException e) {
                    logger.error("Received incomplete flowstep,{}", JSON.toJSONString(message));
                    throw e;
                } catch (RetryException e) {
                    logger.info("Failed once, retrying,{}", JSON.toJSONString(message));
                    throw new MessagingException(message);
                } catch (UserException t) {
                    try {
                        logger.error("User error", t);
                        CallerMessage m = JSON.parseObject(message.getPayload().toString(), CallerMessage.class);
                        DFlow.globalError(m.traceId, t.getCause());
                    } catch (Exception e) {
                        logger.error("DFlow systemError report error", e);
                        throw e;
                    }
                } catch (Throwable t) {
                    logger.error("DFlow systemError", t);
                    throw t;
                }
            }
        });
    }

    private static GlobalStoreInterface global = new GlobalStoreInterface() {

        @Override
        public Long incr(String key) {
            throw new RuntimeException("call DFlow.globalInit first");
        }

        @Override
        public Long decr(String key) {
            throw new RuntimeException("call DFlow.globalInit first");
        }

        @Override
        public String get(String key) {
            throw new RuntimeException("call DFlow.globalInit first");
        }

        @Override
        public void put(String key, String context) {
            throw new RuntimeException("call DFlow.globalInit first");
        }

        @Override
        public int keepAlive(String key) {
            throw new RuntimeException("call DFlow.globalInit first");
        }

        @Override
        public List<String> getIPs(String key) {
            throw new RuntimeException("call DFlow.globalInit first");
        }

        @Override
        public int getAlived(String key) {
            throw new RuntimeException("call DFlow.globalInit first");
        }
    };

    public static void setGlobalStorage(GlobalStoreInterface storage) {
        DFlow.global = new RetryProtectedGlobalStoreInterface(storage);
    }

    public static void setDelayManager(DFlowDelayManager delayManager){
        DFlowDelay.setDFlowDelayManager(delayManager);
    }

    public static ContextStoreInterface getStorage() {
        return storage;
    }

    protected static GlobalStoreInterface getGlobalStorage() {
        return global;
    }

    public static void setStorage(ContextStoreInterface storage) {
        DFlow.storage = new RetryProtectedContentStoreInterface(new ContextStoreInterface() {
            @Override
            public ContextStack getContext(String traceId) {
                return storage.getContext(traceId);
            }

            @Override
            public void putContext(String key, ContextStack context) throws PersistentException {
                if (context.isNoStorage()) {
                    return;
                }
                storage.putContext(key, context);
            }

            @Override
            public void expireContext(String traceId) {
                storage.expireContext(traceId);
            }

            @Override
            public void removeContext(String traceId) {
                storage.removeContext(traceId);
            }
        });
    }

    /**
     * 分布式存储，应该设置为rdb之类的
     */
    private static ContextStoreInterface storage;

    protected ContextStack getOrCreateCurrent(String traceId) throws PersistentException {
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }
        return getOrCreateCurrent(traceId, true);
    }

    private static List<com.alibaba.dflow.config.StepHandler> stepHandlers = new ArrayList<>();

    public static void addHandler(StepHandler h) {
        for (int i = 0; i < stepHandlers.size(); i++) {
            if (stepHandlers.get(i).order() > h.order()) {
                stepHandlers.add(i, h);
                return;
            }
        }
        stepHandlers.add(h);
    }

    /**
     * ===================== ==========Internal=========== =====================
     */

    /**
     * Fail Fast
     * 用于统计上一次DFlow创建时的行号，init时清空，一次init之间没换号会抛异常
     * 仍然不能处理所有情况，如
     * DFlow.just(1)
     * .flatMap(
     * x->if(x==1) DFlow.fromCall(entry1) else DFlow.fromCall(entry2)
     * )
     * 两个fromCall是无法区分的，用户写了只能自求多福。。
     */
    protected static ThreadLocal<String> lastPosition = new ThreadLocal<>();
    /**
     * 所有本地初始化好的代码，key为FlowStepId
     */
    protected static ConcurrentHashMap<String, DFlow> STEPS = new ConcurrentHashMap<>();

    /**
     * 是否每个执行通过消息换机器
     */
    public static boolean allAsync = true;

    /**
     * 外部创建时的代码位置
     */
    public StackTraceElement constructingPos;

    /**
     * 用于转换的类型
     */
    public Type clazz;

    /**
     * 下一节点
     */
    protected String nextStepId = null;

    /**
     * flowStepId是init后之后串联上下文的ID
     * 构建时确定，需保障各机器相同
     */
    private String flowStepId;
    /**
     * 确定代码实例的ID,通常是创建的代码位置，和FlowStepId的区别在于不管后续Flow
     */
    private String constructingPosStr;

    /**
     * 父节点
     */
    private String parentFlowStep;

    public void setParentFlowStep(String parentFlowStep) {
        this.parentFlowStep = parentFlowStep;
    }

    /**
     * debug使用的显示名称
     */
    private String debugName;
    /**
     * mock数据
     */
    private Function<ContextStack, T> mockFunc = null;

    /**
     * 异常处理回调
     */
    private ClosureEnabledFunction<T> errorHandler = null;

    /*
         getIDName是 确定串联后流节点的Identifier
     */
    public String getIDName() {
        return debugName == null ? String.valueOf(flowStepId) : (debugName + "-" + flowStepId);
    }

    protected String pipelineName;

    private PipeLineInfo currentPipeLineInfo = null;

    /**
     * 1.首先init一个DFlow
     * flowStepId根据当前函数功能和 下一节点 唯一确定数据流
     * flowStepId应该要能区分内部的两个分支流
     * DFlow.just(1).flatMap(x->if(x==1) DFlow.just(2).flatMap(y->DFlow.just(x)) else
     * DFlow.just(3).flatMap(y->DFlow.just(y)))
     *
     * @param parentSetter
     * @param stepcount    useless now
     * @param nextFlowID
     * @return
     * @throws Exception
     */
    public PipeLineInfo init(DFlow parentSetter, String pipelineName, int stepcount, String nextFlowID)
        throws DFlowConstructionException {

        if (g_strictMode) {
            if (id == null) {
                throw new DFlowConstructionException("Must have id in StrictMode@" + functionalUniqName());
            }
        }

        //去除当前创建行数的记录，再调用不计算同一行异常
        lastPosition.remove();

        if (flowStepId == null) {
            flowStepId = //superFlow +
                functionalUniqName() + "->" + nextFlowID;
            this.pipelineName = pipelineName;
            if (parentSetter != null) {
                parentSetter.parentFlowStep = functionalUniqName();
            }
        } else {
            throw new DFlowConstructionException("Can not init twice! Create new instance to reuse@"+flowStepId);
        }
        if(g_debugLog) {
            logger.info("********:" + getIDName());
        }

        this.nextStepId = nextFlowID;

        //如果已经初始化过了，不需再次计数，每次使用不一定类实例是同一个，但是代码层面的功能是一样的
        DFlow existFlow = STEPS.get(getIDName());
        if (existFlow != null) {
            return existFlow.currentPipeLineInfo;
        }

        //初始化次数
        global.keepAlive(getIDName());
        STEPS.put(getIDName(), this);

        currentPipeLineInfo = new PipeLineInfo();
        return currentPipeLineInfo;
    }

    /**
     * 2.然后触发一个DFlow节点
     * 所有DFlow的激活入口，一定调用它才进入工作状态
     * 模板模式解决从分布式系统中取context的部分
     *
     * @param traceId
     */
    public void call(String traceId) throws RetryException, UserException {
        DFlow.traceId.set(traceId);
        for (Pattern p : g_pattern) {
            if (p.matcher(traceId).find()) {
                error(getOrCreateCurrent(traceId), new Exception("traceId is omitted by pattern:" + p.pattern()));
                return;
            }
        }

        //保护同一个节点同时处理，极端情况可能同时走过来，需保证代码自己幂等
        ContextStack c = getStorage().getContext(traceId);
        if (c != null && c.getStack().peek() != null && STATUS_BEGIN.equals(c.getStack().peek().getStatus())) {
            logger.error("received duplicated call,already started:" + traceId + "@" + getIDName());
            return;
        }

        ContextStack contextStack = getOrCreateCurrent(traceId);
        if (isLocal.get() != null && isLocal.get()) {
            contextStack.setLocal();
        }

        //异常处理
        if (!contextStack.getStack().isEmpty()
            && ContextStack.STATUS_ERROR.equals(contextStack.getStack().peek().getStatus())) {
            errorReturn(contextStack);
            return;
        }

        //不重复处理已经过时的节点
        if (contextStack.getName() != null &&
            contextStack.getNextStepId() != null &&
            //判断执行进度是否为本节点， flatmap和正常节点实现有区别，当flatmap实现得更好的时候此处无需这么多种case
            !(
                parentFlowStep == null ||
                    (
                        getIDName().equals(contextStack.getNextStepId())
                            //兼容前一个版本没写nextStepId数据的节点
                            || (STEPS.get(contextStack.getName()) != null && getIDName().equals(
                            STEPS.get(contextStack.getName()).nextStepId))
                    )
            )) {
            logger.error("received duplicated call:" + traceId + "@" + getIDName());
            return;
        }

        //当前节点堆积统计
        getGlobalStorage().incr("Static-" + getIDName());

        //堆栈清理，将上层函数体出栈，结果作为当前参数，构建新的函数栈
        InternalHelper.rebuildNewStack(contextStack, getIDName(), debugName);

        //持久化
        getStorage().putContext(contextStack.getId(), contextStack);
        if (g_testMode && contextStack.isMock() && mockFunc != null) {
            try {
                T mockResult = mockFunc.apply(contextStack);
                if (mockResult != null) {
                    onReturn(contextStack, mockResult);
                    return;
                }
            } catch (Throwable t) {
                throw new UserException(t);
            }
        }

        try {
            stepHandlers.forEach(h -> h.onStepBefore(contextStack));
            if(stepHandlers.size() > 0) {
                stepHandlers.get(0).wrap(contextStack, (x) ->  callAfterStackBuild(x));
            }else{
                callAfterStackBuild(contextStack);
            }
        } catch (RetryException e) {
            InternalHelper.setStatus(contextStack, ContextStack.STATUS_RETRY + ":" + e.getMessage());
            getStorage().putContext(contextStack.getId(), contextStack);
            throw e;
        } catch (Throwable t) {
            throw new UserException(t);
        }

    }

    /**
     * 3.结束后触发下一个节点
     *
     * @param nextStepId
     * @param traceId
     */
    private void triggerNext(String nextStepId, String traceId) throws RetryException, UserException {
        getGlobalStorage().decr("Static-" + getIDName());
        if (nextStepId == null) {
            ContextStack contextStack = getOrCreateCurrent(traceId);
            contextStack.setFinished();
            InternalHelper.setNextStep(contextStack, "TERMINATED");
            getStorage().putContext(contextStack.getId(), contextStack);
            getStorage().expireContext(contextStack.getId());
            return;
        }
        ContextStack stack = getStorage().getContext(traceId);
        /*改到flatmap中做此逻辑
        //在异步模式且下一级flow已经初始化好的情况下一步调用，
        //if(!isAllInited(getIDName(),nextStepId)){
        //    stack.setLocal();
        //    getStorage().putContext(traceId,stack);
        //}
        */

        //rdb get fail, can not exception@triggerNext
        if (stack == null) {
            logger.error("Rdb access error:" + traceId);
            String msgId = traceId + InternalHelper.getStackIndex(stack);
            logger.info("DFLow trigger nextMsg:" + msgId);
            SimpleMessage message = new SimpleMessage(
                msgId,
                JSON.toJSONString(new CallerMessage(traceId, nextStepId)));
            if (!getMessageChannel(pipelineName).send(message)) {
                logger.error("MQ send failed:" + traceId);
                globalError(traceId, new Exception("MQ send fail"));
            }
            ;
        } else {
            //记录下一次step
            InternalHelper.setNextStep(stack, nextStepId);
            getStorage().putContext(traceId, stack);
        }

        if (allAsync
            //&& !stack.isLocal()
        ) {
            String msgId = traceId + InternalHelper.getStackIndex(stack);
            if(g_debugLog) {
                logger.info("DFLow trigger nextMsg:" + msgId);
            }
            SimpleMessage message = new SimpleMessage(
                msgId,
                JSON.toJSONString(new CallerMessage(traceId, nextStepId)));
            if (!getMessageChannel(pipelineName).send(message)) {
                logger.error("MQ send failed:" + traceId);
                globalError(traceId, new Exception("MQ send fail"));
            }
            ;
        } else {
            STEPS.get(nextStepId).call(traceId);
        }
    }

    private ConcurrentHashMap<String, SubscribableChannel> g_customChannel = new ConcurrentHashMap<>();

    private SubscribableChannel getMessageChannel(String pipelineName) {
        if (g_customChannel.contains(pipelineName)) {
            return g_customChannel.get(pipelineName);
        } else {
            return getMessageChannel();
        }
    }

    /**
     * 4.收到消息进行下一个轮回
     *
     * @param message
     * @throws MessagingException
     */
    private static void callReceived(String message) throws RetryException, UserException {
        CallerMessage m = JSON.parseObject(message, CallerMessage.class);
        DFlow flow = STEPS.get(m.getName());

        ContextStack contextStack = getStorage().getContext(m.getTraceId());

        //local的call尝试发回去
        if (contextStack.isLocal() && !StringUtils.equals(InternalHelper.getIp(), contextStack.getIP())) {
            if(InitEntry.transferDflowInnerCall(m, contextStack.getIP())){
                return;
            }else{
                //转发也没有，可能目标ip的机器已经下线，如果要求local，去除local再拯救一下
                contextStack.removeLocal();
                DFlow.getStorage().putContext(m.getTraceId(), contextStack);
                throw new RetryException("transfer local back failed, remove local&retry@" + m.getTraceId());
            }
        }

        if (flow == null) {
            List<String> ips = (global.getIPs(m.getName()));
            if (ips == null || ips.size() == 0) {
                if (global.getAlived(STEPS.keys().nextElement()) == 0) {
                    throw new RetryException("System not inited");
                }
                throw new PipeLineNotInitedOnMachineException();
            } else {
                logger.error("Local no step "+ m.getName() +", transfer to " + ips.get(0) + "@" + m.getTraceId());
                if (!InitEntry.transferDflowInnerCall(m, ips.get(0))) {
                    throw new RetryException("transfer call failed@" + m.getTraceId());
                }
                ;
            }
        } else {
            flow.call(m.getTraceId());
        }
    }

    /**
     * ===================== ==========扩展DFlow内部使用API=========== =====================
     */

    /**
     * 准备好栈以后调用
     *
     * @param context
     * @return
     * @throws RetryException
     */
    protected abstract boolean callAfterStackBuild(ContextStack context) throws RetryException, UserException;

    /**
     * 二进制功能相同的函数名
     * 难于实现时使用创建时的位置和行号
     *
     * @return
     */
    protected abstract String functionalUniqName(String constructingPos);

    protected String functionalUniqName() {
        if (id != null) {
            return id;
        }
        return functionalUniqName(constructingPosStr);
    }

    public DFlow(Type clazz) throws DFlowConstructionException {
        this(getCallingPosition(), clazz);
    }

    /**
     * 内部辅助节点，行数判断逻辑使用，不做检查，可视化也可以忽略
     */
    private boolean internalNode = false;

    /**
     * @param identifier
     * @param clazz
     * @param internalNode
     * @throws DFlowConstructionException
     */
    public DFlow(String identifier, Type clazz, boolean internalNode) throws DFlowConstructionException {
        this.internalNode = internalNode;
        this.constructingPosStr = identifier;
        this.clazz = clazz;
        constructingPos = getCallingPositionOriginal();
        if (!g_strictMode && !internalNode && this.constructingPosStr.equals(lastPosition.get())) {
            throw new DFlowConstructionException(
                "You must chain the flow in different line:" + this.constructingPosStr);
        }
        lastPosition.set(this.constructingPosStr);
    }

    /**
     * @param identifier 自定义的代码唯一确定ID
     * @param clazz
     * @throws DFlowConstructionException
     */
    public DFlow(String identifier, Type clazz) throws DFlowConstructionException {
        this(identifier, clazz, false);
    }

    /**
     * 所有DFlow实现拿到结果后调用的函数，调用它以开始下一个步骤
     * 辅助函数，当step结束时调用，将结果存入状态，通知监听回调，以便伺机发起下一个Step
     *
     * @param context
     * @param t
     */
    protected void onReturn(ContextStack context, T t) throws RetryException, UserException {
        InternalHelper.setResultAndStatus(context, t, ContextStack.STATUS_END);
        //设好值后持久化，触发下一step
        getStorage().putContext(context.getId(), context);

        //触发回调
        stepHandlers.forEach(h -> h.onStepAfter(context));

        //不持久化contenxt只是为了单节点调试，就不要再往后了
        if (context.isNoStorage()) {
            return;
        }
        triggerNext(nextStepId, context.getId());
    }

    protected static void globalError(String traceId, Throwable t) {
        ContextStack contextStack = DFlow.getStorage().getContext(traceId);
        logger.error("DFlow SystemError:", t);
        if (contextStack != null) {
            contextStack.appendError(t);
            try {
                DFlow.getStorage().putContext(traceId, contextStack);
            } catch (PersistentException e) {
                logger.error("SystemError return failed@" + traceId, t);
            }
        }
    }

    protected void error(ContextStack context, Throwable t) throws RetryException, UserException {
        if (t instanceof TerminalException) {
            logger.info("Terminaled:" + context.getId() + "@" + context.getStack().peek().getName());
            return;
        }
        //子流程结束处理
        if(t instanceof EndException){
            context.setFinished();
            getStorage().putContext(context.getId(), context);
            getStorage().expireContext(context.getId());
            return;
        }

        logger.error("Flow step RuntimeError:" + context.getId(), t);
        if (!getIDName().equals(context.getName())) {
            logger.error("Current name:" + getIDName() + ", context name:" + context.getName());
        } else {
            context.setLocal();
            context.getStack().peek().setStatus(ContextStack.STATUS_ERROR);
            context.appendError(t);
            //context.getStack().peek().put("Error:",t.getClass().toString()+"|"+ t.getMessage());
            getStorage().putContext(context.getId(), context);
        }
        if (t instanceof RetryException) {
            throw (RetryException)t;
        }
        errorReturn(context);
    }

    /**
     * ===================== ==========辅助Util代码=========== =====================
     */
    private void errorReturn(ContextStack contextStack) throws RetryException, UserException {
        if (errorHandler != null) {
            T errorData = null;
            try {
                errorData = errorHandler.apply(contextStack);
                //构建本层函数栈
                InternalHelper.rebuildNewStack(contextStack, getIDName(), debugName);
                onReturn(contextStack, errorData);
            } catch (TerminalException e) {
                logger.info("Terminated:" + contextStack.getId() + "@" + contextStack.getStack().peek().getName());
            } catch (Exception e) {
                logger.error("Error happened in errorHandler:" + getIDName(), e);
                triggerNext(nextStepId, contextStack.getId());
            }
        } else {
            triggerNext(nextStepId, contextStack.getId());
        }
    }

    /**
     * 只有在每台机器都初始化过，才能放心交给metaq随便分发
     *
     * @param flowStepName
     * @return
     */
    protected static boolean isAllInited(String thisStepIDname, String flowStepName) {
        int parentCount = global.getAlived(thisStepIDname);
        return parentCount == global.getAlived(flowStepName);
    }

    public static StackTraceElement getCallingPositionOriginal() {
        StackTraceElement[] stack = new Exception().getStackTrace();
        String last = "";
        for (int i = 0; i < stack.length; i++) {
            if (stack[i].toString().startsWith("com.alibaba.dflow")) {
                continue;
            }
            return stack[i];
        }
        return null;
    }

    private ContextStack getOrCreateCurrent(String traceId, boolean forceCreate) throws PersistentException {
        ContextStack c = getStorage().getContext(traceId);
        if (c == null && forceCreate) {
            c = new ContextStack();
            c.setId(traceId);
            getStorage().putContext(traceId, c);
        }
        return c;
    }

    protected static String getCallingPosition() {
        StackTraceElement call = getCallingPositionOriginal();
        return (call == null) ? "" : call.toString();
    }

    public static class SimpleMessage implements Message {
        private String msg;
        private MessageHeaders headers;

        public SimpleMessage(String id, String msg) {
            HashMap map = new HashMap();
            map.put("KEYS", id);
            headers = new MessageHeaders(map);
            this.msg = msg;
        }

        @Override
        public Object getPayload() {
            return msg;
        }

        @Override
        public MessageHeaders getHeaders() {
            return headers;
        }
    }

    public static class CallerMessage {
        private String traceId;
        private String name;

        public CallerMessage() {}

        public CallerMessage(String traceId, String name) {
            this.name = name;
            this.traceId = traceId;
        }

        public String getTraceId() {
            return traceId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    public static void globalInit(GlobalStoreInterface globalStoreInterface,
        RequestResender resender,
        ContextStoreInterface contextStoreInterface,
        SubscribableChannel channel
    ) {
        setSubscribableChannel(channel);
        setRequestResender(resender);
        setGlobalStorage(globalStoreInterface);
        setStorage(contextStoreInterface);
    }

    public static void globalInitForTest() {
        DFlow.g_testMode = true;

        DFlow.setDelayManager(new DFlowDelayManager() {
            @Override
            protected void addTask(String idname, String traceId, long timeout) throws Exception {
                Thread.sleep(timeout);
                resume(idname, traceId);
            }
        });
        DFlow.setGlobalStorage(new GlobalStoreInterface() {
            private ConcurrentHashMap<String, AtomicLong> counter = new ConcurrentHashMap<>();
            private ConcurrentHashMap<String, String> m = new ConcurrentHashMap<>();

            @Override
            public Long incr(String s) {
                if (counter.get(s) != null) {
                    return counter.get(s).incrementAndGet();
                } else {
                    counter.put(s, new AtomicLong(0));
                    return counter.get(s).incrementAndGet();
                }
            }

            @Override
            public Long decr(String s) {
                if (counter.get(s) != null) {
                    return counter.get(s).decrementAndGet();
                } else {
                    counter.put(s, new AtomicLong(0));
                    return counter.get(s).decrementAndGet();
                }
            }

            @Override
            public String get(String s) {
                return m.get(s);
            }

            @Override
            public void put(String s, String s1) {
                m.put(s, s1);
            }

            @Override
            public int keepAlive(String s) {
                return 1;
            }

            @Override
            public int getAlived(String s) {
                return 1;
            }

            @Override
            public List<String> getIPs(String key) {
                return null;
            }
        });

        DFlow.setRequestResender(new RequestResender() {
            private Callback callback;

            @Override
            public void setCallback(Callback callback) {
                this.callback = callback;
            }

            @Override
            public boolean resendCall(String s, String s1, String s2, String s3) {
                System.out.println("Local Test won't Resend");
                return false;
            }
        });

        DFlow.setStorage(new ContextStoreInterface() {

            ConcurrentHashMap<String, String> m = new ConcurrentHashMap<>();

            @Override
            public ContextStack getContext(String s) {
                String result = m.get(s);
                if (result == null) {
                    return null;
                }
                return ContextStack.rebuild(result);
            }

            @Override
            public void putContext(String s, ContextStack contextStack) throws PersistentException {
                m.put(s, contextStack.dump());
            }

            @Override
            public void expireContext(String traceId) {
                m.remove(traceId);
            }

            @Override
            public void removeContext(String s) {
                m.remove(s);
            }
        });

        DFlow.setSubscribableChannel(new org.springframework.messaging.SubscribableChannel() {

            MessageHandler handler;

            @Override
            public boolean send(Message<?> message) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.handleMessage(message);
                    }
                }).start();
                return true;
            }

            @Override
            public boolean send(Message<?> message, long timeout) {
                return send(message);
            }

            @Override
            public boolean subscribe(MessageHandler messageHandler) {
                handler = messageHandler;
                return true;
            }

            @Override
            public boolean unsubscribe(MessageHandler messageHandler) {
                return true;
            }
        });

        System.out.println("TestDFLow inited");
    }
}