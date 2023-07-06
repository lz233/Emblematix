package cn.ac.lz233.emblematix.util.ktx

import androidx.exifinterface.media.ExifInterface
import cn.ac.lz233.emblematix.logic.dao.ConfigDao
import java.math.BigDecimal
import kotlin.math.pow

fun ExifInterface.getManufacturer() = getAttribute(ExifInterface.TAG_MAKE)

fun ExifInterface.getModel() = getAttribute(ExifInterface.TAG_MODEL)
fun ExifInterface.getDevice() = StringBuilder().apply {
    if (ConfigDao.showManufacturer && getManufacturer() != null) append("${getManufacturer()} ")
    if (ConfigDao.showModel && getModel() != null) append("${getModel()}")
}.toString().trim()

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
    if (ConfigDao.showFNumber && getFNumber() != null) append("f/${getFNumber()} • ")
    if (ConfigDao.showShutterSpeed && getShutterSpeed() != null) append("${getShutterSpeed()}s • ")
    if (ConfigDao.showFocalLength && getFocalLength() != null) append("${getFocalLength()}mm • ")
    if (ConfigDao.showISO && getISO() != null) append("ISO${getISO()}")
    if (endsWith(" • ")) delete(length - 4, length - 1)
}.toString()

fun ExifInterface.getDate() = getAttribute(ExifInterface.TAG_DATETIME)

fun ExifInterface.getCopyRight() = StringBuilder().apply {
    val dateTime = getDate()
    if (ConfigDao.showDateTime && dateTime != null) append("${dateTime.substring(dateTime.indexOf(':') + 1).replaceFirst(':', '.')}  ")
    if (ConfigDao.showCopyright) {
        if (ConfigDao.copyright != "") {
            append("Image © ")
            if (dateTime != null) append("${dateTime.substring(0, dateTime.indexOf(':'))} ")
            append("${ConfigDao.copyright}. ")
        } else if (getAttribute(ExifInterface.TAG_COPYRIGHT) != null) {
            append("Image © ")
            if (dateTime != null) append("${dateTime.substring(0, dateTime.indexOf(':'))} ")
            append("${getAttribute(ExifInterface.TAG_COPYRIGHT)}. ")
        }
        append("All rights reserved.")
    }
}.toString().trim()