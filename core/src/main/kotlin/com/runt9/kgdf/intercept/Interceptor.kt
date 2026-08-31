package com.runt9.kgdf.intercept

interface Interceptor<T : InterceptableContext> {
    val hook: InterceptorHook

    fun intercept(context: T)
    fun canIntercept(context: T): Boolean
}

inline fun <reified T : InterceptableContext> intercept(hook: InterceptorHook, crossinline interceptor: (context: T) -> Unit) = object : Interceptor<T> {
    override val hook = hook
    override fun intercept(context: T) = interceptor(context)
    override fun canIntercept(context: T) = context::class == T::class
}

