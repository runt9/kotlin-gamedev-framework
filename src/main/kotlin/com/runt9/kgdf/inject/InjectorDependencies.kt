package com.runt9.kgdf.inject

import ktx.inject.Context

abstract class InjectorDependencies {
    fun initStartupDeps(ctx: Context) = ctx.initStartupDepsInternal()
    fun initRunningDeps(ctx: Context) = ctx.initRunningDepsInternal()

    protected fun Context.initStartupDepsInternal() {}
    protected fun Context.initRunningDepsInternal() {}
}
