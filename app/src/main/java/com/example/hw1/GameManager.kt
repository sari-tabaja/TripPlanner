package com.example.hw1

import android.widget.ImageView

class GameManager(
    private val heart1: ImageView,
    private val heart2: ImageView,
    private val heart3: ImageView,
    private val carManager: CarManager
) {
    private var lives = 3

    fun checkCollision(obstacle: ImageView): Boolean {
        val carPosition = carManager.getCar().x
        val obstaclePosition = obstacle.x

        if (Math.abs(carPosition - obstaclePosition) < carManager.getCar().width) {
            lives--
            updateHearts()
            return true
        }
        return false
    }

    fun getLives(): Int {
        return lives
    }

    private fun updateHearts() {
        when (lives) {
            3 -> {
                heart1.visibility = ImageView.VISIBLE
                heart2.visibility = ImageView.VISIBLE
                heart3.visibility = ImageView.VISIBLE
            }
            2 -> {
                heart1.visibility = ImageView.VISIBLE
                heart2.visibility = ImageView.VISIBLE
                heart3.visibility = ImageView.INVISIBLE
            }
            1 -> {
                heart1.visibility = ImageView.VISIBLE
                heart2.visibility = ImageView.INVISIBLE
                heart3.visibility = ImageView.INVISIBLE
            }
            0 -> {
                heart1.visibility = ImageView.INVISIBLE
                heart2.visibility = ImageView.INVISIBLE
                heart3.visibility = ImageView.INVISIBLE
            }
        }
    }

    fun resetGame() {
        lives = 3
        carManager.resetCarPosition()
        updateHearts()
    }
}
