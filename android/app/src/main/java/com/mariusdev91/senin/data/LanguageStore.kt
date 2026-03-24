package com.mariusdev91.senin.data

import android.content.Context
import com.mariusdev91.senin.i18n.AppLanguage

class LanguageStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): AppLanguage = AppLanguage.fromCode(prefs.getString(KEY_LANGUAGE, null))

    fun save(language: AppLanguage) {
        prefs.edit()
            .putString(KEY_LANGUAGE, language.code)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "senin_language"
        private const val KEY_LANGUAGE = "language"
    }
}
