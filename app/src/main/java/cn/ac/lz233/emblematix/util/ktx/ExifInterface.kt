package cn.ac.lz233.emblematix.util.ktx

import androidx.exifinterface.media.ExifInterface
import cn.ac.lz233.emblematix.logic.dao.ConfigDao
import java.math.BigDecimal
import kotlin.math.pow

fun ExifInterface.getDevice() = "${getAttribute(ExifInterface.TAG_MAKE) ?: ""} ${getAttribute(ExifInterface.TAG_MODEL) ?: ""}"

fun ExifInterface.getFNumber() = getAttribute(ExifInterface.TAG_F_NUMBER)

fun ExifInterface.getShutterSpeed() = getAttribute(ExifInterface.TAG_EXPOSURE_TIME)?.let {
    if (it.toDouble() >= 1) it else "1/${(1 / it.toDouble()).toInt()}"
}

fun ExifInterface.getFocalLength() = getAttribute(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM) ?: getAttribute(ExifInterface.TAG_FOCAL_LENGTH)?.let {
    val parts = it.split('/')
    if (parts.size == 2) {
        (parts[0].toFloat() / parts[1].toFloat()).toString().run {
            substring(0, this.indexOf('.') + 3)
        }
    } else {
        null
    }
}

fun ExifInterface.getISO() = getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)

fun ExifInterface.getPhotoInfo() = StringBuilder().apply {
    if (getFNumber() != null) append("f/${getFNumber()}")
    if (getShutterSpeed() != null) append(" • ${getShutterSpeed()}s")
    if (getFocalLength() != null) append(" • ${getFocalLength()}mm")
    if (getISO() != null) append(" • ISO${getISO()}")
}.toString()

fun ExifInterface.getDate() = getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)

fun ExifInterface.getCopyRight() = StringBuilder().apply {
    if (getDate() != null) append("${getDate()}  ")
    if (ConfigDao.copyright != "") {
        append("Image © ${ConfigDao.copyright}. ")
    } else if (getAttribute(ExifInterface.TAG_COPYRIGHT) != null) {
        append("Image © ${getAttribute(ExifInterface.TAG_COPYRIGHT)}. ")
    }
    append("All rights reserved.")
}.toString()