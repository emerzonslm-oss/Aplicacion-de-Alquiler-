package com.example.alquigo.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.alquigo.ui.auth.AuthViewModel
import com.example.alquigo.ui.auth.LoginScreen
import com.example.alquigo.ui.auth.RegisterScreen
import com.example.alquigo.ui.auth.UiEvent
import com.example.alquigo.ui.components.GlassBottomBar
import com.example.alquigo.ui.home.HomeScreen
import com.example.alquigo.ui.profile.ProfileScreen
import com.example.alquigo.ui.property.AddPropertyScreen
import com.example.alquigo.ui.property.AssignTenantScreen
import com.example.alquigo.ui.property.EditPropertyScreen
import com.example.alquigo.ui.property.PropertyListScreen
import com.example.alquigo.ui.property.PropertyDetailScreen
import com.example.alquigo.ui.property.PropertyViewModel
import com.example.alquigo.ui.tenants.AddTenantScreen
import com.example.alquigo.ui.tenants.TenantsScreen
import dev.chrisbanes.haze.HazeState

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object AddProperty : Screen("add_property")
    object PropertyList : Screen("property_list")
    object EditProperty : Screen("edit_property/{propertyId}") {
        fun createRoute(propertyId: String) = "edit_property/$propertyId"
    }
    object PropertyDetail : Screen("property_detail/{propertyId}") {
        fun createRoute(propertyId: String) = "property_detail/$propertyId"
    }
    object AssignTenant : Screen("assign_tenant/{propertyId}") {
        fun createRoute(propertyId: String) = "assign_tenant/$propertyId"
    }
    object Tenants : Screen("tenants")
    object AddTenant : Screen("add_tenant")
    object Profile : Screen("profile")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: AuthViewModel
) {
    val propertyViewModel: PropertyViewModel = viewModel()
    val hazeState = remember { HazeState() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    val showBottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.PropertyList.route,
        Screen.Tenants.route,
        Screen.Profile.route
    )

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.NavigateToHome -> {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
                is UiEvent.NavigateToLogin -> {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }
        }
    }

    val startDestination = if (viewModel.isUserLoggedIn()) Screen.Home.route else Screen.Login.route

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    propertyViewModel = propertyViewModel,
                    onNavigateToAddProperty = { navController.navigate(Screen.AddProperty.route) },
                    onNavigateToPropertyList = { navController.navigate(Screen.PropertyList.route) },
                    onNavigateToPropertyDetail = { propertyId ->
                        navController.navigate(Screen.PropertyDetail.createRoute(propertyId))
                    },
                    hazeState = hazeState
                )
            }
            composable(Screen.AddProperty.route) {
                AddPropertyScreen(
                    viewModel = propertyViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.PropertyList.route) {
                PropertyListScreen(
                    viewModel = propertyViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditProperty = { propertyId ->
                        navController.navigate(Screen.EditProperty.createRoute(propertyId))
                    },
                    onNavigateToPropertyDetail = { propertyId ->
                        navController.navigate(Screen.PropertyDetail.createRoute(propertyId))
                    }
                )
            }
            composable(
                route = Screen.EditProperty.route,
                arguments = listOf(navArgument("propertyId") { type = NavType.StringType })
            ) { backStackEntry ->
                val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""
                EditPropertyScreen(
                    propertyId = propertyId,
                    viewModel = propertyViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.PropertyDetail.route,
                arguments = listOf(navArgument("propertyId") { type = NavType.StringType })
            ) { backStackEntry ->
                val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""
                PropertyDetailScreen(
                    propertyId = propertyId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAssignTenant = { propId ->
                        navController.navigate(Screen.AssignTenant.createRoute(propId))
                    }
                )
            }
            composable(
                route = Screen.AssignTenant.route,
                arguments = listOf(navArgument("propertyId") { type = NavType.StringType })
            ) { backStackEntry ->
                val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""
                AssignTenantScreen(
                    propertyId = propertyId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddTenant = { navController.navigate(Screen.AddTenant.route) }
                )
            }
            composable(Screen.Tenants.route) {
                TenantsScreen(
                    onNavigateToAddTenant = { navController.navigate(Screen.AddTenant.route) },
                    hazeState = hazeState
                )
            }
            composable(Screen.AddTenant.route) {
                AddTenantScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    hazeState = hazeState
                )
            }
        }

        // Floating Liquid Glass Bottom Bar (Overlay superpuesto)
        if (currentRoute in showBottomBarRoutes) {
            GlassBottomBar(
                currentRoute = currentRoute,
                onNavigate = { targetRoute ->
                    if (targetRoute != currentRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter),
                hazeState = hazeState
            )
        }
    }
}
