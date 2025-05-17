package com.alibaba.langengine.jsonrepair;

import java.util.ArrayList;
import java.util.List;

/**
 * JsonContext - A class to track the context during JSON parsing
 * 
 * This class helps the parser keep track of the current context
 * (object key, object value, or array) during parsing.
 */
public class JsonContext {
    
    /**
     * Enum representing different context values during JSON parsing
     */
    public enum ContextValues {
        OBJECT_KEY,
        OBJECT_VALUE,
        ARRAY
    }
    
    private List<ContextValues> context;
    private ContextValues current;
    private boolean empty;
    
    /**
     * Creates a new JsonContext
     */
    public JsonContext() {
        this.context = new ArrayList<>();
        this.current = null;
        this.empty = true;
    }
    
    /**
     * Sets a new context value
     * 
     * @param value The context value to set
     */
    public void set(ContextValues value) {
        context.add(value);
        current = value;
        empty = false;
    }
    
    /**
     * Resets the most recent context value
     */
    public void reset() {
        if (!context.isEmpty()) {
            context.remove(context.size() - 1);
            current = context.isEmpty() ? null : context.get(context.size() - 1);
            empty = context.isEmpty();
        }
    }
    
    /**
     * Checks if the context is empty
     * 
     * @return true if the context is empty, false otherwise
     */
    public boolean isEmpty() {
        return empty;
    }
    
    /**
     * Gets the current context value
     * 
     * @return The current context value
     */
    public ContextValues getCurrent() {
        return current;
    }
    
    /**
     * Checks if the context contains a specific value
     * 
     * @param value The value to check for
     * @return true if the context contains the value, false otherwise
     */
    public boolean contains(ContextValues value) {
        return context.contains(value);
    }
}
