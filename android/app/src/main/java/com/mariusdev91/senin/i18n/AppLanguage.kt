package com.mariusdev91.senin.i18n

enum class AppLanguage(
    val code: String,
    val nativeLabel: String,
) {
    Romanian("ro", "Romana"),
    English("en", "English"),
    Hungarian("hu", "Magyar");

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code } ?: Romanian
    }
}
