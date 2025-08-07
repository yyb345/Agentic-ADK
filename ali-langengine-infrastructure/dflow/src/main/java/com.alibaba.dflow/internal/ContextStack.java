package com.alibaba.dflow.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;

/**
 * 每一个计算流计算中的状态
 * 各层变量栈,
 */
public class ContextStack{

    public static final String STATUS_BEGIN = "BEGIN";
    public static final String STATUS_SUB = "PREPARE_SUBCALL";
    public static final String STATUS_END = "END";
    public static final String STATUS_RETRY = "FAIL_RETRY";
    public static final String STATUS_RETRYING = "RETRYING";
    public static final String STATUS_ERROR = "ERROR";
    public static final String ERROR_MSG = "ERROR_MSG";

    private String id;
    private HashMap<String,Object> global = new HashMap<>();

    private Stack<ContextNode> stack = new Stack<>();

    private static HashSet<String> INNERKEYS = new HashSet<String>(){{
        this.add(ContextNode.ID);
        this.add(ContextNode.NAME);
    }};

    public HashMap<String,Object> getGlobal(){return global;}

    @Override
    public ContextStack clone() {
        ContextStack stack  =new ContextStack();
        stack.setId(this.getId());
        stack.global = (HashMap<String, Object>)this.global.clone();
        stack.stack = (Stack<ContextNode>)this.stack.clone();
        return stack;
    }

    public String dump(){
        HashMap<String,Object> res = new HashMap<>();
        res.put("id",getId());
        res.put("global",JSON.toJSONString(global));
        res.put("stack",JSON.toJSONString(stack));
        return JSON.toJSONString(res);
    }

    public static ContextStack rebuild(String dumpString){
        HashMap obj = JSON.parseObject(dumpString,HashMap.class);
        ContextStack s = new ContextStack();
        s.setId((String)obj.get("id"));
        s.global = JSON.parseObject((String)obj.get("global"),new TypeReference<HashMap<String, Object>>(){});
        s.stack = JSON.parseObject((String)obj.get("stack"), new TypeReference<Stack<ContextNode>>(){});
        return s;
    }



    public ContextNode findNodeByDebugName(String debugName){
        if(debugName == null){return null;}
        for(ContextNode n : stack){
            if(debugName.equals(n.getDebugname())){
                return n;
            }
        }
        return null;
    }

    void setName(String name){
        stack.peek().setName(name);
    }
    public String getName(){
        if(stack.empty()){
            return null;
        }
        return stack.peek().getName();
    }

    void setChildTask(String[] childTask){
        if(stack.empty()){
            return;
        }
        stack.peek().setChildTask(childTask);
    }

    public Stack<ContextNode> getStack(){
        return stack;
    }

    void pop(){
        if(stack.empty()){
            return;
        }
        stack.pop();
    }

    void setClosureData(String key,HashMap<String,Object> closureData){
        this.stack.peek().setClosure(key,closureData);
    }
    HashMap<String,Object> getClosureData(String key){
        return this.stack.peek().getClosure(key);
    }

    void setStack(Stack<ContextNode> stack){
        this.stack = stack;
    }

    public void put(String key,Object o) {
        global.put(key,o);
    }

    void setStatus(String status){
        stack.peek().setStatus(status);
    }
    public String getErrorMsg(){
        return String.valueOf(global.get(ERROR_MSG));
    }

    public void appendError(Throwable t){
        String errorMsg = t.getClass().toString()+"|"+ t.getMessage()+";";
        appendErrorMsg(errorMsg);
    }
    public void appendErrorMsg(String info){
        String errorMsg = info+";";
        errorMsg += getGlobal().get(ContextStack.ERROR_MSG);
        getGlobal().put(ContextStack.ERROR_MSG,errorMsg);
    }

    public boolean isFinished() {return global.get("_FINISHED") != null;}
    public void setFinished() {global.put("_FINISHED",true);}

    /**
     * 当前上下文是否持久化，仅仅用于单节点调试
     */
    public void setNoStorage() {global.put("_CURRENTREPLY",true);}
    public boolean isNoStorage() { return global.get("_CURRENTREPLY") != null;}

    public boolean isLocal(){
        return global.get("_ISLOCAL") != null;
    }

    public void removeLocal(){
        global.remove("_ISLOCAL");
    }
    public void setLocal(){
        global.put("_ISLOCAL",true);
    }

    public void unsetLocal(){
        global.remove("_ISLOCAL");
    }

    public boolean isMock(){
        return global.get("_ISMOCK") != null;
    }

    public void setMock(){
        global.put("_ISMOCK",true);
    }

    public void unsetMock(){
        global.remove("_ISMOCK");
    }

    public void setNextStepId(String nextStepId) {
        global.put("_NEXTSTEP",nextStepId);
    }

    public String getNextStepId(){
        return (String)global.get("_NEXTSTEP");
    }

    String getStatus(){
        int i = stack.size();
        if(i > 0) {
            return stack.peek().getStatus();
        }
        return null;
    }

    Object getResult(){
        int i = stack.size();
        if(i > 0){
            return stack.get(i-1).getRet();
        }
        return null;
    }

    Object getParam(){
        int i = stack.size();
        if(i > 0){
            return stack.get(i-1).getParam();
        }
        return null;
    }
    void setResult(Object o){
        stack.peek().setRet(o);
    }

    void push(ContextNode node){
        stack.push(node);
    }

    void addAll(ContextStack stack){
        this.stack.addAll(stack.stack);
    }

    int size(){
        return stack.size();
    }

    public String get(String key){
        return String.valueOf(global.get(key));
    }

    public <T> T get (String key, Class<T> clazz){
        return TypeUtils.cast(global.get(key),clazz, ParserConfig.getGlobalInstance());
    }
    public <T> T get (String key, TypeReference<T> clazz){
        return TypeUtils.cast(global.get(key),clazz.getType(), ParserConfig.getGlobalInstance());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIP(String IP) {
        stack.peek().setIP(IP);
    }
    public String getIP(){
        if(stack.isEmpty()){
            return InternalHelper.getIp();
        }
        return stack.peek().getIP();
    }


    public static class ContextNode<T> extends HashMap<String,Object> {

        public static final String ID = "_ID"; //dynamic
        public static final String NAME = "_NAME";//dynamic
        public static final String RET = "_RET";//dynamic
        public static final String PARAM = "_PARAM";//dynamic
        public static final String STATUS = "_STATUS";//dynamic
        public static final String CLOSURE = "_CLOSURE";//dynamic
        public static final String IP = "_IP";//dynamic
        public static final String DEBUGNAME = "_DEBUGNAME";//dynamic
        public static final String CHILDTASK = "_CHILDTASK";//dynamic
        public static final String STARTTIME = "_STARTTIME";//dynamic
        public static final String COSTTIME = "COSTTIME";//dynamic
        public static final String CURRENT_INDEX = "SCATTER_CURRENT";
        public static final String TOTAL_SIZE = "SCATTER_TOTAL_CHILDSIZE";
        public static final String PARALLEL = "SCATTER_IS_PARALLEL";

        public Integer getCurrentIndex(){
            return (Integer)get(CURRENT_INDEX);
        }

        public void setCurrentIndex(int index){
            put(CURRENT_INDEX,index);
        }

        public Integer getChildTotalSize(){
            return (Integer)get(TOTAL_SIZE);
        }

        public void setChildTotalSize(int size){
            put(TOTAL_SIZE,size);
        }

        public boolean isParallel(){
            Boolean r = (Boolean)get(PARALLEL);
            return r == null ? false: r;
        }

        public void setParallel(){
            put(PARALLEL,true);
        }


        public String[] getChildTask(){ Object s = get(CHILDTASK);
            if(s instanceof String[]){
                return (String[])s;
            }else if(s instanceof JSONArray){
                List<String> array = JSON.parseArray(s.toString(),String.class);
                return (String[])array.toArray(new String[array.size()]);
            }
            return null;
        }
        public void setChildTask(String[] c) {put(CHILDTASK,c);}

        public T getRet(){
            return (T)get(RET);
        }
        public void setRet(T r){
            put(RET,r);
        }
        public T getParam(){
            return (T)get(PARAM);
        }
        public void setParam(Object r){
            put(PARAM,r);
        }

        public String getId(){
            return (String)get(ID);
        }
        public void setId(String id){
            put(ID,id);
        }

        public Long getCostTime(){
            return Long.parseLong(String.valueOf(get(COSTTIME)));
        }
        public void setCostTime(Long id){
            put(COSTTIME,id);
        }

        public Long getStartTime(){
            return (Long)get(STARTTIME);
        }
        public void setStartTime(Long id){
            put(STARTTIME,id);
        }

        public String getStatus(){
            return (String)get(STATUS);
        }
        public void setStatus(String status){
            put(STATUS,status);
        }

        public String getName(){
            return (String)get(NAME);
        }
        public void setName(String name){
            put(NAME,name);
        }

        public String getIP(){
            return (String)get(IP);
        }
        public void setIP(String name){
            put(IP,name);
        }


        public void setDebugName(String debugName) {
            put(DEBUGNAME,debugName);
        }
        public String getDebugname(){
            return (String)get(DEBUGNAME);
        }

        public HashMap<String,Object> getClosure(String key){
            return (HashMap<String, Object>)get(CLOSURE+"-"+key);
        }
        public void setClosure(String key,HashMap<String,Object> data){
            put(CLOSURE+"-"+key,data);
        }

    }

}
