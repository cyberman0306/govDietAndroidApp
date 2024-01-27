package com.hyunjun.govdietandroid

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class MyViewModel : ViewModel() {

    private val _currentGbdTitleAndValue = mutableStateOf("정부청사 CD003") // MutableState를 private으로 선언
    val currentGbdTitleAndValue: MutableState<String> = _currentGbdTitleAndValue // 읽기 전용 State로 외부에 노출

    private val _currentGbdTitle = mutableStateOf("정부청사") // MutableState를 private으로 선언
    val currentGbdTitle: MutableState<String> = _currentGbdTitle // 읽기 전용 State로 외부에 노출


    private val _currentGbdValue = mutableStateOf("CD003") // MutableState를 private으로 선언
    val currentGbdValue: MutableState<String> = _currentGbdValue // 읽기 전용 State로 외부에 노출

    private val _currentImgUrl = mutableStateOf("")
    val currentImgUrl: MutableState<String> = _currentImgUrl

    private val _rcOptions = mutableStateOf(listOf<Pair<String, String>>())
    val rcOptions: MutableState<List<Pair<String, String>>> = _rcOptions

    private val _gbdOptions = mutableStateOf(listOf<Pair<String, String>>())
    val gbdOptions: MutableState<List<Pair<String, String>>> = _gbdOptions

    private val _buttonIndex = mutableIntStateOf(0)
    val buttonIndex: MutableState<Int> = _buttonIndex

    private val showDialog = mutableStateOf(false)
    private val showImgState = mutableStateOf(false)

    fun updateCurrentGbdTitleAndValue(newValue: String) {
        _currentGbdTitleAndValue.value = newValue

        val parts = newValue.split(" ")
        _currentGbdTitle.value = parts[0]
        _currentGbdValue.value = parts[1]
        println("updateCurrentGbdTitleAndValue")
        println(newValue)
        println("updateCurrentGbdTitleAndValue")
    }
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

    fun setShowImgState(show: Boolean) {
        showImgState.value = show
    }

    fun updateButtonIndex(newIndex: Int) {
        _buttonIndex.value = newIndex
    }
    fun deleteAllImages(context: Context, extension: String) {
        viewModelScope.launch {
            deleteAllImagesFromInternalStorage(context, extension)
        }
    }

}
