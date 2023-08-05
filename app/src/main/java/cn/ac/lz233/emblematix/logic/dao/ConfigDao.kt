package cn.ac.lz233.emblematix.logic.dao

import cn.ac.lz233.emblematix.App

object ConfigDao {
    var copyright: String
        get() = App.sp.getString("copyright", "")!!
        set(value) = App.editor.putString("copyright", value).apply()

    var showManufacturer: Boolean
        get() = App.sp.getBoolean("showManufacturer", true)
        set(value) = App.editor.putBoolean("showManufacturer", value).apply()

    var showModel: Boolean
        get() = App.sp.getBoolean("showModel", true)
        set(value) = App.editor.putBoolean("showModel", value).apply()

    var showFNumber: Boolean
        get() = App.sp.getBoolean("showFNumber", true)
        set(value) = App.editor.putBoolean("showFNumber", value).apply()

    var showShutterSpeed: Boolean
        get() = App.sp.getBoolean("showShutterSpeed", true)
        set(value) = App.editor.putBoolean("showShutterSpeed", value).apply()

    var showFocalLength: Boolean
        get() = App.sp.getBoolean("showFocalLength", true)
        set(value) = App.editor.putBoolean("showFocalLength", value).apply()

    var showISO: Boolean
        get() = App.sp.getBoolean("showISO", true)
        set(value) = App.editor.putBoolean("showISO", value).apply()

    var showDateTime: Boolean
        get() = App.sp.getBoolean("showDateTime", true)
        set(value) = App.editor.putBoolean("showDateTime", value).apply()

    var showCopyright: Boolean
        get() = App.sp.getBoolean("showCopyright", true)
        set(value) = App.editor.putBoolean("showCopyright", value).apply()

    var randomization: String
        get() = App.sp.getString("randomization", "randomized")!!
        set(value) = App.editor.putString("randomization", value).apply()

    var alterBrightness: String
        get() = App.sp.getString("alterBrightness", "brighten")!!
        set(value) = App.editor.putString("alterBrightness", value).apply()

    var watermarkType:String
        get() = App.sp.getString("watermarkType", "normal")!!
        set(value) = App.editor.putString("watermarkType", value).apply()
}