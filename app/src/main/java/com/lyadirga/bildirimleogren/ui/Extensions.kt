package com.lyadirga.bildirimleogren.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lyadirga.bildirimleogren.R

fun Context.showToast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}


fun Context.showAlert(@StringRes message: Int) {
    MaterialAlertDialogBuilder(this, R.style.Theme_BildirimleOgren_MaterialAlertDialog).apply {
        setTitle(R.string.generic_warning)
        setMessage(message)
        setPositiveButton(R.string.generic_ok) { _, _ -> }
        show()
    }
}


fun Context.isInternetAvailable(): Boolean {
    val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}


fun View.clickWithDebounce(debounceTime: Long = 500L, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            else action()

            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}

fun View.setVisible() {
    this.visibility = View.VISIBLE
}

fun View.setGone() {
    this.visibility = View.GONE
}

fun View.setInVisible() {
    this.visibility = View.INVISIBLE
}

fun View.fadeVisibility(visibility: Int, duration: Long = 100) {

    val isCurrentlyVisible = this.isVisible
    val willBeVisible = visibility == View.VISIBLE

    if (isCurrentlyVisible == willBeVisible) {
        // 🇹🇷Türkçe: Aynı grup: VISIBLE ↔ VISIBLE veya INVISIBLE/GONE ↔ INVISIBLE/GONE → animasyon yapma
        // 🇬🇧English: Same group: VISIBLE ↔ VISIBLE or INVISIBLE/GONE ↔ INVISIBLE/GONE → do not animate
        return
    }


    // Start animation
    val fadeIn = ObjectAnimator.ofFloat(this, View.ALPHA, if (visibility == View.VISIBLE) 0f else 1f, if (visibility == View.VISIBLE) 1f else 0f)
    fadeIn.duration = duration
    fadeIn.addListener(object : android.animation.AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator) {
            super.onAnimationStart(animation)
            if (visibility == View.VISIBLE) {
                // 🇹🇷Türkçe: Görünürlük ayarla ve görünür yap
                // 🇬🇧English: Set visibility and make it visible
                this@fadeVisibility.visibility = visibility
            }
        }

        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            if (visibility != View.VISIBLE) {
                // 🇹🇷Türkçe: Görünürlük ayarla ve gizle
                // 🇬🇧English: Set visibility and hide
                this@fadeVisibility.visibility = visibility
            }
        }
    })
    fadeIn.start()
}

