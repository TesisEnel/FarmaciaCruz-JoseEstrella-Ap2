package edu.ucne.farmaciacruz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import edu.ucne.farmaciacruz.presentation.navigation.AppNavigation
import edu.ucne.farmaciacruz.ui.theme.FarmaciaCruzTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FarmaciaCruzTheme {
                AppNavigation()
            }
        }
    }
}