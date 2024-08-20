package com.example.testapp.ui.bible
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import androidx.viewpager2.widget.ViewPager2

class CustomTouchListener(private val viewPager: ViewPager2) : View.OnTouchListener {
    private var initialX = 0f
    private var initialY = 0f
    private var isVerticalSwipe = false

    // Base thresholds for the left and right swipe
    private val baseLeftSwipeThreshold = 9999999
    private val baseRightSwipeThreshold = 9999999

    // Scaling factor to increase difficulty with vertical swipe
    private val rightScaleFactor = 1.0f
    private val leftScaleFactor = 3.5f

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = event.x
                initialY = event.y
                isVerticalSwipe = false
                // Allow the event to be handled by the ViewPager
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - initialX
                val dy = event.y - initialY
                val verticalSwipeDistance = abs(dy)

                // Calculate dynamic thresholds based on vertical swipe distance
                val dynamicLeftSwipeThreshold = baseLeftSwipeThreshold + (verticalSwipeDistance * leftScaleFactor)
                val dynamicRightSwipeThreshold = baseRightSwipeThreshold + (verticalSwipeDistance * rightScaleFactor)
                if (!isVerticalSwipe) {
                    if (abs(dy) > abs(dx)) {
                        isVerticalSwipe = true
                        Log.d("CustomTouchListener", "Vertical swipe detected: dx = $dx, dy = $dy")
                    } else {
                        // Check for left swipe with dynamic threshold
                        if (dx > dynamicLeftSwipeThreshold) {
                            Log.d("CustomTouchListener", "Left swipe detected with dx = $dx, dy = $dy,  threshold = $dynamicLeftSwipeThreshold")
                            // Allow left swipe only if it exceeds the dynamic threshold
                            return false
                        }
                        // Check for right swipe with dynamic threshold
                        if (dx > dynamicRightSwipeThreshold) {
                            Log.d("CustomTouchListener", "Right swipe detected with dx = $dx, dy = $dy, threshold = $dynamicRightSwipeThreshold")
                            // Allow right swipe only if it exceeds the dynamic threshold
                            return false
                        }
                        // Consume the event if the swipe does not exceed the dynamic threshold
                        return true
                    }
                }
                // If it is a vertical swipe, consume the event to prevent horizontal swipe
                return isVerticalSwipe
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Reset the state on touch up or cancel
                initialX = 0f
                initialY = 0f
                isVerticalSwipe = false
                // Allow the ViewPager to handle the event
                return false
            }
        }
        // Allow the ViewPager to handle the event
        return false
    }
}