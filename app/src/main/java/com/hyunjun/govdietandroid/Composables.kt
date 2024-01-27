package com.hyunjun.govdietandroid

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp




@Composable
fun DynamicButtonList(rcOptions: List<Pair<String, String>>, onButtonClick: (String, Int) -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(4.dp)
    ) {
        rcOptions.forEachIndexed { index, (value, title) ->
            if (title != "") {
                OutlinedButton(onClick = {
                    onButtonClick(value, index)
                }, modifier = Modifier
                    .padding(4.dp)
                ) {
                    Text(title,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black)
                }
            }

        }
    }
}

@Composable
fun progressView() {
    CircularProgressIndicator(
        modifier = Modifier.width(64.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}



@Composable
fun DisplayImageFromInternalStorage(context: Context, filename: String) {
    val bitmap = loadImageFromInternalStorage(context, filename)
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        rotation += rotationChange
        offset += offsetChange
    }
    bitmap?.let {
        println("filename is : $filename")
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "mainView Diet img", // 적절한 설명
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state = state)
                .fillMaxSize()
        )
    }
}

fun deleteAllImagesFromInternalStorage(context: Context, extension: String) {
    val directory = context.filesDir
    val files = directory.listFiles()
    files?.forEach { file ->
        if (file.isFile && file.name.endsWith(".$extension")) {
            file.delete()
        }
    }
}