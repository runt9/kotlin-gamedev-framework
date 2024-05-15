package com.runt9.kgdf.inject

import ktx.inject.Context

abstract class InjectorDependencies {
    fun initStartupDeps(ctx: Context) = ctx.initStartupDepsInternal()
    fun initServiceDeps(ctx: Context) = ctx.initServiceDepsInternal()
    fun initControllerDeps(ctx: Context) = ctx.initControllerDepsInternal()
    fun initOtherRunningDeps(ctx: Context) = ctx.initOtherRunningDepsInternal()

    protected open fun Context.initStartupDepsInternal() {}
    protected open fun Context.initServiceDepsInternal() {}
    protected open fun Context.initControllerDepsInternal() {}
    protected open fun Context.initOtherRunningDepsInternal() {}
}

typealias AdditionalInjectorDependencies = MutableList<InjectorDependencies>
fun AdditionalInjectorDependencies.initStartupDeps(ctx: Context) = forEach { it.initStartupDeps(ctx) }
fun AdditionalInjectorDependencies.initServiceDeps(ctx: Context) = forEach { it.initServiceDeps(ctx) }
fun AdditionalInjectorDependencies.initControllerDeps(ctx: Context) = forEach { it.initControllerDeps(ctx) }
fun AdditionalInjectorDependencies.initOtherRunningDeps(ctx: Context) = forEach { it.initOtherRunningDeps(ctx) }
