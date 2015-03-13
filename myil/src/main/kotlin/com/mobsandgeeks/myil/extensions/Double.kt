package com.mobsandgeeks.myil.extensions

/**
 * Extension function from
 * http://stackoverflow.com/questions/23086291/format-in-kotlin-string-templates
 */
fun Float.format(digits: Int) = java.lang.String.format("%.${digits}f", this)
