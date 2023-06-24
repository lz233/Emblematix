package cn.ac.lz233.emblematix

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class App : Application() {
    companion object {
        lateinit var context: Context
        lateinit var sp: SharedPreferences
        lateinit var editor: SharedPreferences.Editor
        const val TAG = "Emblematix"
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        sp = PreferenceManager.getDefaultSharedPreferences(context)
        editor = sp.edit()
    }
}