package com.example.farmhelper.ui.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.ComponentActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object LanguageManager {
    private val LANGUAGE_KEY = stringPreferencesKey("language_preference")

    var currentLanguage = "en"

    fun getLanguageFlow(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY] ?: getDefaultLocale(context)
        }
    }

    suspend fun saveLanguage(context: Context, languageCode: String) {
        currentLanguage = languageCode
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }

    fun getDefaultLocale(context: Context): String {
        val systemLocale = context.resources.configuration.locales.get(0)?.language ?: "en"
        return when (systemLocale) {
            "hi" -> "hi"
            "gu" -> "gu"
            else -> "en"
        }
    }

    fun get(key: String): String {
        return when (currentLanguage) {
            "gu" -> gujarati[key] ?: key
            "hi" -> hindi[key] ?: key
            else -> english[key] ?: key
        }
    }

    fun translateDynamic(text: String): String {
        val trimmed = text.trim()
        val lowerText = trimmed.lowercase()
        return when (currentLanguage) {
            "gu" -> gujaratiDynamic[lowerText] ?: (gujarati[trimmed] ?: (gujarati[lowerText] ?: trimmed))
            "hi" -> hindiDynamic[lowerText] ?: (hindi[trimmed] ?: (hindi[lowerText] ?: trimmed))
            else -> text
        }
    }

    private val gujaratiDynamic = mapOf(
        // Crops
        "groundnut" to "મગફળી",
        "cotton" to "કપાસ",
        "wheat" to "ઘઉં",
        "rice" to "ચોખા",
        "paddy" to "ડાંગર",
        "sugarcane" to "શેરડી",
        "mustard" to "રાઈ",
        "castor seed" to "દિવેલા",
        "cumin seeds" to "જીરું",
        "sesamum" to "તલ",
        "gram" to "ચણા",
        "onion" to "ડુંગળી",
        "potato" to "બટાકા",
        "tomato" to "ટામેટા",
        "garlic" to "લસણ",
        "chilli" to "મરચાં",
        
        // Districts
        "rajkot" to "રાજકોટ",
        "junagadh" to "જૂનાગઢ",
        "amreli" to "અમરેલી",
        "bhavnagar" to "ભાવનગર",
        "jamnagar" to "જામનગર",
        "morbi" to "મોરબી",
        "porbandar" to "પોરબંદર",
        "gir somnath" to "ગીર સોમનાથ",
        "ahmedabad" to "અમદાવાદ",
        "surat" to "સૂરત",
        "vadodara" to "વડોદરા",
        "mehsana" to "મહેસાણા",
        "banaskantha" to "બનાસકાંઠા",
        "sabarkantha" to "સાબરકાંઠા",
        "patan" to "પાટણ",
        "surendranagar" to "સુરેન્દ્રનગર",
        "gandhinagar" to "ગાંધીનગર",
        "kheda" to "ખેડા",
        "anand" to "આણંદ",
        "bharuch" to "ભરૂચ",
        "narmada" to "નર્મદા",
        "tapi" to "તાપી",
        "navsari" to "નવસારી",
        "valsad" to "વલસાડ",
        "dangs" to "ડાંગ",
        "dahod" to "દાહોદ",
        "panchmahal" to "પંચમહાલ",
        "mahisagar" to "મહીસાગર",
        "aravalli" to "અરવલ્લી",
        "chhota udepur" to "છોટાઉદેપુર",
        "botad" to "બોટાદ",
        "devbhumi dwarka" to "દેવભૂમિ દ્વારકા",
        "devbhoomi dwarka" to "દેવભૂમિ દ્વારકા",
        "kutch" to "કચ્છ",
        
        // Markets
        "gondal" to "ગોંડલ",
        "jetpur" to "જેતપુર",
        "jasdan" to "જસદણ",
        "wankaner" to "વાંકાનેર",
        "gondal(veg)" to "ગોંડલ(શાકભાજી)",
        "gondal(fruit)" to "ગોંડલ(ફળો)",
        "mahuva" to "મહુવા",
        "visnagar" to "વિસનગર",
        "deesa" to "ડીસા",
        "thara" to "થરા",
        "himatanagar" to "હિંમતનગર",
        "idar" to "ઇડર",
        "halvad" to "હળવદ",
        "nadiad" to "નડીઆદ",
        "kapadwanj" to "કપડવંજ",
        "borsad" to "બોરસદ",
        "morvi" to "મોરબી",
        "dharampur" to "ધરમપુર",
        "vyara" to "વ્યારા",
        
        // Decisions & Insights
        "good time to sell" to "વેચવા માટે સારો સમય",
        "sell immediately" to "તરત જ વેચો",
        "hold" to "અટકાવો (હોલ્ડ)",
        "strong hold" to "મજબૂત રીતે અટકાવો",
        "hold selling (price down)" to "વેચાણ અટકાવો (ભાવ નીચો છે)",
        "good day to sell!" to "વેચવા માટે સારો દિવસ!",
        
        // Common reasons
        "demand is increasing in local markets" to "સ્થાનિક બજારોમાં માંગ વધી રહી છે",
        "supply is lower due to unseasonal rain" to "કમોસમી વરસાદને કારણે સપ્લાય ઓછી છે",
        "good export demand from other states" to "અન્ય રાજ્યોમાંથી નિકાસની સારી માંગ છે",
        "hold crop as prices are expected to rise next month" to "ભાવ આવતા મહિને વધવાની ધારણા હોવાથી પાક અટકાવી રાખો",
        "msp is higher than prevailing market price" to "એમએસપી હાલના બજાર ભાવ કરતા વધારે છે",
        "market arrivals are expected to peak soon, causing price drop" to "બજારમાં આવક ટૂંક સમયમાં પીક પર પહોંચવાની ધારણા છે, જેનાથી ભાવ ઘટશે",
        "steady demand with moderate market arrivals" to "મધ્યમ બજાર આવક સાથે સ્થિર માંગ",
        "prices are near the season high, sell now" to "ભાવ સીઝનની ઊંચી સપાટીની નજીક છે, અત્યારે વેચો"
    )

    private val hindiDynamic = mapOf(
        // Crops
        "groundnut" to "मूंगफली",
        "cotton" to "कपास",
        "wheat" to "गेहूं",
        "rice" to "चावल",
        "paddy" to "धान",
        "sugarcane" to "गन्ना",
        "mustard" to "सरसों",
        "castor seed" to "अरंडी",
        "cumin seeds" to "जीरा",
        "sesamum" to "तिल",
        "gram" to "चना",
        "onion" to "प्याज",
        "potato" to "आलू",
        "tomato" to "टमाटर",
        "garlic" to "लहसुन",
        "chilli" to "मिर्च",
        
        // Districts
        "rajkot" to "राजकोट",
        "junagadh" to "जूनागढ़",
        "amreli" to "अमरेली",
        "bhavnagar" to "भावनगर",
        "jamnagar" to "जामनगर",
        "morbi" to "मोरबी",
        "porbandar" to "पोरबंदर",
        "gir somnath" to "गिर सोमनाथ",
        "ahmedabad" to "अहमदाबाद",
        "surat" to "सूरत",
        "vadodara" to "वडोदरा",
        "mehsana" to "मेहसाणा",
        "banaskantha" to "बनासकांठा",
        "sabarkantha" to "साबरकांठा",
        "patan" to "पाटन",
        "surendranagar" to "सुरेंद्रनगर",
        "gandhinagar" to "गांधीनगर",
        "kheda" to "खेड़ा",
        "anand" to "आनंद",
        "bharuch" to "भरूच",
        "narmada" to "नर्मदा",
        "tapi" to "तापी",
        "navsari" to "नवसारी",
        "valsad" to "वलसाड",
        "dangs" to "डांग",
        "dahod" to "दाहोद",
        "panchmahal" to "पंचमहाल",
        "mahisagar" to "महीसागर",
        "aravalli" to "अरावली",
        "chhota udepur" to "छोटा उदयपुर",
        "botad" to "बोटाद",
        "devbhumi dwarka" to "देवभूमि द्वारका",
        "devbhoomi dwarka" to "देवभूमि द्वारका",
        "kutch" to "कच्छ",
        
        // Markets
        "gondal" to "गोंडल",
        "jetpur" to "गेतपुर",
        "jasdan" to "जसदण",
        "wankaner" to "वांकानेर",
        "gondal(veg)" to "गोंडल(सब्जी)",
        "gondal(fruit)" to "गोंडल(फल)",
        "mahuva" to "महुआ",
        "visnagar" to "विसनगर",
        "deesa" to "डीसा",
        "thara" to "थरा",
        "himatanagar" to "हिम्मतनगर",
        "idar" to "इदर",
        "halvad" to "हलवद",
        "nadiad" to "नडियाद",
        "kapadwanj" to "कपड़वंज",
        "borsad" to "बोरसद",
        "morvi" to "मोरबी",
        "dharampur" to "धर्मपुर",
        "vyara" to "व्यारा",
        
        // Decisions & Insights
        "good time to sell" to "बेचने के लिए अच्छा समय",
        "sell immediately" to "तुरंत बेचें",
        "hold" to "रोकें (होल्ड करें)",
        "strong hold" to "मजबूती से रोकें",
        "hold selling (price down)" to "बिक्री रोकें (कीमत कम है)",
        "good day to sell!" to "बेचने के लिए अच्छा दिन है!",
        
        // Common reasons
        "demand is increasing in local markets" to "स्थानीय बाजारों में मांग बढ़ रही है",
        "supply is lower due to unseasonal rain" to "बेमौसम बारिश के कारण आपूर्ति कम है",
        "good export demand from other states" to "अन्य राज्यों से निर्यात की अच्छी मांग है",
        "hold crop as prices are expected to rise next month" to "अगले महीने कीमतें बढ़ने की उम्मीद में फसल रोक कर रखें",
        "msp is higher than prevailing market price" to "एमएसपी मौजूदा बाजार मूल्य से अधिक है",
        "market arrivals are expected to peak soon, causing price drop" to "बाजार में आवक जल्द ही चरम पर पहुंचने की उम्मीद है, जिससे कीमतें घटेंगी",
        "steady demand with moderate market arrivals" to "मध्यम बाजार आवक के साथ स्थिर मांग",
        "prices are near the season high, sell now" to "कीमतें सीजन के उच्चतम स्तर के करीब हैं, अभी बेचें"
    )

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

private tailrec fun Context.findActivity(): ComponentActivity? {
    return when (this) {
        is ComponentActivity -> this
        is android.content.ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

@Composable
fun LocalizedApp(
    context: Context,
    content: @Composable () -> Unit
) {
    val languageFlow = remember { LanguageManager.getLanguageFlow(context) }
    val currentLanguage by languageFlow.collectAsState(initial = LanguageManager.currentLanguage)

    LaunchedEffect(currentLanguage) {
        LanguageManager.currentLanguage = currentLanguage
    }

    val locale = remember(currentLanguage) { java.util.Locale(currentLanguage) }
    val configuration = context.resources.configuration
    configuration.setLocale(locale)

    val localizedContext = remember(currentLanguage) {
        context.createConfigurationContext(configuration)
    }

    val activity = context.findActivity()
    if (activity != null) {
        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalActivityResultRegistryOwner provides activity,
            LocalOnBackPressedDispatcherOwner provides activity
        ) {
            content()
        }
    } else {
        CompositionLocalProvider(LocalContext provides localizedContext) {
            content()
        }
    }
}