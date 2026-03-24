package com.smartexpense.ai.util

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.smartexpense.ai.R

class BindingAdapterUtils {
    companion object {

        /**
         *
         * @param view [MKImageView]
         * @param url [String] image URL to load
         * @param showRetry [Boolean] weather to show retry or not
         * @param alsoGoneView Also gone view if imageUrl is null
         * @return [Unit]
         */
//            @JvmStatic
//            @BindingAdapter(value = ["bindingImageUrl", "bindingShowRetry", "bindingAlsoGoneView"],
//                requireAll = false)
//            fun loadImage(view: MKImageView, url: String?, showRetry: Boolean? = false,
//                          alsoGoneView: Boolean? = true) {
//                if ((alsoGoneView == true || alsoGoneView == null) && url.isNullOrBlank()) {
//                    view.gone()
//                    return
//                } else {
//                    view.visible()
//                }
//                view.setURLAndStartLoading(url, showRetry ?: false)
//            }

        /**
         *
         * @param view any view that support text and color property [androidx.appcompat.widget.AppCompatTextView] [androidx.appcompat.widget.AppCompatButton]
         * @param text textValue
         * @param colorCode colorCode
         * @param alsoGoneView also gone view if both params [text] and [defaultText] are null and blank
         * @param defaultText default value of text if text is null and blank
         */
        @JvmStatic
        @BindingAdapter(value = ["bindingText", "bindingColor", "bindingAlsoGoneView",
            "bindingDefaultText"], requireAll = false)
        fun setUpTextView(view: View, text: String?, colorCode: String?,
                          alsoGoneView: Boolean? = true, defaultText: String? = null) {
            if ((alsoGoneView == true || alsoGoneView == null) && text.isNullOrBlank()
                && defaultText.isNullOrBlank()) {
                view.gone()
                return
            } else {
                view.visible()
            }
            if (view is TextView) {
                view.text = text ?: defaultText
                colorCode?.let {
                    view.setTextColor(it.parseColor())
                }
            }
        }

        /**
         * This function is used to setting up the gradient drawable
         *
         * @param view
         * @param startColor
         * @param endColor
         * @param radius
         */
        @JvmStatic
        @BindingAdapter(value = ["bindingStartColor", "bindingEndColor", "bindingRadius",
            "bindingTopLeftRadius", "bindingTopRightRadius", "bindingBottomLeftRadius",
            "bindingBottomRightRadius", "bindingStrokeColor", "bindingStrokeWidth", "bindingShape",
            "bindingOrientation", "bindingCenterColor", "bindingStrokeColors", "bindingGradientColors"], requireAll = false)
        fun setBackground(view: View, startColor: Int? = null, endColor: Int? = null,
                          radius: Float = 0f, topLeftRadius : Float = 0f, topRightRadius : Float = 0f,
                          bottomLeftRadius : Float = 0f, bottomRightRadius : Float = 0f,
                          strokeColor: Int? = null, strokeWidth: Float? = null,
                          shape: Int? = null, orientation: GradientDrawable.Orientation? = null, centerColor: Int? = null, strokeColors: List<Int>? = null, gradientColors: List<Int>? = null) {

            view.let {
                val drawable = GradientDrawable()
                if (topLeftRadius > 0 || topRightRadius > 0 || bottomLeftRadius > 0 || bottomRightRadius > 0)
                    drawable.cornerRadii = floatArrayOf(topLeftRadius, topLeftRadius, topRightRadius,
                        topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius)
                if (radius > 0)
                    drawable.cornerRadius = radius
                drawable.orientation = orientation ?: GradientDrawable.Orientation.LEFT_RIGHT
                drawable.shape = shape ?: GradientDrawable.RECTANGLE
                if (startColor != null || centerColor != null || endColor != null) {
                    val colorsList = ArrayList<Int>()
                    startColor?.let {
                        colorsList.add(it)
                    }
                    centerColor?.let {
                        colorsList.add(it)
                    }
                    endColor?.let {
                        colorsList.add(it)
                    }
                    if (colorsList.size == 1) {
                        drawable.setColor(colorsList.first())
                    } else {
                        drawable.colors = colorsList.toIntArray()
                    }
                } else if (gradientColors != null) {
                    drawable.colors = gradientColors.toIntArray()
                }

                if (!strokeColors.isNullOrEmpty()) {
                    val strokeWidthInt = strokeWidth?.toInt() ?: view.context.resources.getDimension(
                        R.dimen.size1).toInt()
                    val drawableStroke = GradientDrawable().apply {
                        if (topLeftRadius > 0 || topRightRadius > 0 || bottomLeftRadius > 0 || bottomRightRadius > 0) {
                            this.cornerRadii = floatArrayOf(topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius)
                        }
                        if (radius > 0) this.cornerRadius = radius
                        this.orientation = orientation ?: GradientDrawable.Orientation.LEFT_RIGHT
                        this.shape = shape ?: GradientDrawable.RECTANGLE
                        this.colors = strokeColors.toIntArray()
                    }
                    val finalDrawable = LayerDrawable(arrayOf<Drawable>(drawableStroke, drawable))
                    finalDrawable.setLayerInset(0, 0, 0, 0, 0)
                    finalDrawable.setLayerInset(1, strokeWidthInt, strokeWidthInt, strokeWidthInt, strokeWidthInt)
                    it.background = finalDrawable
                } else {
                    if (strokeColor != null) {
                        if (strokeWidth == null) {
                            drawable.setStroke(view.context.resources.getDimension(R.dimen.size1).toInt(), strokeColor)
                        } else {
                            drawable.setStroke(strokeWidth.toInt(), strokeColor)
                        }
                    }
                    it.background = drawable
                }
            }
        }
        @JvmStatic
        @BindingAdapter(
            value = ["bindingEnableHapticFeedback", "bindingHapticFeedbackScaleDownTo"],
            requireAll = false
        )
        fun setHapticFeedBackAnimation(view: View, enableHapticFeedback: Boolean = true, scaleDownTo: Float = 0.94f) {
            if (enableHapticFeedback) {
                TapScalingViewHandler().initialize(context = view.context, view = view, scaleDownTo = if (scaleDownTo <= 0) 0.94f else scaleDownTo)
            } else {
                view.setOnTouchListener { _: View, event: MotionEvent? ->
                    true
                }
            }
        }
    }
}