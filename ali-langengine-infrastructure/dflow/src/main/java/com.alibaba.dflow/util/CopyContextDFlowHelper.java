package com.alibaba.dflow.util;

import java.util.Arrays;
import java.util.function.BiFunction;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.InitEntry.Entry;
import com.alibaba.dflow.func.ValidClosure;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;

import io.reactivex.functions.Function;

import static com.alibaba.dflow.internal.DFlowMultiCall.nonInheritGlobalKeys;

public class CopyContextDFlowHelper implements ValidClosure {
    Entry innerEntry;
    private String name;

    public interface ConstructFunction extends BiFunction<ContextStack,String, DFlow<String>>, ValidClosure {
    }

    public String getName() {
        return name;
    }

    public CopyContextDFlowHelper(String name, ConstructFunction innerFlow) throws DFlowConstructionException {
        this.name = name;
        //子流程用新id-inner，清除上下文防止干扰
        innerEntry = DFlow.fromCall("DFlow_Agent_Tool" + name).id("DFlow_Agent_Tool" + name)
            .map((c, x) -> {
                String upperId = removeLastInner(c.getId());
                ContextStack ctx = DFlow.getStorage().getContext(upperId);
                for(String key:ctx.getGlobal().keySet()){
                    if(!nonInheritGlobalKeys.contains(key)){
                        c.getGlobal().put(key, ctx.getGlobal().get(key));
                    }
                }
                return x;
            }).id("DFlow_newcontext_copyParam")
            .flatMap((c,x) -> innerFlow.apply(c,x)).id("DFlow_Agent_Tool_Real" + name)
            .map((c, x) -> {
                // 去掉_inner后缀
                String upperId = removeLastInner(c.getId());
                DFlow.call(new Entry("DFlow_Agent_Tool_Back" + getName()), x, upperId);
                return x;
            }).id("DFlow_Agent_Tool_CallBack" + name)
            .init().getEntry("DFlow_Agent_Tool" + name);
    }

    public String removeLastInner(String id) {
        String[] parts = id.split("_inner"); // 分割所有 _inner 出现的位置
        if (parts.length <= 1) {
            return id; // 没有或只有一个 _inner，直接返回原字符串
        }
        // 取前 parts.length -1 个部分，并用 "_inner" 连接
        return String.join("_inner", Arrays.copyOf(parts, parts.length - 1));
    }

    public DFlow<String> run(ContextStack ctx, String toolInput) throws DFlowConstructionException {
        return DFlow.just(toolInput) // just开头以支持并发时数据不串
            .map((c1, x) -> { c1.put("_toolInput", x); return x; }).id("DFlow_Agent_Tool_PrepareInput")
            .flatMap((c2, x) ->
                DFlow.fromCall((c) -> {
                    String innerId = c.getId() + "_inner" + c.getStack().size();
                    c.put("_DFLOW_INNERID", innerId);
                    innerEntry.call(c.get("_toolInput"), innerId);
                }, "DFlow_Agent_Tool_Back" + name).id("DFlow_Agent_Tool_Back" + name)
            ).id("DFlow_Agent_Tool_BackOutter" + name);
    }
}
