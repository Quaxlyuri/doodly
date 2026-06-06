# Doodly 제출 전 체크리스트

## GitHub

- [ ] 저장소 주소가 `https://github.com/Quaxlyuri/doodly`인지 확인
- [ ] 최종 소스가 `main` 브랜치에 있는지 확인
- [ ] 다른 브랜치에만 남은 코드가 없는지 확인
- [ ] `secrets.properties`와 `local.properties`가 커밋되지 않았는지 확인
- [ ] GitHub README에서 모든 스크린샷이 표시되는지 확인
- [ ] APK 링크를 눌러 다운로드되는지 확인
- [ ] PDF 링크를 눌러 보고서가 열리는지 확인

## 직접 입력할 내용

- [ ] `docs/FINAL_REPORT.md` 작성자 이름 입력
- [ ] 학번 입력
- [ ] 과목명/분반 입력
- [ ] 제출일 입력
- [ ] README의 2분 요약 영상 TODO를 실제 URL로 교체
- [ ] README의 10분 상세 발표 영상 TODO를 실제 URL로 교체

## 영상

- [ ] 2분 요약 영상 업로드
- [ ] 10분 상세 발표 영상 YouTube 업로드
- [ ] 영상 공개 또는 일부 공개 설정
- [ ] 영상 설명란에 GitHub 저장소 링크 추가
- [ ] 개인정보가 화면에 노출되지 않는지 확인

## 앱 검증

- [ ] 새 기기/에뮬레이터에서 APK 설치
- [ ] 일기 작성 → AI 생성 → 저장
- [ ] 갤러리 그리드/리스트 전환
- [ ] 감정 캘린더 날짜 이동
- [ ] 미래 날짜 선택 불가 확인
- [ ] 상세 화면 그림만 공유
- [ ] 상세 화면 그림과 글 공유
- [ ] 알림 ON/OFF 및 시간 변경
- [ ] 작성/상세 딥링크 테스트

## 제출 직전 명령

```powershell
.\gradlew.bat testDebugUnitTest compileDebugAndroidTestKotlin assembleDebug
git status
git branch --show-current
git ls-files secrets.properties local.properties
```

마지막 명령에서 아무 파일도 출력되지 않아야 합니다.
