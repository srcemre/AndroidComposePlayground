package com.emresarac.codechallanges

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.emresarac.codechallanges.challanges.MengerSpongeFractalOpenGL

import com.emresarac.codechallanges.challanges.StarfieldSimulation
import com.emresarac.codechallanges.ui.theme.CodeChallengesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodeChallengesTheme {
                //StarfieldSimulation()
                MengerSpongeFractalOpenGL()
            }
        }
    }
}
