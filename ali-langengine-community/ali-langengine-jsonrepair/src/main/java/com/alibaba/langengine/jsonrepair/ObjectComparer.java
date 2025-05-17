package com.alibaba.langengine.jsonrepair;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ObjectComparer - A utility class to compare objects
 * 
 * This class provides methods to compare objects and determine if they have
 * the same structure.
 */
public class ObjectComparer {
    
    /**
     * Checks if two objects have the same structure
     * 
     * @param obj1 The first object
     * @param obj2 The second object
     * @return true if the objects have the same structure, false otherwise
     */
    public static boolean isSameObject(Object obj1, Object obj2) {
        return isSameObject(obj1, obj2, "");
    }
    
    /**
     * Checks if two objects have the same structure
     * 
     * @param obj1 The first object
     * @param obj2 The second object
     * @param path The current path in the object structure
     * @return true if the objects have the same structure, false otherwise
     */
    @SuppressWarnings("unchecked")
    private static boolean isSameObject(Object obj1, Object obj2, String path) {
        // Check if types match
        if ((obj1 == null && obj2 != null) || (obj1 != null && obj2 == null)) {
            return false;
        }
        
        if (obj1 == null && obj2 == null) {
            return true;
        }
        
        if (!obj1.getClass().equals(obj2.getClass())) {
            return false;
        }
        
        // Compare maps (JSON objects)
        if (obj1 instanceof Map) {
            Map<String, Object> map1 = (Map<String, Object>) obj1;
            Map<String, Object> map2 = (Map<String, Object>) obj2;
            
            // Compare keys
            Set<String> keys1 = map1.keySet();
            Set<String> keys2 = map2.keySet();
            
            if (!keys1.equals(keys2)) {
                return false;
            }
            
            // Recursively compare values
            for (String key : keys1) {
                if (!isSameObject(map1.get(key), map2.get(key), path + "/" + key)) {
                    return false;
                }
            }
            
            return true;
        }
        
        // Compare lists (JSON arrays)
        if (obj1 instanceof List) {
            List<Object> list1 = (List<Object>) obj1;
            List<Object> list2 = (List<Object>) obj2;
            
            if (list1.size() != list2.size()) {
                return false;
            }
            
            for (int i = 0; i < list1.size(); i++) {
                if (!isSameObject(list1.get(i), list2.get(i), path + "[" + i + "]")) {
                    return false;
                }
            }
            
            return true;
        }
        
        // Compare primitive values
        return obj1.equals(obj2);
    }
}
