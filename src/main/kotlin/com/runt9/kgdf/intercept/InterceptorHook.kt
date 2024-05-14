package com.runt9.kgdf.intercept

// TODO: Any way to force this to be implemented as an enum or something? This may not work
interface InterceptorHook

enum class BaseInterceptorHook : InterceptorHook {
    ON_CHANGE
}
