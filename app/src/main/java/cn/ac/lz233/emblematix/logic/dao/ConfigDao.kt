package cn.ac.lz233.emblematix.logic.dao

import cn.ac.lz233.emblematix.App

object ConfigDao {
    var copyright: String
        get() = App.sp.getString("copyright", "")!!
        set(value) = App.editor.putString("copyright", value).apply()
}