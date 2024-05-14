package com.runt9.kgdf.ext

import com.runt9.kgdf.inject.Injector
import ktx.reflect.ReflectedClass
import ktx.reflect.reflect
import kotlin.reflect.KClass

inline fun <reified Type : Any> inject(): Type = Injector.inject()
inline fun <reified Type : Any> lazyInject() = lazy { inject<Type>() }

@Suppress("UNCHECKED_CAST")
fun <Type : Any> Injector.newInstanceOf(clazz: KClass<Type>): Type {
    val constructor = ReflectedClass(clazz.java).constructor
    val parameters = constructor.parameterTypes.map { getProvider(it).invoke() }.toTypedArray()
    return constructor.newInstance(*parameters) as Type
}

@Suppress("UNCHECKED_CAST")
fun <Type : Any> dynamicInject(clazz: KClass<out Type>, vararg additionalInjects: Pair<(Class<*>) -> Boolean, Any>): Type {
    val constructor = reflect(clazz).constructor

    val parameters = constructor.parameterTypes.map { param ->
        additionalInjects.forEach { (test, value) -> if (test(param)) return@map value }
        return@map Injector.getProvider(param).invoke()
    }.toTypedArray()

    return constructor.newInstance(*parameters) as Type
}

fun dynamicInjectCheckIsSubclassOf(clazz: Class<*>) = { classToCheck: Class<*> ->
    val matchFinder = { c: Class<*> -> c == clazz || c.superclass == clazz || c.interfaces.contains(clazz) }
    var foundMatch = matchFinder(classToCheck)
    var currentClazz = classToCheck

    while (currentClazz.superclass != null && !foundMatch) {
        currentClazz = currentClazz.superclass
        foundMatch = matchFinder(currentClazz)
    }

    foundMatch
}
fun dynamicInjectCheckAssignableFrom(clazz: Class<*>) = { c: Class<*> -> c.isAssignableFrom(clazz) }
