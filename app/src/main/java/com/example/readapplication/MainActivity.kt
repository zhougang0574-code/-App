package com.example.readapplication

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.readapplication.navigation.AppNavigation
import com.example.readapplication.ui.theme.KidsMathTheme
import com.example.readapplication.viewmodel.QuizViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            KidsMathTheme {
                val navController = rememberNavController()
                val viewModel: QuizViewModel = viewModel()
                AppNavigation(navController = navController, viewModel = viewModel)
            }
        }
    }
}
