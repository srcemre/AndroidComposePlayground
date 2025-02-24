@file:Suppress("UNUSED_EXPRESSION")

package com.emresarac.codechallanges.challanges

import android.annotation.SuppressLint
import android.opengl.GLES20
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glDrawElements
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform3fv
import android.opengl.GLES20.glUniform4fv
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs

@Composable
fun MengerSpongeFractalOpenGL() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        AndroidView(factory = { context ->
            object : GLSurfaceView(context) {

                private val renderer = MyGLRenderer()

                init {
                    setEGLContextClientVersion(2)
                    setRenderer(renderer)
                    renderMode = RENDERMODE_CONTINUOUSLY
                }

                @SuppressLint("ClickableViewAccessibility")
                override fun onTouchEvent(event: MotionEvent): Boolean {
                    if (event.action == MotionEvent.ACTION_UP) {
                        queueEvent {
                            renderer.regenerateCube() // Call regenerateCube on touch
                        }
                    }
                    return true
                }
            }
        })
    }
}

class MyGLRenderer : GLSurfaceView.Renderer {


    data class CubeData(
        var scale: Float,
        var x: Float,
        var y: Float,
        var z: Float
    )
    private val cubeList = mutableListOf<CubeData>()

    private lateinit var cube: Cube
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    private var rotationAngle: Float = 0f
    private val lightDirection = floatArrayOf(0.5f, 0.5f, -1.0f)

    fun regenerateCube() {
        /**
        * Menger Sponge Fractal
        * */
        val nextCubeList = mutableListOf<CubeData>()

        for (currentCube in cubeList) {

            for (x in -1..1 step 1) {
                for (y in -1..1 step 1) {
                    for (z in -1..1 step 1) {

                        val sum = abs(x) + abs(y) + abs(z)
                        if (sum > 1) {
                            val newScale = currentCube.scale / 3f
                            val offsetX = currentCube.x + x * (newScale * 2)
                            val offsetY = currentCube.y + y * (newScale * 2)
                            val offsetZ = currentCube.z + z * (newScale * 2)

                            nextCubeList.add(CubeData(newScale, offsetX, offsetY, offsetZ))
                        }
                    }
                }
            }

        }
        cubeList.clear()
        cubeList.addAll(nextCubeList)
    }

    override fun onSurfaceCreated(unused: GL10?, p1: javax.microedition.khronos.egl.EGLConfig?) {
        GLES20.glClearColor(0.8f, 0.8f, 0.8f, 1.0f) // Set background color to light gray
        GLES20.glEnable(GLES20.GL_DEPTH_TEST) // Enable depth testing for proper occlusion
        GLES20.glDisable(GLES20.GL_BLEND)
        cube = Cube()

        cubeList.add(CubeData(0.35f, 0f, 0f, 0f))
    }

    override fun onDrawFrame(unused: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT) // Clear color and depth buffers

        // Set camera view: position, target, up vector
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, -3f,
            0f, 0f, 0f,
            0f, 1.0f, 0f
        )

        //Drawing Cube Objects
        rotationAngle += 0.5f
        if (rotationAngle >= 360) rotationAngle = 0f

        for (currentCube in cubeList) {

            val cubeMatrix = FloatArray(16)
            Matrix.setIdentityM(cubeMatrix, 0)

            Matrix.rotateM(cubeMatrix, 0, rotationAngle, -1.0f, -1.0f, 0f)
            Matrix.translateM(cubeMatrix, 0, currentCube.x, currentCube.y, currentCube.z)
            Matrix.scaleM(cubeMatrix, 0, currentCube.scale, currentCube.scale, currentCube.scale)

            val mvpMatrix = FloatArray(16)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0) // View * Model
            Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, cubeMatrix, 0)  // Projection * (View * Model)

            cube.draw(
                mvpMatrix = mvpMatrix,
                color = floatArrayOf(0f, 1f, 0f, 1f),
                lightDirection = lightDirection
            )
        }
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height
        Matrix.perspectiveM(projectionMatrix, 0, 45f, ratio, 0.1f, 100f) // Set projection matrix (perspective)
    }

}


class Cube {

    private val vertexShaderCode =
        "uniform mat4 uMVPMatrix;\n" +
                "attribute vec4 vPosition;\n" +
                "varying vec4 position;\n" +
                "void main() {\n" +
                "  gl_Position = uMVPMatrix * vPosition;\n" +
                "  position = vPosition;\n" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;\n" +
                "uniform vec4 uColor;\n" +
                "uniform vec3 uLightDirection;\n" +
                "varying vec4 position;\n" +
                "void main() {\n" +
                "  vec3 normal;\n" +
                "  if (position.x == -1.0) normal = vec3(-1.0, 0.0, 0.0);\n" +
                "  else if (position.x == 1.0) normal = vec3(1.0, 0.0, 0.0);\n" +
                "  else if (position.y == -1.0) normal = vec3(0.0, -1.0, 0.0);\n" +
                "  else if (position.y == 1.0) normal = vec3(0.0, 1.0, 0.0);\n" +
                "  else if (position.z == -1.0) normal = vec3(0.0, 0.0, -1.0);\n" +
                "  else normal = vec3(0.0, 0.0, 1.0);\n" +

                "  float diffuseIntensity = max(dot(normal, normalize(uLightDirection)), 0.2);\n" +
                "  gl_FragColor = uColor * diffuseIntensity ;\n" +
                "}"

    private var program: Int
    private val cubeVertices = floatArrayOf(
        -1f, -1f, -1f,
        1f, -1f, -1f,
        1f, 1f, -1f,
        -1f, 1f, -1f,
        -1f, -1f, 1f,
        1f, -1f, 1f,
        1f, 1f, 1f,
        -1f, 1f, 1f
    )

    private val drawOrder = shortArrayOf(
        0, 1, 2, 0, 2, 3,
        1, 5, 6, 1, 6, 2,
        5, 4, 7, 5, 7, 6,
        4, 0, 3, 4, 3, 7,
        3, 2, 6, 3, 6, 7,
        4, 5, 1, 4, 1, 0
    )

    private val vertexBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(cubeVertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(cubeVertices)
                position(0)
            }
        }

    private val drawListBuffer: ShortBuffer =
        ByteBuffer.allocateDirect(drawOrder.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }

    private var positionHandle: Int = 0
    private var mvpMatrixHandle: Int = 0
    private var colorHandle: Int = 0
    private var lightDirectionHandle: Int = 0

    private val vertexStride: Int = COORDS_PER_VERTEX * 4

    init {
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    fun draw(mvpMatrix: FloatArray, color: FloatArray, lightDirection: FloatArray) {
        GLES20.glUseProgram(program)

        positionHandle = glGetAttribLocation(program, "vPosition").also {
            glEnableVertexAttribArray(it)
            glVertexAttribPointer(
                it,
                COORDS_PER_VERTEX,
                GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )
        }

        mvpMatrixHandle = glGetUniformLocation(program, "uMVPMatrix").also { location ->
            glUniformMatrix4fv(location, 1, false, mvpMatrix, 0)
        }

        colorHandle = glGetUniformLocation(program, "uColor").also { location ->
            glUniform4fv(location, 1, color, 0)
        }

        lightDirectionHandle = glGetUniformLocation(program, "uLightDirection").also { location ->
            glUniform3fv(location, 1, lightDirection, 0)
        }

        glDrawElements(GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer)
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    companion object {
        const val COORDS_PER_VERTEX = 3

        fun loadShader(type: Int, shaderCode: String): Int {
            return GLES20.glCreateShader(type).also { shader ->
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)

                val compiled = IntArray(1)
                GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
                if (compiled[0] == 0) {
                    val error = GLES20.glGetShaderInfoLog(shader)
                    println("Shader compilation error: $error")
                    GLES20.glDeleteShader(shader)
                    0
                } else {
                    shader
                }
            }
        }
    }
}