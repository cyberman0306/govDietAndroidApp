package com.hyunjun.govdietandroid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import com.hyunjun.govdietandroid.SettingDataStore.Companion.dataStore


class MyViewModel : ViewModel() {
    
    
    private val _currentGbdValue = mutableStateOf("CD003") // MutableState를 private으로 선언
    val currentGbdValue: MutableState<String> = _currentGbdValue // 읽기 전용 State로 외부에 노출

    private val _currentImgUrl = mutableStateOf("")
    val currentImgUrl: MutableState<String> = _currentImgUrl

    private val _rcOptions = mutableStateOf(listOf<Pair<String, String>>())
    val rcOptions: MutableState<List<Pair<String, String>>> = _rcOptions

    private val _gbdOptions = mutableStateOf(listOf<Pair<String, String>>())
    val gbdOptions: MutableState<List<Pair<String, String>>> = _gbdOptions

    val showDialog = mutableStateOf(false)


    // 데이터 갱신을 위한 함수
    fun updateCurrentGbdValue(newValue: String) {
        _currentGbdValue.value = newValue
    }

    fun updateCurrentImgUrl(newUrl: String) {
        _currentImgUrl.value = newUrl
    }

    fun updateRcOptions(newOptions: List<Pair<String, String>>) {
        _rcOptions.value = newOptions
    }

    fun addGbdOption(newOption: Pair<String, String>) {
        val currentList = ArrayList(_gbdOptions.value)
        currentList.add(newOption)
        _gbdOptions.value = currentList
    }

    fun setShowDialog(show: Boolean) {
        showDialog.value = show
    }

}
