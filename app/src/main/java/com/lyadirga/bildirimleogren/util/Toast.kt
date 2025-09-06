package com.lyadirga.bildirimleogren.util

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.lyadirga.bildirimleogren.R

enum class ToastType {
    ALERT,
    SUCCESS
}

enum class ToastDuration(val duration: Int) {
    SHORT(2000),
    LONG(3000)
}

class Toast private constructor(
    private val context: Context,
    type: ToastType,
    private val message: String? = null,
    @StringRes private val messageResId: Int? = null,
    private val durationType: ToastDuration
)  {

    class Builder(private val context: Context) {

        private var type = ToastType.ALERT
        private var message: String? = null
        private var messageResId: Int? = null
        private var duration: ToastDuration = ToastDuration.SHORT

        fun setType(t: ToastType): Builder {
            this.type = t
            return this
        }

        fun setMessage(resId: Int? = null, message: String? = null): Builder {
            resId?.let {
                this.messageResId = it
            } ?: run {
                this.message = message
            }
            return this
        }

        fun setDuration(duration: ToastDuration): Builder {
            this.duration = duration
            return this
        }

        fun build(): Toast {
            val toast = Toast(context, type, message, messageResId, duration)
            return toast
        }
    }

    companion object {
        private fun builder(context: Context): Builder {
            return Builder(context)
        }

        fun showSuccessToast(context: Context, @StringRes messageResId: Int, duration: ToastDuration = ToastDuration.SHORT){
            builder(context).apply {
                setMessage(messageResId)
                setType(ToastType.SUCCESS)
                setDuration(duration)
            }.build().show()
        }

        fun showSuccessToast(context: Context, message: String, duration: ToastDuration = ToastDuration.SHORT){
                builder(context).apply {
                    setMessage(message = message)
                    setType(ToastType.SUCCESS)
                    setDuration(duration)
                }.build().show()
        }

        fun showAlertToast(context: Context, @StringRes messageResId: Int, duration: ToastDuration = ToastDuration.SHORT){
                builder(context).apply {
                    setMessage(messageResId)
                    setType(ToastType.ALERT)
                    setDuration(duration)
                }.build().show()
        }

        fun showAlertToast(context: Context, message: String, duration: ToastDuration = ToastDuration.SHORT){
                builder(context).apply {
                    setMessage(message = message)
                    setType(ToastType.ALERT)
                    setDuration(duration)
                }.build().show()
        }
    }

    private var fab: ExtendedFloatingActionButton
    private var rootView: FrameLayout
    private var marginTop: Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 30.toFloat(), context.resources.displayMetrics
    ).toInt()


    init {

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.topMargin = marginTop

        rootView = FrameLayout(context)
        val inflater = LayoutInflater.from(context)

        if (type == ToastType.ALERT) {
            fab = inflater.inflate(R.layout.fab_toast_error, rootView, false) as ExtendedFloatingActionButton
        } else {
            fab = inflater.inflate(R.layout.fab_toast_succes, rootView, false) as ExtendedFloatingActionButton
        }

        messageResId?.let {
            fab.setText(it)
        } ?: run {
            fab.text = message
        }

        rootView.addView(fab, params)

    } // end init

    private fun Context.getThemeColor(attrRes: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }

    fun show() {
        fab.shrink()
        (context as Activity).window.addContentView(
            rootView, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val enterAnimation = ObjectAnimator.ofFloat(rootView, View.TRANSLATION_Y, -(marginTop*2.9).toFloat(), 0f)
        val exitAnimation = ObjectAnimator.ofFloat(rootView, View.TRANSLATION_Y, 0f, -(marginTop*2.9).toFloat())

        exitAnimation.apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object: Animator.AnimatorListener{
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                   fab.visibility = View.GONE
                   rootView.visibility = View.GONE
                }
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }

        enterAnimation.apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : Animator.AnimatorListener{
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    fab.extend()
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        fab.shrink()
                        handler.postDelayed({
                            exitAnimation.start()
                        },300)
                    }, durationType.duration.toLong())
                }
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            } )
        }.start()
    }

}