package com.jrg_upm.tennisrank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.jrg_upm.tennisrank.navigation.LogNavigation
import com.jrg_upm.tennisrank.ui.theme.TennisRankTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TennisRankTheme {
                val navController = rememberNavController()
                LogNavigation(navController = navController)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    TennisRankTheme()  {
    }
}

