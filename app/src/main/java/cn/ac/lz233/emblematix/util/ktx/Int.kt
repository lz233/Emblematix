package cn.ac.lz233.emblematix.util.ktx

import cn.ac.lz233.emblematix.App

fun Int.getString() = App.context.getString(this)