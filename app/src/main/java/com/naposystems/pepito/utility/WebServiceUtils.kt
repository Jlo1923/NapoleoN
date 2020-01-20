package com.naposystems.pepito.utility

import timber.log.Timber
import java.lang.reflect.Field
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class WebServiceUtils {

    companion object {
        fun get422Errors(clazz: Any): ArrayList<String> {

            val errorList = ArrayList<String>()

            val fields: Array<Field> = clazz.javaClass.declaredFields

            for (field in fields) {

                try {

                    field.isAccessible = true

                    val name: List<String> = readInstanceProperty(clazz, field.name)

                    if (name.isNotEmpty())
                        for (error in name)
                            errorList.add(error)

                } catch (e: Exception) {
                    Timber.e(e)
                }
            }

            return errorList

        }

        @Suppress("UNCHECKED_CAST")
        private fun <R> readInstanceProperty(clazz: Any, propertyName: String): R {
            val property = clazz::class.memberProperties
                // don't cast here to <Any, R>, it would succeed silently
                .first { it.name == propertyName } as KProperty1<Any, *>
            // force a invalid cast exception if incorrect type here
            return property.get(clazz) as R
        }
    }
}