package com.hyunjun.govdietandroid

import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import org.jsoup.Jsoup
import org.jsoup.nodes.Document



class MainActivity : ComponentActivity() {
    private val myViewModel: MyViewModel by viewModels()

    private lateinit var webView: WebView // WebView 인스턴스를 저장하기 위한 변수
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val showDialog = remember { mutableStateOf(false) }
            //SaveAndLoadFunc("")
            MainScreen(showDialog)
            SettingsDialog( myViewModel.gbdOptions.value, showDialog) { gbdValue ->
                myViewModel.updateCurrentImgUrl("")
                myViewModel.updateCurrentGbdValue(gbdValue)
                //myViewModel.currentGbdValue.value = gbdValue
                loadWebViewWithGbd(gbdValue)
                showDialog.value = false // 설정 창 닫기

                // 지정한 gbd 저장함수 추가
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
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                MainWebView(showDialog) { gbdValue ->
                    //여기작동안함
                    loadWebViewWithGbd(gbdValue) // URL 바탕 로딩
                    showDialog.value = false // 설정 창 닫기
                    println("MainWebView callback")
                    // 여기 점검하기
                }
                Box(modifier = Modifier
                    .fillMaxWidth(1f)
                    .fillMaxHeight(0.9f),
                    contentAlignment = Alignment.Center
                ) {
                    if (myViewModel.currentImgUrl.value != "") {
                        DisplayImage(imageUrl = myViewModel.currentImgUrl.value)
                    } else {
                        progressView()
                    }
                }
                DynamicButtonList(rcOptions = myViewModel.rcOptions.value) { value ->
                    myViewModel.updateCurrentImgUrl("")
                    loadWebViewWithRC(gbdValue = myViewModel.currentGbdValue.value, RcValue = value) // URL 바탕 로딩

                    // 마지막으로 누른것 바탕으로 GBD, rc 저장해서 가지고 있기 함수 추가
                }
                SaveDateShowPage()

            }
        }
    }

    @Composable
    fun MainWebView(showDialog: MutableState<Boolean>, onGbdSelected: (String) -> Unit) {

        val javascriptInterfaceGetGbdInfo = JavaScriptInterfaceGetGbdInfo { option ->
            if (!myViewModel.gbdOptions.value.contains(option)) {
                myViewModel.addGbdOption(option)
                println("gbdOptions.add")
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxHeight(0.0f),
            factory = { context ->
                WebView(context).also { webView = it }.apply {
                    webViewClient = CustomWebViewClient(javascriptInterfaceGetGbdInfo)
                    settings.javaScriptEnabled = true
                    addJavascriptInterface(javascriptInterfaceGetGbdInfo, "HTMLViewer")
                    println("loadURL in AndroidView")
                    loadUrl("https://gbmo.go.kr/chungsa/dv/dietView/selectDietCalendarView.do?gbd=${myViewModel.currentGbdValue.value}")

                    val javaScriptInterfaceForRC =
                        JavaScriptInterfaceForRC(upperCode = myViewModel.currentGbdValue.value) { newRcOptions ->
                            //rcValue를 처리하는 로직
                            myViewModel.rcOptions.value = myViewModel.rcOptions.value + newRcOptions
                            println(myViewModel.rcOptions.value)
                        }
                    addJavascriptInterface(javaScriptInterfaceForRC, "RCInterface")

                    val javaScriptInterfaceForImg =
                        JavaScriptInterfaceForImg { imgSrc ->
                            println("이미지 소스 URL:" + imgSrc)
                            myViewModel.updateCurrentImgUrl(newUrl = imgSrc)
                        }
                    addJavascriptInterface(javaScriptInterfaceForImg, "ImgInterface")
                }
            }
        )
    }

    private fun loadWebViewWithGbd(gbdValue: String) {
        myViewModel.rcOptions.value = listOf<Pair<String, String>>()
        println("loadWebViewWithGbd start")
        println("gbdValue: " + gbdValue)
        val url = "https://gbmo.go.kr/chungsa/dv/dietView/selectDietCalendarView.do?gbd=$gbdValue"
        webView.apply {
            val javaScriptInterfaceForRC =
                JavaScriptInterfaceForRC(upperCode = gbdValue) { newRcOptions ->
                    // rcValue를 처리하는 로직
                    myViewModel.rcOptions.value = myViewModel.rcOptions.value + newRcOptions
                    //println(myViewModel.rcOptions.value)
                }
            addJavascriptInterface(javaScriptInterfaceForRC, "RCInterface")

            val javaScriptInterfaceForImg =
                JavaScriptInterfaceForImg { imgSrc ->
                // 이미지 소스 URL을 처리하는 로직
                println("이미지 소스 URL :"+imgSrc)
                myViewModel.updateCurrentImgUrl(newUrl = imgSrc)
            }
            addJavascriptInterface(javaScriptInterfaceForImg, "ImgInterface")
            loadUrl(url)
        }
    }

    private fun loadWebViewWithRC(gbdValue: String, RcValue: String) {
        println("loadWebViewWithRC start")
        println("gbdValue: " + gbdValue)
        val url = "https://gbmo.go.kr/chungsa/dv/dietView/selectDietCalendarView.do?gbd=$gbdValue&rc=$RcValue"
        webView.apply {

            removeJavascriptInterface("HTMLViewer")
            removeJavascriptInterface("RCInterface")
            updateWebViewUrl(url)
        }
    }


    // URL 업데이트를 위한 함수
    private fun updateWebViewUrl(newUrl: String) {
        webView.loadUrl(newUrl)
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









    class CustomWebViewClient(
        val javaScriptInterfaceGetGbdInfo: JavaScriptInterfaceGetGbdInfo
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

    inner class JavaScriptInterfaceGetGbdInfo(private val updateOptions: (Pair<String, String>) -> Unit) {
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
                    } else {
                        println("gbd element value is empty")
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
                        //println(value + " " + title)
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
        @JavascriptInterface
        fun processHTMLForImg(html: String) {
            lifecycleScope.launch {
                val doc: Document = Jsoup.parse(html)
                val imgElement = doc.select("img[src][data-id='diet']").first()

                println("JavaScriptInterfaceForImg start")
                imgElement?.let {
                    val imgSrc = it.attr("src")
                    updateImgSrc(imgSrc)
                }
            }
        }
    }
}