package com.alibaba.dflow.test;

import java.util.function.BiFunction;

public class main {
    public static void main(String args[]){
        try {
            B s = new B();
            s.a();
            System.out.println(s.a);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

abstract class A{
    abstract void a();

     BiFunction<String,String,String> a;
    void b() throws Exception {

    }

}

class B extends A{

    @Override
    void a() {
        a = new BiFunction<String, String, String>() {
            @Override
            public String apply(String s, String s2) {
                return null;
            }
        };
    }
}

class C extends A{

    @Override
    void a() {

    }
}