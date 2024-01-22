package com.example.govdietandroid

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter

@Composable
fun DisplayImage(imageUrl: String?) {
    imageUrl?.let { url ->
        println("DisplayImage img URL : " + imageUrl)
        val painter = rememberImagePainter(data = "https://gbmo.go.kr" + url)
        Image(
            painter = painter,
            contentDescription = null, // 적절한 설명을 제공하시면 좋습니다.
            //modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // 이미지 스케일 조정
        )
    }
}


@Composable
fun DynamicButtonList(rcOptions: List<Pair<String, String>>, onButtonClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            //.fillMaxSize()
            //.padding(horizontal = 5.dp)  // 양쪽으로 패딩 추가
            .padding(vertical = 8.dp, horizontal = 5.dp) // 양쪽으로 패딩 추가
            .horizontalScroll(rememberScrollState())
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

