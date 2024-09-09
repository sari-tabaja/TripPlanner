package com.example.hw1

import android.widget.ImageView

class CarManager(private val car: ImageView) {
    private var currentLane = 1 // Start in the middle lane

    fun moveCarToLane(lane: Int) {
        val screenWidth = car.resources.displayMetrics.widthPixels
        val laneWidth = screenWidth / 3f // Divide screen into 3 lanes
        val carWidth = car.width

        val position = when (lane) {
            0 -> laneWidth / 2f - carWidth / 2f  // left lane
            1 -> laneWidth * 1.5f - carWidth / 2f // center lane
            2 -> laneWidth * 2.5f - carWidth / 2f // right lane
            else -> laneWidth * 1.5f - carWidth / 2f // default to center lane if invalid
        }

        car.animate().x(position).setDuration(300).start() // Animate car movement
        currentLane = lane
    }

    fun getCurrentLane(): Int {
        return currentLane
    }

    fun resetCarPosition() {
        currentLane = 1
        moveCarToLane(currentLane)
    }

    fun getCar(): ImageView {
        return car
    }
}
