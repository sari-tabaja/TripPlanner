package com.example.hw1

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import androidx.appcompat.app.AppCompatActivity



class MainActivity : AppCompatActivity() {
    private lateinit var carManager: CarManager
    private lateinit var gameManager: GameManager
    private lateinit var obstacleManager: ObstacleManager
    private lateinit var vibrator: Vibrator // Vibrator service

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val car = findViewById<ImageView>(R.id.car)
        val btnLeft = findViewById<Button>(R.id.btnLeft)
        val btnRight = findViewById<Button>(R.id.btnRight)
        val heart1 = findViewById<ImageView>(R.id.redheart1)
        val heart2 = findViewById<ImageView>(R.id.redheart2)
        val heart3 = findViewById<ImageView>(R.id.redheart3)
        val layout = findViewById<RelativeLayout>(R.id.activity_main)

        // Initialize vibrator service
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        carManager = CarManager(car)
        gameManager = GameManager(heart1, heart2, heart3, carManager)
        obstacleManager = ObstacleManager(layout, car, this::handleCollision)

        btnLeft.setOnClickListener {
            val currentLane = carManager.getCurrentLane()
            if (currentLane > 0) {
                carManager.moveCarToLane(currentLane - 1)
            }
        }

        btnRight.setOnClickListener {
            val currentLane = carManager.getCurrentLane()
            if (currentLane < 2) {
                carManager.moveCarToLane(currentLane + 1)
            }
        }

        obstacleManager.generateObstacles()
    }

    private fun handleCollision(obstacle: ImageView) {
        val isCollision = gameManager.checkCollision(obstacle)
        if (isCollision) {
            val lives = gameManager.getLives()
            Toast.makeText(this, "Hit! Lives left: $lives", Toast.LENGTH_SHORT).show()

            // Trigger vibration on collision
            vibrate()

            if (lives == 0) {
                Toast.makeText(this, "Game Over!", Toast.LENGTH_SHORT).show()
                vibrate() // Vibrate when the game is over
                resetGame()
            }
        }
    }

    private fun vibrate() {
        // Check if the vibrator has vibration capabilities
        if (vibrator.hasVibrator()) {
            // Vibrate for 500 milliseconds
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun resetGame() {
        gameManager.resetGame()
        obstacleManager.resetObstacleSpeed()
    }
}
