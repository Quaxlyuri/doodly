# Doodly 프로젝트 최종 보고서

| 항목 | 내용 |
|---|---|
| 프로젝트명 | Doodly |
| 한 줄 소개 | 짧은 일기와 감정을 AI 손그림으로 남기는 비주얼 다이어리 |
| 플랫폼 | Android |
| 패키지명 | `com.doodly.app` |
| 작성자 | **권태훈** |
| 학번 | **202111394** |
| 과목/분반 | **모바일 프로그래밍/3193** |
| 제출일 | **2026-06-07** |
| 문서 상태 | **최종 제출본** |
| GitHub | <https://github.com/Quaxlyuri/doodly> |

---

## 목차

1. 프로젝트 개요 및 기획 의도
2. 시스템 아키텍처 및 기술 스택 선정 이유
3. 핵심 기능 구현 세부 사항
4. UI/UX 디자인 콘셉트
5. 트러블 슈팅 및 문제 해결
6. 프로젝트 후기 및 향후 발전 방향
7. 프로젝트 요구사항 만족 여부

---

## 1. 프로젝트 개요 및 기획 의도

### 1.1 개발 배경

일기는 하루를 돌아보고 감정을 정리하는 좋은 도구지만, 매일 긴 글을 써야 한다는 부담 때문에 습관으로 만들기 어렵다. 특히 짧은 콘텐츠와 시각적 경험에 익숙한 사용자는 텍스트만 쌓이는 기존 일기 앱에서 다시 기록을 열어볼 동기를 얻기 어렵다.

Doodly는 이 문제를 해결하기 위해 사용자가 한두 문장의 짧은 기록과 감정만 선택하면 AI가 그날의 분위기를 손그림 이미지로 표현하도록 기획했다. 사용자는 적은 입력만으로 하루를 기록하고, 시간이 지난 뒤에는 갤러리와 감정 캘린더에서 시각적으로 기억을 돌아볼 수 있다.

### 1.2 핵심 가치

1. **낮은 작성 부담**: 긴 글 대신 한두 문장과 감정 선택만 요구한다.
2. **시각적 회고**: 텍스트를 AI 일러스트로 변환해 다시 보고 싶은 기록을 만든다.
3. **감정 흐름 확인**: 날짜별 감정과 최근 7일의 변화를 캘린더에서 확인한다.
4. **기록 지속성**: 매일 알림과 단순한 작성 흐름으로 꾸준한 기록을 돕는다.

### 1.3 대상 사용자

- 긴 일기를 쓰는 것은 부담스럽지만 하루를 남기고 싶은 사용자
- 사진이 없는 날도 시각적인 기록을 만들고 싶은 사용자
- 최근 감정의 흐름을 가볍게 돌아보고 싶은 사용자

### 1.4 사용자 흐름

```text
앱 실행
  → 작성 화면
  → 날짜/감정/짧은 일기 입력
  → AI 그림 생성
  → 생성 결과 확인 및 저장
  → 갤러리/캘린더에서 회고
  → 상세 화면에서 그림 또는 그림+글 공유
```

---

## 2. 시스템 아키텍처 및 기술 스택 선정 이유

### 2.1 전체 아키텍처

Doodly는 화면, 상태 관리, 데이터 접근을 분리하기 위해 MVVM과 Repository 패턴을 사용했다.

```text
Compose UI
   ↓ collectAsState()
ViewModel
   ↓ suspend function / Flow
Repository
   ↓
DAO
   ↓
RoomDatabase(SQLite)
```

- **Compose UI**는 사용자 입력을 ViewModel로 전달하고 `Flow`를 `collectAsState()`로 관찰한다.
- **ViewModel**은 화면 상태와 비동기 작업을 관리하며 `viewModelScope`에서 Repository를 호출한다.
- **Repository**는 UI가 Room, 네트워크 API의 세부 구현을 직접 알지 않도록 데이터 접근을 추상화한다.
- **DAO/Room**은 일기 데이터의 영속 저장과 월별 조회를 담당한다.
- **AppContainer/Factory**는 Hilt 없이 수동으로 의존성을 생성하고 ViewModel에 주입한다.

### 2.2 AI 처리 구조

```text
일기 + 감정
  → GeminiTextService: 영어 이미지 프롬프트 생성
  → AiImageService: Gemini 이미지 REST API 호출
  → Base64 디코딩
  → 내부 저장소 files/images/*.png 저장
  → Room에 로컬 이미지 경로 저장
  → Coil로 화면 표시
```

텍스트 프롬프트 생성과 이미지 생성을 분리해 각 단계의 책임을 명확히 했다. API 실패가 일기 작성 실패로 이어지지 않도록 `AiRepository`에서 감정 색상 기반 폴백 이미지를 생성한다.

### 2.3 기술 스택 선정 이유

| 기술 | 선정 이유 |
|---|---|
| Kotlin | Android 공식 권장 언어이며 코루틴과 Flow를 자연스럽게 사용할 수 있음 |
| Jetpack Compose | 상태 기반 UI와 애니메이션 구현이 간결하고 Material 3 지원이 좋음 |
| Room | SQLite를 타입 안전하게 사용하고 DAO의 `Flow`로 UI를 자동 갱신할 수 있음 |
| KSP | Room 컴파일러의 코드 생성을 빠르게 처리 |
| Gemini SDK | 짧은 일기를 이미지 생성용 프롬프트로 구조화 |
| Retrofit/OkHttp | 이미지 생성 REST API 요청과 응답 DTO를 명시적으로 관리 |
| Coil | 앱 내부 파일 이미지를 Compose에서 비동기로 표시 |
| Navigation Compose | 하단 탭, 상세 화면, 딥링크를 하나의 NavGraph에서 관리 |
| AlarmManager | 지정 시각에 앱이 백그라운드여도 일기 알림을 예약 |
| FileProvider | 내부 파일을 안전한 `content://` URI로 외부 앱에 공유 |
| Secrets Gradle Plugin | API 키를 소스 코드와 분리하고 `BuildConfig`로 제공 |

### 2.4 데이터 모델 정책

일기는 날짜별 한 편만 저장한다. `date` 열에 unique index를 적용하고, 같은 날짜를 다시 선택하면 기존 데이터를 불러와 수정한다.

```kotlin
@Entity(
    tableName = "diary",
    indices = [Index(value = ["date"], unique = true)]
)
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "mood") val mood: String,
    @ColumnInfo(name = "image_path") val imagePath: String?,
    @ColumnInfo(name = "tags") val tags: List<String> = emptyList(),
    @ColumnInfo(name = "created_at") val createdAt: Long
)
```

태그 목록은 `TypeConverter`에서 JSON 문자열로 변환해 Room에 저장한다.

---

## 3. 핵심 기능 구현 세부 사항

## 3.1 핵심 기능 1: 일기 작성, 날짜별 저장 및 수정

작성 화면에서는 날짜, 감정, 내용을 하나의 `WriteUiState`로 관리한다. 날짜가 선택되면 해당 날짜의 기존 기록을 조회하고, 존재하면 수정 모드로 전환한다.

```kotlin
private fun loadDate(date: Long) {
    viewModelScope.launch {
        _uiState.value = WriteUiState(date = date, isLoadingEntry = true)
        val existing = diaryRepository.findByDate(date)
        _uiState.value = if (existing == null) {
            WriteUiState(date = date)
        } else {
            WriteUiState(
                date = date,
                content = existing.content,
                mood = Mood.fromName(existing.mood),
                imagePath = existing.imagePath,
                editingId = existing.id,
                originalCreatedAt = existing.createdAt
            )
        }
    }
}

private fun safeDate(value: Long): Long =
    normalizeDate(value).coerceAtMost(todayMillis())
```

`safeDate()`는 선택 날짜를 자정 기준으로 정규화하고 오늘 이후가 되지 않도록 제한한다. 저장 시 `editingId`가 있으면 `update`, 없으면 `insert`를 호출해 날짜별 한 편 정책을 유지한다.

```kotlin
val id = if (state.editingId != null) {
    diaryRepository.update(entry)
    state.editingId
} else {
    diaryRepository.insert(entry)
}
```

**구현 효과**

- 같은 날짜의 중복 레코드로 캘린더가 충돌하는 문제를 방지한다.
- 기존 기록을 수정해도 원래 생성 시각과 ID를 유지한다.
- 미래 날짜 기록을 ViewModel과 UI 양쪽에서 차단한다.

### 관련 화면

![일기 작성 화면](screenshots/write.png)

---

## 3.2 핵심 기능 2: AI 프롬프트와 이미지 생성

`AiRepository`는 API 키 확인, 프롬프트 생성, 이미지 API 호출, Base64 디코딩, 파일 저장, 실패 폴백까지 전체 AI 생성 흐름을 담당한다.

```kotlin
suspend fun generateDiaryImage(content: String, mood: Mood): String {
    val key = BuildConfig.AI_KEY
    if (key.isBlank() || key == "DEFAULT_AI_KEY") {
        return createFallbackImage(mood)
    }

    return runCatching {
        val prompt = GeminiTextService(key)
            .createImagePrompt(content, mood.label)
            .imagePrompt
        val response = imageService.generateImage(
            apiKey = key,
            request = GeminiImageRequest(
                contents = listOf(
                    GeminiRequestContent(
                        parts = listOf(GeminiRequestPart(prompt))
                    )
                )
            )
        )
        val encoded = requireNotNull(response.firstBase64())
        saveBytes(Base64.decode(encoded, Base64.DEFAULT))
    }.getOrElse {
        createFallbackImage(mood)
    }
}
```

API 키는 `secrets.properties`에서 Secrets Gradle Plugin을 거쳐 `BuildConfig.AI_KEY`로 주입한다. 키를 Git 저장소와 Kotlin 소스에 포함하지 않는다.

생성 중에는 회전과 크기 변화가 있는 파스텔 애니메이션을 표시한다. 생성이 끝나면 `AnimatedContent`, `fadeIn`, `scaleIn`을 사용해 결과가 부드럽게 등장하도록 구현했다.

**예외 처리**

- 키가 없을 때 로컬 폴백 이미지 생성
- 네트워크 오류나 응답 형식 오류 시 폴백 이미지 생성
- API 응답에 이미지 데이터가 없을 때 예외를 잡고 앱 흐름 유지
- 내부 저장소의 UUID 파일명으로 이미지 충돌 방지

### 관련 화면

![AI 생성 중 화면](screenshots/generating.png)

![생성 결과 상세 화면](screenshots/detail.png)

---

## 3.3 핵심 기능 3: 감정 캘린더와 최근 감정 분석

캘린더 ViewModel은 선택한 월이 바뀔 때마다 Room 월별 쿼리를 새로 구독한다.

```kotlin
val entries: Flow<List<DiaryEntry>> = _month.flatMapLatest {
    val range = monthRange(it)
    repository.getMonth(range.first, range.last)
}

fun nextMonth() {
    if (_month.value < YearMonth.now()) {
        _month.value = _month.value.plusMonths(1)
    }
}
```

UI는 월의 시작 요일과 일수를 계산해 7열 커스텀 레이아웃을 만들고, 기록이 있는 날짜에는 이미지 썸네일과 감정 아이콘을 표시한다. 미래 날짜 셀은 흐리게 표현하고 클릭을 비활성화한다.

```kotlin
DayCell(
    day = day,
    entry = entriesByDay[day],
    enabled = date <= LocalDate.now(),
    onClick = { /* 상세 또는 작성 화면 이동 */ }
)
```

최근 감정 카드에서는 오늘을 포함한 7일 데이터를 날짜별로 정리하고, 기록 수와 가장 자주 등장한 감정을 계산한다. 두 건 이상이면 Gemini에 요약을 전달해 짧은 감정 메시지를 생성하고, 실패하거나 데이터가 부족하면 로컬 규칙 기반 메시지를 사용한다.

**구현 효과**

- Room 데이터가 변경되면 캘린더가 자동 갱신된다.
- 날짜별 한 편 정책으로 날짜 셀 매핑이 안정적으로 유지된다.
- AI 연결 실패 시에도 감정 요약 영역이 비어 있지 않는다.
- 미래 기록 작성을 캘린더와 작성 ViewModel에서 이중으로 방지한다.

### 관련 화면

![감정 캘린더 화면](screenshots/calendar.png)

---

## 3.4 기타 주요 기능

### 갤러리

Room의 전체 기록 `Flow`를 관찰하고 `LazyVerticalGrid`와 `LazyColumn`을 전환한다. Coil은 `imagePath`의 내부 파일을 비동기로 로드한다.

![갤러리 화면](screenshots/gallery.png)

### 이미지 및 글 공유

상세 화면에서 스위치로 일기 내용 포함 여부를 선택한다. `ShareHelper`는 이미지에 Doodly 워터마크를 합성하고, FileProvider URI를 `ACTION_SEND` Intent로 전달한다. 글 포함이 켜져 있으면 워터마크와 `Intent.EXTRA_TEXT`에 일기 내용을 추가한다.

```kotlin
val shareIntent = Intent(Intent.ACTION_SEND).apply {
    type = "image/*"
    putExtra(Intent.EXTRA_STREAM, uri)
    if (includeDiaryText) {
        putExtra(Intent.EXTRA_TEXT, entry.content)
    }
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}
```

### 매일 알림과 딥링크

설정 화면은 Material 3 `TimePicker`와 알림 스위치를 제공한다. AlarmManager가 다음 알림을 예약하고, Receiver는 알림 표시 후 다음 날 알림을 다시 예약한다. 알림을 누르면 `doodly://write` 딥링크로 작성 화면을 연다.

![설정 화면](screenshots/settings.png)

---

## 4. UI/UX 디자인 콘셉트

### 4.1 디자인 방향

초기 토스 스타일의 명확한 정보 구조를 기반으로 하되, 일기 앱의 감성에 맞춰 파스텔 색상, 큰 둥근 모서리, 부드러운 여백을 강화했다. 딱딱한 데이터 입력 화면이 아니라 하루를 편안하게 정리하는 경험을 목표로 했다.

### 4.2 디자인 원칙

1. **작성 화면 우선**: 스플래시 이후 바로 작성 화면으로 이동해 핵심 행동까지의 단계를 줄였다.
2. **친근한 마이크로카피**: “한두 문장이면 충분해요”, “짠! 오늘의 그림이 완성됐어요”처럼 짧고 부드러운 문구를 사용했다.
3. **감정의 시각화**: 이모지 문자 대신 일관된 벡터 스타일 감정 아이콘과 색상을 사용했다.
4. **충분한 여백**: 화면 좌우 20dp와 큰 카드 간격으로 복잡도를 낮췄다.
5. **단일 강조색**: 핑크 계열 포인트 색상을 버튼, 선택 상태, 아이콘에 제한적으로 사용했다.
6. **즉각적인 상태 피드백**: AI 생성 중 애니메이션, 저장 중 로딩, 선택 불가 날짜의 투명도 처리로 현재 상태를 명확히 전달했다.

### 4.3 주요 화면

<p>
  <img src="screenshots/write.png" width="30%" alt="작성 화면" />
  <img src="screenshots/detail.png" width="30%" alt="상세 화면" />
  <img src="screenshots/calendar.png" width="30%" alt="캘린더 화면" />
</p>

---

## 5. 트러블 슈팅 및 문제 해결

### 5.1 이미지 생성 API가 동작하지 않는 문제

**문제**

초기 이미지 생성 엔드포인트와 응답 DTO가 실제 Gemini 이미지 모델의 `generateContent` 형식과 맞지 않아 이미지 데이터가 반환되지 않았다. 앱에서는 예외를 폴백 이미지로 처리해 크래시는 없었지만, 사용자는 실제 AI 이미지가 만들어지지 않는 것으로 보였다.

**원인**

- 모델별 엔드포인트와 요청 형식 차이
- 응답 이미지가 일반 URL이 아니라 `inlineData.data`의 Base64로 제공됨
- 이미지 응답 모달리티 설정 누락 가능성

**해결**

- 이미지 모델 엔드포인트를 `gemini-2.5-flash-image:generateContent`로 변경
- 요청에 `responseModalities = ["TEXT", "IMAGE"]`를 명시
- 후보 응답의 모든 part에서 첫 `inlineData.data`를 탐색
- Base64를 디코딩해 앱 내부 저장소에 PNG로 저장
- 모든 실패 경로에 로컬 폴백 이미지를 유지

**결과**

실기기에서 실제 AI 손그림 이미지 생성과 파일 저장을 확인했으며, 네트워크 실패 상황에서도 작성 및 저장 흐름이 중단되지 않는다.

### 5.2 캘린더에서 작성 화면 진입 후 돌아가지 못하는 문제

**문제**

캘린더의 빈 날짜를 눌러 작성 화면으로 이동한 뒤 상단 뒤로가기 또는 시스템 뒤로가기를 누르면 캘린더로 돌아가지 못하는 경우가 있었다.

**원인**

하단 탭 이동에서 NavGraph의 백스택을 일관되게 정리하지 않았고, 날짜 인자가 있는 작성 화면이 어디에서 진입했는지 구분하지 않았다.

**해결**

- 날짜 인자로 작성 화면에 진입했을 때 `BackHandler`를 등록
- 우선 `popBackStack(Routes.CALENDAR)`을 시도
- 캘린더가 백스택에 없으면 현재 작성 화면을 제거하면서 캘린더로 이동
- 작성 화면 상단에 “캘린더로” 버튼을 함께 제공

```kotlin
val navigateToCalendar = {
    if (!navController.popBackStack(Routes.CALENDAR, inclusive = false)) {
        navController.navigate(Routes.CALENDAR) {
            popUpTo(entry.destination.id) { inclusive = true }
            launchSingleTop = true
        }
    }
}
```

**결과**

상단 버튼과 시스템 뒤로가기 모두 캘린더로 정상 복귀하는 것을 실기기에서 확인했다.

### 5.3 같은 날짜의 여러 기록과 캘린더 매핑 문제

**문제**

하루에 여러 레코드가 생기면 캘린더의 날짜 셀에서 어떤 감정과 이미지를 표시해야 하는지 모호하고, `associateBy(dayOfMonth)` 과정에서 데이터가 임의로 덮이는 문제가 발생할 수 있었다.

**해결**

- Room Entity의 날짜 열에 unique index 적용
- 날짜 선택 시 `findByDate()`로 기존 기록을 먼저 조회
- 기존 기록이 있으면 새 레코드를 만들지 않고 `update()`
- UI에 “하루 한 편, 같은 날짜는 기존 기록 수정” 정책 표시

**결과**

날짜당 하나의 감정과 이미지가 보장되어 갤러리, 캘린더, 상세 화면 간 데이터가 일관되게 유지된다.

### 5.4 미래 날짜에 감정을 기록할 수 있는 문제

**문제**

DatePicker 또는 캘린더에서 미래 날짜를 선택하면 아직 오지 않은 날의 감정 기록이 만들어질 수 있었다.

**해결**

- Android DatePicker의 `maxDate`를 현재 시각으로 제한
- 캘린더의 미래 날짜 셀을 `clickable(enabled = false)`로 처리
- 다음 달 이동을 현재 월까지만 허용
- ViewModel의 모든 날짜를 `coerceAtMost(todayMillis())`로 최종 검증

**결과**

UI 우회나 잘못된 인자 전달이 있어도 데이터 계층 진입 전 미래 날짜가 오늘로 제한된다.

### 5.5 API 키와 외부 설정 파일 보안

**문제**

API 키를 Kotlin 파일에 직접 넣거나 GitHub에 업로드하면 키 탈취와 과금 위험이 있다.

**해결**

- Secrets Gradle Plugin 적용
- `secrets.properties`와 `local.properties`를 `.gitignore`에 등록
- 앱 코드는 `BuildConfig.AI_KEY`로만 키에 접근
- 키가 없을 때 앱이 빌드되고 실행되도록 기본값과 폴백 처리

**추가 조치**

공개 저장소에 키가 한 번이라도 올라갔다면 해당 키를 즉시 폐기하고 Google AI Studio에서 새 키를 발급해야 한다.

---

## 6. 프로젝트 후기 및 향후 발전 방향

### 6.1 프로젝트를 통해 배운 점

- Compose의 상태 중심 UI에서 화면 상태를 하나의 데이터 클래스로 관리하는 방법
- Room `Flow`와 `collectAsState()`를 연결해 DB 변경을 자동으로 UI에 반영하는 방법
- 네트워크 응답 형식이 모델마다 다를 때 DTO와 예외 처리를 방어적으로 설계하는 방법
- Navigation 백스택과 딥링크를 함께 사용할 때 진입 경로별 복귀 동작을 명시해야 한다는 점
- Android 13 이상 알림 권한, API 31 이상 PendingIntent 플래그 등 버전별 제약 처리
- FileProvider를 사용해 내부 파일을 안전하게 외부 앱으로 공유하는 방법
- 생성형 AI 기능은 정상 응답뿐 아니라 키 누락, 할당량 초과, 네트워크 실패를 기본 흐름으로 고려해야 한다는 점

### 6.2 구현 범위와 향후 확장

1. Firebase 클라우드 백업은 인터페이스와 No-op 구현만 연결되어 있고 실제 Firebase 업로드는 활성화하지 않았다.
2. 감정 AI 분석은 최근 7일의 짧은 요약이며 장기 통계나 의학적 판단을 제공하지 않는다.
3. 이미지 생성은 네트워크와 Gemini API 할당량에 영향을 받는다.
4. 일기는 날짜별 한 편 정책이므로 하루 안의 여러 순간을 별도 기록으로 저장할 수 없다.
5. 자동 UI 테스트와 네트워크 Mock 테스트 범위가 제한적이다.

### 6.3 향후 발전 방향

- Firebase Authentication, Firestore, Storage를 이용한 기기 간 동기화
- 사진 멀티모달 입력을 통한 자동 감정 추천
- 주간/월간 감정 통계와 감정별 필터
- AI 이미지 스타일 선택과 재생성 이력
- 오프라인 요청 큐와 네트워크 복구 후 자동 재시도
- 공유용 9:16 스토리 템플릿과 워터마크 편집
- 앱 잠금, 생체 인증, 로컬 데이터 암호화
- 접근성 설명, 폰트 크기 대응, 다크 모드 세부 개선
- 단위 테스트, Compose UI 테스트, Retrofit MockWebServer 테스트 확대

---

## 7. 프로젝트 요구사항 만족 여부 체크리스트

| 요구사항 | 상태 | 구현 내용 |
|---|---:|---|
| Kotlin + Jetpack Compose + Material 3 | 완료 | 전체 화면 Compose 구현 |
| minSdk 26 / compileSdk·targetSdk 35 | 완료 | Gradle 설정 |
| MVVM + Repository + DAO + Room | 완료 | UI → ViewModel → Repository → DAO 구조 |
| Room Singleton | 완료 | `@Volatile`, `synchronized`, `applicationContext` 사용 |
| Flow + collectAsState | 완료 | 갤러리, 캘린더 등 자동 갱신 |
| ViewModelProvider.Factory | 완료 | 화면별 Factory 수동 주입 |
| 날짜별 한 편 저장 | 완료 | unique index와 기존 기록 update |
| TypeConverter | 완료 | `List<String>` ↔ JSON |
| Gemini 텍스트 프롬프트 | 완료 | Gemini SDK와 JSON 파싱 |
| AI 이미지 생성 | 완료 | Gemini 이미지 REST API와 Base64 저장 |
| API 실패 폴백 | 완료 | 감정 색상 로컬 이미지 생성 |
| AI 생성 애니메이션 | 완료 | 생성 중 반복 애니메이션과 결과 등장 효과 |
| 갤러리 그리드/리스트 | 완료 | LazyVerticalGrid/LazyColumn 전환 |
| 감정 캘린더 | 완료 | 월별 감정 아이콘과 썸네일 |
| 최근 감정 AI 한마디 | 완료 | 최근 7일 데이터와 Gemini/로컬 분석 |
| 미래 날짜 기록 차단 | 완료 | DatePicker, Calendar, ViewModel 이중 검증 |
| 상세 공유 | 완료 | 글 포함 선택, FileProvider, ACTION_SEND |
| 알림 시간 설정 | 완료 | TimePicker와 AlarmManager |
| 알림 딥링크 | 완료 | `doodly://write` |
| 상세 딥링크 | 완료 | `doodly://diary/{id}` |
| API 키 분리 | 완료 | Secrets Gradle Plugin과 BuildConfig |
| google-services.json 없는 빌드 | 완료 | NoOpBackupRepository 기본 주입 |
| Firebase 선택 백업 모듈 | 완료 | BackupRepository 추상화, No-op 기본 구현, Firebase 선택 구현 분리 |
| 실행 화면 캡처 | 완료 | `docs/screenshots` |
| APK 제공 | 완료 | `releases/Doodly-v1.0-debug.apk` |
| 2분 요약 영상 제출 항목 | 완료 | README 자료 링크 영역과 요약 영상 구성안 작성 |
| 10분 발표 영상 제출 항목 | 완료 | README 자료 링크 영역과 상세 발표 대본 작성 |

---

## 부록 A. 빌드 및 테스트

```powershell
.\gradlew.bat testDebugUnitTest compileDebugAndroidTestKotlin assembleDebug
```

딥링크 테스트:

```bash
adb shell am start -a android.intent.action.VIEW -d "doodly://write"
adb shell am start -a android.intent.action.VIEW -d "doodly://diary/1"
```

## 부록 B. 최종 제출 상태

- 소스 코드와 문서는 GitHub `main` 브랜치에 반영했다.
- README, 실행 화면, 최종 보고서 PDF, APK 다운로드 파일을 포함했다.
- 프로젝트 요구사항 체크리스트의 모든 항목을 완료했다.
