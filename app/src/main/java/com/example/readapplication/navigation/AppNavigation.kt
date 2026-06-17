package com.example.readapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.readapplication.ui.screen.*
import com.example.readapplication.viewmodel.QuizViewModel

object Routes {
    const val HOME = "home"
    const val DIFFICULTY = "difficulty"
    const val QUIZ = "quiz"
    const val RESULT = "result"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: QuizViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                isInitializing = uiState.isInitializing,
                onStart = { navController.navigate(Routes.DIFFICULTY) }
            )
        }

        composable(Routes.DIFFICULTY) {
            DifficultyScreen(
                onSelect = { difficulty ->
                    viewModel.startQuiz(difficulty)
                    navController.navigate(Routes.QUIZ)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.QUIZ) {
            QuizScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onFinished = {
                    navController.navigate(Routes.RESULT) {
                        popUpTo(Routes.QUIZ) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.RESULT) {
            ResultScreen(
                correctCount = uiState.correctCount,
                totalCount = uiState.totalQuestions,
                answers = uiState.answers,
                onRestart = {
                    viewModel.resetSession()
                    navController.navigate(Routes.DIFFICULTY) {
                        popUpTo(Routes.HOME)
                    }
                },
                onChangeDifficulty = {
                    viewModel.resetSession()
                    navController.navigate(Routes.DIFFICULTY) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
    }
}
