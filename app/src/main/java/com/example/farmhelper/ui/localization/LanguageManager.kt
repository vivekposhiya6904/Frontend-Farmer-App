package com.example.farmhelper.ui.localization

object LanguageManager {

    var currentLanguage = "en"

    fun get(key: String): String {

        return when (currentLanguage) {

            "gu" -> gujarati[key] ?: key

            "hi" -> hindi[key] ?: key

            else -> english[key] ?: key
        }
    }

    private val english = mapOf(
        "weather" to "Weather Forecast",
        "prices" to "Crop Prices",
        "smart" to "Smart Farming",
        "next" to "Next",
        "skip" to "Skip",
        "start" to "Get Started",
        "login" to "Sign In",
        "create_account" to "Create Account",
        "forgot_password" to "Forgot Password?"
    )

    private val hindi = mapOf(
        "weather" to "मौसम पूर्वानुमान",
        "prices" to "फसल मूल्य",
        "smart" to "स्मार्ट खेती",
        "next" to "आगे",
        "skip" to "छोड़ें",
        "start" to "शुरू करें",
        "login" to "लॉगिन",
        "create_account" to "खाता बनाएं",
        "forgot_password" to "पासवर्ड भूल गए?"
    )

    private val gujarati = mapOf(
        "weather" to "હવામાન આગાહી",
        "prices" to "પાકના ભાવ",
        "smart" to "સ્માર્ટ ખેતી",
        "next" to "આગળ",
        "skip" to "છોડી દો",
        "start" to "શરૂ કરો",
        "login" to "લોગિન",
        "create_account" to "એકાઉન્ટ બનાવો",
        "forgot_password" to "પાસવર્ડ ભૂલી ગયા?"
    )
}