package com.kixfobby.security.badge.listener

/**
 * Interface definition for a callback to be invoked when the count on a badge changed.
 * @author Ivan V on 21.02.2018.
 * @version 1.0
 */
interface OnBadgeCountChangeListener {
    fun onCountChange(count: String?)
}