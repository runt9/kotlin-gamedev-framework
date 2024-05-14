@file:Suppress("UNCHECKED_CAST")

package com.runt9.kgdf.intercept

interface InterceptorHolder {
    val interceptors: MutableMap<InterceptorHook, MutableSet<Interceptor<InterceptableContext>>>

    fun addInterceptors(incomingInterceptors: Map<InterceptorHook, MutableSet<Interceptor<InterceptableContext>>>) =
        incomingInterceptors.forEach { (hook, interceptors) -> this.interceptors.computeIfAbsent(hook) { mutableSetOf() }.addAll(interceptors) }

    fun addInterceptors(holder: InterceptorHolder) = addInterceptors(holder.interceptors)

    fun removeInterceptors(holder: InterceptorHolder) =
        holder.interceptors.forEach { (hook, interceptors) -> this.interceptors[hook]?.removeAll(interceptors) }
}

inline fun <reified T : InterceptableContext> InterceptorHolder.addInterceptor(hook: InterceptorHook, interceptor: Interceptor<T>) {
    interceptors.computeIfAbsent(hook) { mutableSetOf() }.add(interceptor as Interceptor<InterceptableContext>)
}

inline fun <reified T : InterceptableContext> InterceptorHolder.addInterceptor(hook: InterceptorHook, crossinline interceptorFn: (T) -> Unit) {
    addInterceptor(hook, intercept(hook, interceptorFn))
}

inline fun <reified T : InterceptableContext> InterceptorHolder.removeInterceptor(hook: InterceptorHook, interceptor: Interceptor<T>): Boolean {
    return interceptors[hook]?.remove(interceptor as Interceptor<InterceptableContext>) ?: false
}

abstract class InterceptorHolderAdapter : InterceptorHolder {
    override val interceptors = mutableMapOf<InterceptorHook, MutableSet<Interceptor<InterceptableContext>>>()
}

abstract class InterceptableAdapter : InterceptorHolderAdapter(), InterceptableContext
