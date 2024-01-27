package com.hyunjun.govdietandroid

/*


뷰모델과 DataStore를 함께 사용하여 간단한 데이터 저장 및 로드 기능을 구현하는 예시를 제공하겠습니다. 이 예시에서는 Preferences DataStore를 사용하여 문자열 데이터를 저장하고 로드하는 기능을 구현합니다.

1. 의존성 추가
먼저, 프로젝트의 build.gradle (Module: app) 파일에 DataStore 관련 의존성을 추가합니다.

gradle
Copy code
dependencies {
    implementation "androidx.datastore:datastore-preferences:1.0.0"
    // 기타 필요한 의존성
}
2. DataStore 키 정의
Preferences 키를 정의하여 DataStore에서 사용할 데이터를 식별합니다.

kotlin
Copy code
val EXAMPLE_KEY = stringPreferencesKey("example_key")
3. ViewModel 구현
ViewModel에서 DataStore를 사용하여 데이터를 저장하고 로드하는 메서드를 구현합니다.

kotlin
Copy code
class MyViewModel(private val context: Context) : ViewModel() {
    // DataStore 초기화
    private val dataStore = context.createDataStore(name = "settings")

    // 데이터를 DataStore에 저장하는 함수
    fun saveData(value: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[EXAMPLE_KEY] = value
            }
        }
    }

    // DataStore에서 데이터를 로드하는 함수
    val data: LiveData<String> = dataStore.data
        .map { preferences ->
            preferences[EXAMPLE_KEY] ?: "Default Value"
        }
        .asLiveData()
}
4. ViewModel 사용
ViewModel을 액티비티나 프래그먼트에서 사용하여 데이터를 저장하고 로드합니다.

kotlin
Copy code
class MainActivity : AppCompatActivity() {
    private val myViewModel by viewModels<MyViewModel> { MyViewModelFactory(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 데이터 저장 예시
        myViewModel.saveData("새로운 값")

        // 데이터 로드 예시
        myViewModel.data.observe(this) { value ->
            // UI 업데이트
        }
    }
}
주의사항
DataStore 접근은 비동기적으로 이루어지므로, 코루틴을 사용하여 데이터를 저장하고 로드합니다.
viewModelScope를 사용하여 ViewModel의 생명주기와 연결된 코루틴을 실행합니다.
ViewModel에 Context를 전달하기 위해 ViewModelFactory를 사용합니다.
이 예시는 ViewModel과 DataStore를 사용하여 간단한 데이터 저장 및 로드 기능을 구현하는 방법을 보여줍니다. 데이터 저장 및 로드는 비동기적으로 이루어지며, LiveData를 사용하여 UI가 데이터 변경을 관찰하고 반응할 수 있도록 합니다.


*/