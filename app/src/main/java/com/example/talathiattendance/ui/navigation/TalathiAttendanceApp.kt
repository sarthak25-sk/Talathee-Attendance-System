
package com.example.talathiattendance.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.talathiattendance.ui.navigation.Screens.AdminDashboard
import com.example.talathiattendance.ui.navigation.Screens.Dashboard
import com.example.talathiattendance.ui.navigation.Screens.Login
import com.example.talathiattendance.ui.navigation.Screens.Registration
import com.example.talathiattendance.ui.navigation.Screens.Splash
import com.example.talathiattendance.ui.navigation.Screens.UserSelection
import com.example.talathiattendance.ui.screens.*

@Composable
fun TalathiAttendanceApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Splash) {
        composable(route = Splash) {
            SplashScreen(navController)
        }
        composable(route = UserSelection) {
            UserSelectionScreen(navController)
        }
        composable(route = Login) {
            var userType: String =
                navController.previousBackStackEntry?.savedStateHandle?.get<String>(
                    "userType"
                )?:"talathi"

            LoginScreen(navController = navController,userType = userType)
        }
        composable(route = Registration) {
            RegisterScreen(navController)
        }

        composable(route = Dashboard) {
            DashboardScreen(navController)
        }

        composable(route = AdminDashboard) {
            AdminDashboardScreen(navController)
        }

    }
}

object Screens {
    const val Splash = "Splash"
    const val UserSelection = "select_user_screen"
    const val Login = "Login"
    const val Registration = "Registration"
    const val Dashboard = "Dashboard"
    const val AdminDashboard = "AdminDashboard"

}