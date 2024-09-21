package com.repo.compose

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.repo.compose.data.generateBinaryTree
import com.repo.compose.ui.custom.DendrogramUI
import com.repo.compose.ui.custom.IconLabelButton
import com.repo.compose.ui.theme.ComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting(this)
                }
            }
        }
    }
}

@Composable
fun Greeting(context: Context, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(all = 16.dp)) {
        DendrogramUI(
            entryNodes = generateBinaryTree(),
            modifier = modifier
        )
        IconLabelButton(
            icon = ImageVector.vectorResource(R.drawable.rocket),
            label = "Click"
        ) {
            Toast.makeText(context, "Click!", Toast.LENGTH_SHORT).show()
        }
    }
}
