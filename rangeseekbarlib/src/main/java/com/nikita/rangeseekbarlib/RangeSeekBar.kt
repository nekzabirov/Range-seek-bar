package com.nikita.rangeseekbarlib

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import kotlin.math.abs

/**
 * Seek bar for use two button on seek bar and making range
 * For example to create price filter
 */
class RangeSeekBar : View {

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    @ColorInt
    var btnColor: Int = Color.BLUE
     /*   set(@ColorRes value) {
        field = this.context.resources.getColor(value)
    }*/

    @ColorInt
    var lineColor: Int = Color.GRAY
    /*    set(@ColorRes value) {
            field = this.context.resources.getColor(value)
        }*/

    @ColorInt
    var selectedColor: Int = Color.BLUE
    /*    set(@ColorRes value) {
            field = this.context.resources.getColor(value)
        }*/

    @ColorInt
    var textColor: Int = Color.BLUE
    /*    set(@ColorRes value) {
            field = this.context.resources.getColor(value)
        }*/

    var stepMove: Float = 1f

    var minValue: Float = 1f

    var maxValue: Float = 8f

    private val txtValuePadding = 30f

    fun getStartValue(): Float {
        return getValueFromBtn(startBtn)
    }

    fun getEndValue(): Float {
        return getValueFromBtn(endBtn)
    }

    private val btnRadius = 40f
    private val swellRadius = 1.3f

    private val minDistance = btnRadius + 16.dp
    private val maxDistance: Float by lazy {
        width.toFloat() - btnRadius - 16.dp
    }
    private val stepsAsDistance: Float by lazy {
        stepMove * maxDistance / maxValue
    }

    private val stepsPosMap = LinkedHashMap<Float, Float>()

    private val startBtn: DrawableBtnCircus by lazy {
        DrawableBtnCircus(
            x = minDistance,
            y = btnRadius * swellRadius,
            radius = btnRadius,
            paint = Paint(Paint.ANTI_ALIAS_FLAG)
                .apply {
                    color = btnColor
                    setShadowLayer(btnRadius / 2, 0f, 15f, Color.GRAY)
                    isAntiAlias = true
                }
        )
    }
    private val endBtn: DrawableBtnCircus by lazy {
        DrawableBtnCircus(
            x = maxDistance,
            y = btnRadius * swellRadius,
            radius = btnRadius,
            paint = Paint(Paint.ANTI_ALIAS_FLAG)
                .apply {
                    color = btnColor
                    setShadowLayer(btnRadius / 2, 0f, 15f, Color.GRAY)
                    isAntiAlias = true
                }
        )
    }
    private var touchedBtn: DrawableBtnCircus? = null
    private var lastXCoordinate = 0f

    private val txtPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 14.dp.toFloat()
        //typeface = ResourcesCompat.getFont(context, R.font.helveticaneue_bold)
        isAntiAlias = true
    } }

    @SuppressLint("Recycle")
    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.RangeSeekBar, defStyle, 0
        )

        //Init colors
        btnColor = a.getColor(R.styleable.RangeSeekBar_btn_color, btnColor)
        lineColor = a.getColor(R.styleable.RangeSeekBar_line_color, lineColor)
        selectedColor = a.getColor(R.styleable.RangeSeekBar_selected_color, selectedColor)
        textColor = a.getColor(R.styleable.RangeSeekBar_text_color, textColor)

        //Init value
        minValue = a.getFloat(R.styleable.RangeSeekBar_min, minValue)
        maxValue = a.getFloat(R.styleable.RangeSeekBar_max, maxValue)
        stepMove = a.getFloat(R.styleable.RangeSeekBar_step, stepMove)

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        initStepsPositions()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec, ((btnRadius * swellRadius + 50) * 2).toInt())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { event ->

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchedBtn = getTouchedBtn(event.x)
                    touchedBtn?.radius = btnRadius * swellRadius
                }
                MotionEvent.ACTION_UP -> {
                    touchedBtn?.radius = btnRadius
                }
                MotionEvent.ACTION_MOVE -> {
                    if (touchedBtn != null) {
                        lastXCoordinate = touchedBtn!!.x
                        if (event.x < minDistance || event.x > maxDistance) {
                            touchedBtn!!.x = lastXCoordinate
                        }
                        else {
                            touchedBtn!!.x = event.x
                        }
                        if (abs(endBtn.x - startBtn.x) < stepsAsDistance + 30) {
                            touchedBtn!!.x = lastXCoordinate
                        }
                    }
                }
                else -> {
                    touchedBtn = null
                }
            }

            invalidate()
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawSeekBar(canvas)
        drawSeekBarSelected(canvas, startBtn.x, endBtn.x)

        drawStartBtn(canvas)
        drawEndBtn(canvas)
    }

    private fun findNearestCoordinate(xN: Float): Float {
        var delta = Float.MAX_VALUE
        var element = 0f
        for (mutableEntry in stepsPosMap) {
            if (abs(xN - mutableEntry.value) < delta) {
                delta = abs(xN - mutableEntry.value)
                element = mutableEntry.value
            }
        }
        return element
    }

    private fun getValueFromCoordinateX(coordinateX: Float): Float {
        var data = 0f
        for (mutableEntry in stepsPosMap) {
            if (coordinateX == mutableEntry.value) {
                data = mutableEntry.key
            }
        }
        return data
    }

    private fun getValueFromBtn(btn: DrawableBtnCircus): Float {
        return getValueFromCoordinateX(
            findNearestCoordinate(btn.x)
        )
    }

    private fun initStepsPositions() {
        stepsPosMap.clear()
        var stepPos = minDistance + stepsAsDistance
        var stepN = minValue + stepMove
        while (stepN < maxValue) {
            stepsPosMap[stepN] = stepPos
            stepPos += stepsAsDistance
            stepN += stepMove
        }
        stepsPosMap[minValue] = minDistance
        stepsPosMap[maxValue] = maxDistance
    }

    private fun getTouchedBtn(xTouch: Float): DrawableBtnCircus? {
        return if (xTouch >= (startBtn.x - startBtn.radius) &&
            xTouch <= (startBtn.x + startBtn.radius)) {
            startBtn
        }
        else if (xTouch >= (endBtn.x - endBtn.radius) &&
            xTouch <= (endBtn.x + endBtn.radius)) {
            endBtn
        }
        else {
            null
        }
    }

    private fun drawSeekBar(canvas: Canvas?) {
        canvas?.drawLine(
            minDistance,
            btnRadius * swellRadius,
            maxDistance,
            btnRadius * swellRadius,
            Paint().apply {
                color = lineColor
                strokeWidth = 4.dp.toFloat()
            }
        )
    }

    private fun drawSeekBarSelected(canvas: Canvas?, startX: Float, endX: Float) {
        canvas?.drawLine(
            startX,
            btnRadius * swellRadius,
            endX,
            btnRadius * swellRadius,
            Paint().apply {
                color = selectedColor
                strokeWidth = 4.dp.toFloat()
            }
        )
    }

    private fun drawStartBtn(canvas: Canvas?) {
        canvas?.drawCircle(startBtn.x, startBtn.y, startBtn.radius, startBtn.paint)

        canvas?.drawText(getValueFromBtn(startBtn).toString(),
            startBtn.x - txtValuePadding,
            startBtn.y + btnRadius + 80f,
            txtPaint
        )
    }

    private fun drawEndBtn(canvas: Canvas?) {
        canvas?.drawCircle(endBtn.x, endBtn.y, endBtn.radius, endBtn.paint)

        canvas?.drawText(getValueFromBtn(endBtn).toString(),
            endBtn.x - txtValuePadding,
            endBtn.y + btnRadius + 80f,
            txtPaint
        )
    }

}

data class DrawableBtnCircus(var x: Float,
                             var y: Float,
                             var radius: Float,
                             val paint: Paint)

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

val Float.dp: Float
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f)