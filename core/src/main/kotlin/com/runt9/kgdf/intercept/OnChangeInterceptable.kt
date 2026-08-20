@file:Suppress("UNCHECKED_CAST")

package com.runt9.kgdf.intercept

inline fun <reified T : InterceptableContext> onChange(crossinline interceptorFn: (context: T) -> Unit) =
    intercept(BaseInterceptorHook.ON_CHANGE, interceptorFn)

abstract class OnChangeInterceptable : InterceptableAdapter() {
    inline fun <reified T : InterceptableContext> onChange(interceptor: Interceptor<T>) = addInterceptor(BaseInterceptorHook.ON_CHANGE, interceptor)
    inline fun <reified T : InterceptableContext> removeOnChange(interceptor: Interceptor<T>) = removeInterceptor(BaseInterceptorHook.ON_CHANGE, interceptor)

    fun <T : OnChangeInterceptable> update(consumer: T.() -> Unit) {
        (this as T).consumer()
        intercept(BaseInterceptorHook.ON_CHANGE)
    }
}
