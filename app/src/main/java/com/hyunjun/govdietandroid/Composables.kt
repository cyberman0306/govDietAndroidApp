package com.hyunjun.govdietandroid

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter

@Composable
fun DisplayImage(imageUrl: String?) {
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        rotation += rotationChange
        offset += offsetChange
    }
    imageUrl?.let { url ->
        val painter = rememberImagePainter(data = "https://gbmo.go.kr" + url)

        Image(
            painter = painter,
            contentDescription = "mainView Diet img", // 적절한 설명을 제공하시면 좋습니다.
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state = state)
                .fillMaxSize()
            //contentScale = ContentScale.Crop // 이미지 스케일 조정
        )
    }
}


@Composable
fun DynamicButtonList(rcOptions: List<Pair<String, String>>, onButtonClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .fillMaxSize()
    ) {
        rcOptions.forEach { (value, title) ->
            if (title != "") {
                Button(onClick = {
                    onButtonClick(value)
                }, modifier = Modifier
                    .padding()
                ) {
                    Text(title)
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