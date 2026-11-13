# 냥도쿠
사진 속 검은 콧수염 턱시도 고양이를 주인공으로 한 Android 논리 퍼즐.

## 플레이
- 각 행, 열, 색깔 구역에 고양이 한 마리. 대각선을 포함해 서로 붙을 수 없음.
- 한 번 탭: 제외/해제. 빠르게 두 번 탭: 고양이. 쓸기: 여러 칸 제외.
- 키보드 Enter/Space는 빈칸 → 제외 → 고양이 → 빈칸으로 순환.
- 4×4~8×8, 연결된 색깔 구역과 유일해를 검증한 240개 레벨.
- 오늘의 퍼즐, 자동 저장, 되돌리기, 힌트, 효과음/진동/색깔 구분 기호 설정.
- 최초 힌트 5개. 각 퍼즐 첫 클리어 힌트 1개. 선택한 보상형 광고 완료 시 힌트 3개.
- 배너, 전면 강제 광고, 레벨 전환 광고, 하트 소진 제한 없음.

## APK 받기
GitHub **Actions → Android APK → 성공한 실행 → Artifacts → Nyangdoku-debug-APK** ZIP을 내려받고 압축 해제 후 APK를 설치합니다. 로그인 필요. Debug APK는 Google 테스트 광고를 사용하며 실제 수익은 발생하지 않습니다.

## 개발 / 검증
JDK 17, Android SDK 36, Gradle 8.11.1 사용.
```
npm install
npm test
gradle assembleDebug lintDebug
python3 -m http.server 8000 --directory app/src/main/assets
```
브라우저에서 http://localhost:8000 을 열어 UI를 확인할 수 있습니다. 광고는 Android 전용이며 브라우저에서 가짜 보상을 지급하지 않습니다.
`python3 scripts/generate_levels.py`는 고정 시드로 전체 레벨을 재생성합니다. 출시 이후 레벨 데이터 순서를 바꾸면 저장된 배치와 호환되지 않으므로 버전 마이그레이션이 필요합니다.

## 출시 전 필수 작업
1. 앱 ID `com.nyangdoku.game` 확정. 이미 등록된 앱 ID는 변경하지 않습니다.
2. AdMob에서 앱과 **보상형 광고 단위** 생성, Privacy & messaging 구성.
3. 실제 ID 전달: `gradle bundleRelease -PADMOB_APP_ID=실제앱ID -PADMOB_REWARDED_ID=실제보상형ID`. 테스트 ID 상태에서는 Release 빌드가 차단됩니다.
4. Android Studio의 Generate Signed Bundle / APK에서 개인 업로드 키로 **서명된 AAB** 생성. 키/비밀번호를 저장소에 커밋하지 않습니다. 현재 Release signingConfig는 의도적으로 미설정입니다.
5. 개인정보처리방침 공개 URL, 운영자 연락처, 데이터 보안/광고/대상 연령/콘텐츠 등급 설정을 실제 SDK 및 운영 방식에 맞게 작성합니다. `docs/PRIVACY-DRAFT.md`는 검토용 초안입니다.
6. 실제 기기에서 오프라인 진행/재시작/뒤로가기/광고 취소/광고 보상/동의 변경/대형 글꼴/다양한 화면 크기를 검증합니다.
7. Play Console에 표시되는 계정별 테스트 및 출시 요건을 완료합니다. 이 저장소 생성은 스토어 출시 또는 광고 계정 승인 완료를 의미하지 않습니다.

## 광고 구현
Google Mobile Ads SDK와 UMP 사용. 동의 상태가 광고 요청을 허용할 때만 초기화합니다. 보상은 `onUserEarnedReward`에서 한 번만 지급하고, 단순 닫기/로드 실패에는 지급하지 않습니다. 사용자가 버튼을 누른 경우에만 광고 표시. 광고는 사전 로드하며 1시간 미만으로 만료 처리합니다.
공식 문서: https://developers.google.com/admob/android/rewarded / https://developers.google.com/admob/android/privacy

## 에셋
`app/src/main/assets/cat.png`: 제공된 두 고양이 사진을 참조해 생성한 투명 배경 캐릭터. 원본 게임 이미지, 코드, 상표는 포함하지 않습니다. 이미지 생성 프롬프트: 검은 턱시도 고양이, 흰 이마·코 줄무늬와 흰 볼, 코 아래 넓고 검은 콧수염, 노란 초록 눈, 귀여운 정면 게임 캐릭터 머리, 투명 배경. 앱 내 사용 파일로 저장됨.

## 저장
진행 기록은 WebView 로컬 저장소에 보관됩니다. 앱 삭제나 데이터 삭제 시 사라지며 클라우드 동기화는 없습니다. 자동 백업은 꺼져 있습니다.
