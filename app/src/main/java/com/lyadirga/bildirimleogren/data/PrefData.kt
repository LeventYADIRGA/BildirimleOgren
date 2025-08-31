package com.lyadirga.bildirimleogren.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PrefData(context: Context) {

    private var sharedPref: SharedPreferences = context.getSharedPreferences(PREF_DATA, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_DATA = "Preferences"
        private const val INDEX = "index"
        private const val CALISMA_SETI = "calisma_seti"
        private const val DEFAULT_INDEX = -1
        private const val DEFAULT_CALISMA_SETI = 0
        private const val IS_FIRST_LAUNCH = "is_first_launch"
    }

    fun resetIndex(){
        sharedPref.edit {
            putInt(INDEX, DEFAULT_INDEX)
        }
    }

    fun setIndex(index: Int){
        sharedPref.edit {
            putInt(INDEX, index)
        }
    }

    fun getIndex(): Int{
      return sharedPref.getInt(INDEX, DEFAULT_INDEX)
    }

    fun setCalismaSeti(index: Int){
        sharedPref.edit {
            putInt(CALISMA_SETI, index)
        }

    }

    fun getCalismaSeti(): Int{
        return sharedPref.getInt(CALISMA_SETI, DEFAULT_CALISMA_SETI)
    }

    // ✅ İlk açılışı takip eden fonksiyonlar
    fun isFirstLaunch(): Boolean {
        return sharedPref.getBoolean(IS_FIRST_LAUNCH, true)
    }

    fun setFirstLaunch(value: Boolean) {
        sharedPref.edit {
            putBoolean(IS_FIRST_LAUNCH, value)
        }
    }
}