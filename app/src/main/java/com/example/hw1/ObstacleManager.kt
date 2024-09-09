package com.example.hw1

import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import kotlin.random.Random

class ObstacleManager(
    private val layout: RelativeLayout,
    private val car: ImageView,
    private val checkCollision: (ImageView) -> Unit
) {
    private var obstacleSpeed = 3000L // Initial obstacle fall speed (3 seconds)
    private var obstacleSpawnDelay = 3000L // Initial obstacle spawn delay (3 seconds)
    private val initialSpeed = 3000L
    private val initialSpawnDelay = 3000L

    fun generateObstacles() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val randomLane = Random.nextInt(0, 3)
                val obstacle = createObstacle(randomLane)

                obstacleSpawnDelay = maxOf(1000L, obstacleSpawnDelay - 100) // Reduce spawn delay
                obstacleSpeed = maxOf(1000L, obstacleSpeed - 100) // Increase fall speed

                handler.postDelayed(this, obstacleSpawnDelay)
            }
        }
        handler.post(runnable)
    }

    private fun createObstacle(lane: Int): ImageView {
        val obstacle = ImageView(layout.context).apply {
            setImageDrawable(ContextCompat.getDrawable(layout.context, R.drawable.rock_svgrepo_com))
            layoutParams = RelativeLayout.LayoutParams(100, 100)
        }

        val screenWidth = layout.resources.displayMetrics.widthPixels
        val laneWidth = screenWidth / 3f
        val obstacleWidth = 100f

        val position = when (lane) {
            0 -> laneWidth / 2f - obstacleWidth / 2f  // left lane
            1 -> laneWidth * 1.5f - obstacleWidth / 2f // center lane
            2 -> laneWidth * 2.5f - obstacleWidth / 2f // right lane
            else -> laneWidth * 1.5f - obstacleWidth / 2f
        }
        obstacle.x = position
        obstacle.y = -150f

        layout.addView(obstacle)

        val screenHeight = layout.resources.displayMetrics.heightPixels

        obstacle.animate()
            .translationYBy(screenHeight + 200f)
            .setDuration(obstacleSpeed)
            .withEndAction {
                checkCollision(obstacle)
                (obstacle.parent as? RelativeLayout)?.removeView(obstacle)
            }.start()

        return obstacle
    }

    fun resetObstacleSpeed() {
        obstacleSpeed = initialSpeed
        obstacleSpawnDelay = initialSpawnDelay
    }
}
