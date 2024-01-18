package com.lyadirga.bildirimleogren

import android.content.Context
import android.content.SharedPreferences

class PrefData(context: Context) {

    private var sharedPref: SharedPreferences

    companion object {
        private const val PREF_DATA = "Preferences"
        private const val INDEX = "index"
        private const val CALISMA_SETI = "calisma_seti"
        private const val DEFAULT_INDEX = 0
        private const val DEFAULT_CALISMA_SETI = 0
    }

    init {
        sharedPref = context.getSharedPreferences(PREF_DATA, Context.MODE_PRIVATE)
    }

    fun setIndex(index: Int){
        with(sharedPref.edit()) {
            putInt(INDEX, index)
            apply()
        }
    }

    fun getIndex(): Int{
      return sharedPref.getInt(CALISMA_SETI, DEFAULT_INDEX)
    }

    fun setCalismaSeti(index: Int){
        with(sharedPref.edit()) {
            putInt(CALISMA_SETI, index)
            apply()
        }
    }

    fun getCalismaSeti(): Int{
        return sharedPref.getInt(CALISMA_SETI, DEFAULT_CALISMA_SETI)
    }
}