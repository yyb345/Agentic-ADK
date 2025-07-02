package com.alibaba.dflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.dflow.InitEntry.Entry;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.fastjson.JSON;

public class PipeLineInfo {
    private String pipelineName;
    private Map<String,Entry> entryMap = new HashMap<>();

    public Entry getEntry(String callType){
        Entry entry = null;
        entry =(entryMap.get(callType));
        if(entry == null) {
            entry = new Entry();
            entry.setCallType(callType);
        }
        entry.setPipelineName(pipelineName);
        return entry;
    }

    public static void call(String callType,String param,String traceId) throws Exception{
        DFlow.call(new Entry(callType),param,traceId);
    }

    public void call(String callType,String param) throws Exception {
        DFlow.call(getEntry(callType),param);
    }

    public void putEntry(String callType,Entry e){
        entryMap.put(callType,e);
    }
    public void setEntryMap(Map<String, Entry> entryMap) {
        this.entryMap = entryMap;
    }

    public static PipeLineInfo merge(PipeLineInfo a,PipeLineInfo b) throws DFlowConstructionException {
        int sizeA = a.entryMap.size();
        int sizeB = b.entryMap.size();
        PipeLineInfo result = new PipeLineInfo();
        HashMap<String,Entry> resultP = new HashMap<>();
        resultP.putAll(a.entryMap);
        resultP.putAll(b.entryMap);
        result.entryMap = resultP;
        if(resultP.size() != sizeA + sizeB){
            Set<String> confliction = new HashSet<>();
            for(String key: a.entryMap.keySet()){
                if(b.entryMap.containsKey(key)){
                    confliction.add(key);
                }
            }

            throw new DFlowConstructionException("Conflicting entry!"+ JSON.toJSONString(confliction));
        }
        return result;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }
}
