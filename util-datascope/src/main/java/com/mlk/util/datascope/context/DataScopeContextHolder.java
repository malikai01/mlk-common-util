package com.mlk.util.datascope.context;

/**
 * @author malikai
 * @version 1.0
 * @date 2021-2-5 14:18
 */
public class DataScopeContextHolder {

    private static final ThreadLocal<String> LOCAL_CONTEXT = new InheritableThreadLocal<>();

    public static String getDataScopeParams() {
        return LOCAL_CONTEXT.get();
    }

    public static void setDataScopeParams(String value) {
        LOCAL_CONTEXT.set(value);
    }

    public static void clean(){
        LOCAL_CONTEXT.remove();
    }
}
