package com.runt9.kgdf.intercept

interface InterceptableContext : InterceptorHolder {
    fun intercept(hook: InterceptorHook) {
        interceptors[hook]?.filter { it.canIntercept(this) }?.forEach { it.intercept(this) }
    }

    fun intercept(hook: InterceptorHook, context: InterceptableContext) {
        context.interceptors[hook]?.forEach { it.intercept(context) }
    }
}
