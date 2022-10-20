@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.bhm.sdk.support

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.lang.IllegalArgumentException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * 本地存储 不要使用
 * @author buhuiming
 * @since  2022/10/19 16:22
 */
object SPUtil {

    fun put(context: Context, key: String?, value: Any) {
        put(context, context.packageName, key, value)
    }

    fun put(context: Context, fileName: String?, key: String?, value: Any) {
        val sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val editor = sp.edit()
        when (value) {
            is String -> {
                editor.putString(key, value)
            }
            is Int -> {
                editor.putInt(key, value)
            }
            is Boolean -> {
                editor.putBoolean(key, value)
            }
            is Float -> {
                editor.putFloat(key, value)
            }
            is Long -> {
                editor.putLong(key, value)
            }
            else -> {
                editor.putString(key, value.toString())
            }
        }
        SharedPreferencesCompat.apply(editor)
    }

    operator fun get(context: Context, key: String, defaultObject: Any): Any {
        return SPUtil[context, context.packageName, key, defaultObject]
    }

    @JvmStatic
    operator fun get(context: Context, fileName: String, key: String, defaultObject: Any): Any {
        return if (contains(context, fileName, key)) {
            val sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            when (defaultObject) {
                is String -> {
                    return sp.getString(key, defaultObject as String?)?: defaultObject
                }
                is Int -> {
                    return sp.getInt(key, (defaultObject as Int?)!!)
                }
                is Boolean -> {
                    return sp.getBoolean(key, (defaultObject as Boolean?)!!)
                }
                is Float -> {
                    return sp.getFloat(key, (defaultObject as Float?)!!)
                }
                is Long -> {
                    return sp.getLong(key, (defaultObject as Long?)!!)
                }
                else -> defaultObject
            }
        } else {
            defaultObject
        }
    }

    fun getAll(context: Context, fileName: String): Map<String, *> {
        val sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sp.all
    }

    fun removeKeyValue(context: Context, fileName: String, key: String) {
        if (contains(context, fileName, key)) {
            val sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            val editor = sp.edit()
            editor.remove(key)
            SharedPreferencesCompat.apply(editor)
        }
    }

    fun removeAll(context: Context, fileName: String) {
        val sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.clear()
        SharedPreferencesCompat.apply(editor)
    }

    fun contains(context: Context, fileName: String, key: String): Boolean {
        val sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sp.contains(key)
    }

    fun contains(context: Context, key: String): Boolean {
        return contains(context, context.packageName, key)
    }

    /**
     * commit方法是同步的,apply方法是异步的，所以尽量使用apply方法.
     * 但是apply是new api，为了兼容低版本客户端,使用以下兼容方案
     */
    private object SharedPreferencesCompat {
        private fun findApplyMethod(): Method? {
            try {
                val clz: Class<*> = SharedPreferences.Editor::class.java
                return clz.getMethod("apply")
            } catch (e: NoSuchMethodException) {
                Log.e("SPUtil", e.localizedMessage?: "NoSuchMethodException")
            }
            return null
        }

        fun apply(editor: SharedPreferences.Editor) {
            try {
                val sApplyMethod = findApplyMethod()
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor)
                    return
                }
            } catch (e: IllegalArgumentException) {
                Log.e("SPUtil", e.localizedMessage?: "IllegalArgumentException")
            } catch (e: IllegalAccessException) {
                Log.e("SPUtil", e.localizedMessage?: "IllegalAccessException")
            } catch (e: InvocationTargetException) {
                Log.e("SPUtil", e.localizedMessage?: "InvocationTargetException")
            }
            editor.commit()
        }
    }
}