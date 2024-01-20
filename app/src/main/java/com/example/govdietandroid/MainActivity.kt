package com.example.govdietandroid

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import org.jsoup.Jsoup
import org.jsoup.nodes.Document



class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView // WebView 인스턴스를 저장하기 위한 변수
    var currentGbdValue: String = ""
    private var currentImgUrl = mutableStateOf("")

    private var rcOptions = mutableStateOf(listOf<Pair<String, String>>())

    private var gbdOptions = mutableListOf<Pair<String, String>>() // value, text 쌍을 저장
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val showDialog = remember { mutableStateOf(false) }
            Text(currentImgUrl.value)
            MainScreen(showDialog)
            SettingsDialog(gbdOptions, showDialog) { gbdValue ->
                currentGbdValue = gbdValue
                loadWebViewWithGbd(gbdValue)
                // 이곳에 버튼눌렀을 때 이미지 가져오는 함수 추가?
                showDialog.value = false // 설정 창 닫기
            }

        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(showDialog: MutableState<Boolean>) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("정부청사 식단") },
                    actions = {
                        IconButton(onClick = { showDialog.value = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = "설정")
                        }
                    }
                )
            }
        ) { paddingValues ->
            // 여기에 나머지 컨텐츠 배치
            Column(modifier = Modifier.padding(paddingValues)) {
                MainWebView(showDialog) { gbdValue ->  // 이 부분에 onGbdSelected 콜백 함수 전달
                    loadWebViewWithGbd(gbdValue) // URL 바탕 로딩
                    showDialog.value = false // 설정 창 닫기
                }
                Text(currentImgUrl.value)
                DisplayImage(imageUrl = currentImgUrl.value)
                DynamicButtonList()
            }
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun MainWebView(showDialog: MutableState<Boolean>, onGbdSelected: (String) -> Unit) {

        val javascriptInterface = JavaScriptInterface { option ->
            if (!gbdOptions.contains(option)) {
                gbdOptions.add(option)
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxHeight(0.9f),
            factory = { context ->
                WebView(context).also { webView = it }.apply {
                    webViewClient = CustomWebViewClient(javascriptInterface)
                    settings.javaScriptEnabled = true
                    addJavascriptInterface(javascriptInterface, "HTMLViewer")
                    loadUrl("https://gbmo.go.kr/chungsa/dv/dietView/selectDietCalendarView.do")

                    // 웹뷰 크기를 0으로 설정
                    this.layoutParams = FrameLayout.LayoutParams(0, 0)
                }
            }
        )

//        Button(onClick = { showDialog.value = true }) {
//            Text("설정")
//        }
        //}

    }

    private fun loadWebViewWithGbd(gbdValue: String) {
        rcOptions.value = listOf<Pair<String, String>>()

        val url = "https://gbmo.go.kr/chungsa/dv/dietView/selectDietCalendarView.do?gbd=$gbdValue"
        webView.apply {
            val javaScriptInterfaceForRC =
                JavaScriptInterfaceForRC(upperCode = gbdValue) { newRcOptions ->
                    // rcValue를 처리하는 로직
                    rcOptions.value = rcOptions.value + newRcOptions
                }
            addJavascriptInterface(javaScriptInterfaceForRC, "RCInterface")

            val javaScriptInterfaceForImg =
                JavaScriptInterfaceForImg { imgSrc ->
                // 이미지 소스 URL을 처리하는 로직
                //println("이미지 소스 URL :"+imgSrc)
            }
            addJavascriptInterface(javaScriptInterfaceForImg, "ImgInterface")



            loadUrl(url)
        }
    }

    class CustomWebViewClient(
        val javaScriptInterface: JavaScriptInterface
    ) : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            // 페이지 로드 완료 후 HTML 내용을 JavaScript 인터페이스로 전송
            view.evaluateJavascript(
                "javascript:window.HTMLViewer.processHTML(document.documentElement.outerHTML);",
                null
            )
            // rc 값을 가져오는 JavaScript 실행
            view.evaluateJavascript(
                "javascript:window.RCInterface.processHTMLForRC(document.documentElement.outerHTML);",
                null
            )

            // 이미지 URL을 가져오는 JavaScript 실행
            view.evaluateJavascript(
                "javascript:window.ImgInterface.processHTMLForImg(document.documentElement.outerHTML);",
                null
            )
        }
    }

    inner class JavaScriptInterface(private val updateOptions: (Pair<String, String>) -> Unit) {
        @JavascriptInterface
        fun processHTML(html: String) {
            lifecycleScope.launch {
                val doc: Document = Jsoup.parse(html)
                // 'gbd' 이름을 가진 <select> 요소 내의 모든 <option> 태그 찾기
                val options = doc.select("select[name=gbd] option")
                println("processHTML start")
                // 각 <option> 태그에서 value 추출
                options.forEach { element ->
                    val value = element.attr("value")
                    val text = element.text()
                    //println("Option Value: $value, Text: $text")
                    if (value.isNotEmpty()) {
                        updateOptions(value to text)
                    }
                }

            }
        }
    }

    //HTML에서 RC 값 추출
    inner class JavaScriptInterfaceForRC(
        private val upperCode: String,
        private val updateRcOptions: (Pair<String, String>) -> Unit
    ) {
        @JavascriptInterface
        fun processHTMLForRC(html: String) {
            lifecycleScope.launch {
                val doc: Document = Jsoup.parse(html)
                val options = doc.select("select[name=rc] option[data-upper_code=$upperCode]")
                println("JavaScriptInterfaceForRC start")
                // 각 <option> 태그에서 value 추출
                options.forEach { element ->
                    val value = element.attr("value")
                    val title = element.attr("title")
                    //println("RC Value: $value, Text: $text")
                    if (value.isNotEmpty()) {
                        println(value + " " + title)
                        updateRcOptions(Pair(value, title))
                    }
                }
            }
        }
    }

    //HTML에서 IMG 값 추출
    inner class JavaScriptInterfaceForImg(
        private val updateImgSrc: (String) -> Unit
    ) {
        //@SuppressLint("NotConstructor")
        @JavascriptInterface
        fun processHTMLForImg(html: String) {
            lifecycleScope.launch {
                val doc: Document = Jsoup.parse(html)
                val imgElement = doc.select("img[src][data-id='diet']").first()

                println("JavaScriptInterfaceForImg start")
                imgElement?.let {
                    val imgSrc = it.attr("src")
                    println("Image Source: $imgSrc")
                    currentImgUrl.value  = imgSrc

                    updateImgSrc(imgSrc)
                }
            }
        }
    }

    @Composable
    fun DisplayImage(imageUrl: String?) {
        imageUrl?.let { url ->
            println("DisplayImage img URL : " + imageUrl)
            val painter = rememberImagePainter(data = "https://gbmo.go.kr" + url)
            Image(
                painter = painter,
                contentDescription = null, // 적절한 설명을 제공하시면 좋습니다.
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // 이미지 스케일 조정
            )
        }
    }

    // URL 업데이트를 위한 함수
    private fun updateWebViewUrl(newUrl: String) {
        rcOptions.value = listOf<Pair<String, String>>()
        webView.loadUrl(newUrl)
    }

    @Composable
    fun DynamicButtonList() {
        Row(
            modifier = Modifier
                .fillMaxSize()
                //.padding(horizontal = 5.dp)  // 양쪽으로 패딩 추가
                .padding(vertical = 8.dp, horizontal = 5.dp) // 양쪽으로 패딩 추가
                .horizontalScroll(rememberScrollState())
        ) {
            rcOptions.value.forEach { (value, title) ->
                if (title != "") {
                    Button(onClick = {
                        updateWebViewUrl("https://gbmo.go.kr/chungsa/dv/dietView/selectDietCalendarView.do?gbd=$currentGbdValue&rc=$value")
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
    fun SettingsDialog(
        gbdOptions: List<Pair<String, String>>,
        showDialog: MutableState<Boolean>,
        onGbdSelected: (String) -> Unit
    ) {
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text("설정") },
                text = {
                    LazyColumn { // LazyColumn을 사용하여 스크롤 가능한 리스트 생성
                        items(gbdOptions) { option ->
                            val (value, text) = option
                            Row {
                                Text("$text")
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = { onGbdSelected(value) }) {
                                    Text("이동")
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showDialog.value = false }) {
                        Text("닫기")
                    }
                }
            )
        }
    }
}