package com.emresarac.codechallanges.challanges

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class Planet(
    val position: Offset,
    val velocity: Offset,
    val orbit: List<Offset> = listOf(),
    val mass: Float = 100f,
    val color: Color
)

@Composable
fun SolarSystemGravitySimulation(){
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        SolarSystemGravityProcess()
    }
}

@Composable
fun SolarSystemGravityProcess() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }
        val sunPosition = Offset(x = screenWidthPx / 2f, y = screenHeightPx / 2f)
        val sunRadius = 60f

        var planets by remember { mutableStateOf(listOf<Planet>()) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        // If the clicked location is outside the sun
                        if ((tapOffset - sunPosition).getDistance() > sunRadius) {

                            val randomColor = Color(
                                red = Random.nextFloat(),
                                green = Random.nextFloat(),
                                blue = Random.nextFloat(),
                                alpha = 1f
                            )
                            // We specify a random angle and create the initial velocity vector
                            val angle = Random.nextFloat() * 2f * PI.toFloat()
                            val initialSpeed = 200f  // piksellik ilk hız
                            val velocity = Offset(
                                x = cos(angle) * initialSpeed,
                                y = sin(angle) * initialSpeed
                            )
                            // When creating a new planet, the starting position is added to the orbit list
                            planets = planets + Planet(
                                position = tapOffset,
                                velocity = velocity,
                                orbit = listOf(tapOffset),
                                mass = 100f,  // gezegen kütlesi
                                color = randomColor
                            )
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw the sun
                drawCircle(
                    color = Color.Yellow,
                    radius = sunRadius,
                    center = sunPosition
                )
                // Draw the planets and their orbits
                planets.forEach { planet ->
                    // Orbit line: we connect the points that the planet follows
                    if (planet.orbit.size > 1) {
                        drawPoints(
                            points = planet.orbit,
                            pointMode = PointMode.Polygon,
                            color = planet.color.copy(alpha = 0.5f),
                            strokeWidth = 2f
                        )
                    }
                    // Draw the planet
                    drawCircle(
                        color = planet.color,
                        radius = 20f,
                        center = planet.position
                    )
                }
            }

            LaunchedEffect(Unit) {
                val dt = 0.016f  // time step in seconds (~60 FPS)
                val gravitationalConstant = 5000f
                val sunMass = 10000f
                while (true) {
                    planets = planets.map { planet ->
                        var acceleration = Offset.Zero
                            val epsilon = 10f  // to prevent excessive force increase at very close distances

                        // Sun's gravity
                        val dxSun = sunPosition.x - planet.position.x
                        val dySun = sunPosition.y - planet.position.y
                        val distanceSun = sqrt(dxSun * dxSun + dySun * dySun)
                        if (distanceSun != 0f) {
                            val accelerationSunMagnitude = gravitationalConstant * sunMass / ((distanceSun * distanceSun) + epsilon)
                            acceleration += Offset(dxSun / distanceSun, dySun / distanceSun) * accelerationSunMagnitude
                        }

                        // Gravity of other planets
                        planets.filter { it !== planet }.forEach { other ->
                            val dx = other.position.x - planet.position.x
                            val dy = other.position.y - planet.position.y
                            val distance = sqrt(dx * dx + dy * dy)
                            if (distance != 0f) {
                                val accelerationPlanetMagnitude = gravitationalConstant * other.mass / ((distance * distance) + epsilon)
                                acceleration += Offset(dx / distance, dy / distance) * accelerationPlanetMagnitude
                            }
                        }

                        // Speed and position update
                        val newVelocity = planet.velocity + acceleration * dt
                        val newPosition = planet.position + newVelocity * dt
                        planet.copy(
                            position = newPosition,
                            velocity = newVelocity,
                            orbit = planet.orbit + newPosition
                        )
                    }
                    delay(32L)
                }
            }
        }
    }
}