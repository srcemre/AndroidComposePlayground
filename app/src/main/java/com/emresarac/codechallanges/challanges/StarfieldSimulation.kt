package com.emresarac.codechallanges.challanges

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

@Composable
fun StarfieldSimulation() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        StarfieldProcess()
    }
}

data class Star(
    var x: Float,
    var y: Float,
    var z: Float
) {
    companion object {
        fun createStar(width: Float, height: Float, z: Float? = null): Star {
            val centerX = width / 2f
            val centerY = height / 2f
            return Star(
                x = (-centerX.toInt()..centerX.toInt()).random().toFloat(),
                y = (-centerY.toInt()..centerY.toInt()).random().toFloat(),
                z = z ?: (0..centerX.toInt()).random().toFloat()
            )
        }
    }
}

@Composable
fun StarfieldProcess() {

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val density = LocalDensity.current
        val screenWidth = with(density) { maxWidth.toPx() }
        val screenHeight = with(density) { maxHeight.toPx() }
        val starCount = 400
        val projectionDistance = screenWidth/3

        val stars = remember {
            mutableStateOf(
                List(starCount) { Star.createStar(screenWidth, screenHeight) }
            )
        }

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            stars.value.forEach { star ->

                val newX = centerX + (star.x * projectionDistance) / star.z
                val newY = centerY + (star.y * projectionDistance) / star.z
                val radius = star.z.mapRange(0f, centerX, 10f, 0f)

                drawCircle(
                    color = Color.White,
                    radius = radius,
                    center = Offset(newX, newY)
                )
            }
        }

        LaunchedEffect(Unit) {
            while (true) {
                stars.value = stars.value.map { star ->

                    if (star.z <= 1f){
                        Star.createStar(screenWidth,screenHeight,screenWidth/2)
                    }else{
                        star.copy(z = star.z - 10f)
                    }

                }
                delay(16L)
            }
        }

    }

}

fun Float.mapRange(fromMin: Float, fromMax: Float, toMin: Float, toMax: Float): Float {
    return toMin + (this - fromMin) * (toMax - toMin) / (fromMax - fromMin)
}

@Preview
@Composable
fun StarfieldPreview() {
    StarfieldSimulation()
}