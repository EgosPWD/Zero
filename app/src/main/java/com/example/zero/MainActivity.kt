package com.example.zero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zero.ui.theme.ZeroTheme
import com.example.zero.views.auth.LoginScreen
import com.example.zero.views.auth.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZeroTheme{
                val viewModel: LoginViewModel = viewModel()
                LoginScreen(viewModel)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    ZeroTheme {

    }
}