package com.alibaba.dflow.func;

/**
 * Safe object must be identical across machines!
 * @param <T>
 */
public class SafeObject<T>  implements ValidClosure{
    public T data;
}