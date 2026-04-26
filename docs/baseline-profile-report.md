# Baseline Profile Report

**Generated**: 2026-04-15 01:39 KST
**Branch**: develop
**Device**: Pixel 6 API 32 (Gradle Managed Device)
**Flavor**: demo

## Summary

| Metric | Value |
|---|---|
| Total rules | 21,636 |
| Startup (HSP) rules | 15,726 |
| Hot + Startup (HS) | 15,726 |
| Hot + Post-startup (HP) | 256 |
| Startup only (S) | 82 |
| Post-startup only (P) | 1,083 |
| File size | 3.1 MB |

## Top 15 Billboard Classes

| Class | Rules |
|---|---|
| HomePresenter | 158 |
| DaggerHiltComponents_SingletonC | 120 |
| MainActivity | 68 |
| PlayerWithPagerKt | 62 |
| PlayerState | 50 |
| PipState | 41 |
| FilterRowKt | 32 |
| HomeUiKt | 26 |
| HomeState | 25 |
| ChartFilter | 25 |
| ListPipPlayerKt | 22 |
| ComposableSingletons | 22 |
| Chart (domain model) | 21 |
| BillboardHeaderKt | 20 |
| BillboardFont | 20 |

## Top 15 Framework Classes

| Class | Rules |
|---|---|
| java.lang.Object | 3,472 |
| java.lang.String | 663 |
| Function1 | 550 |
| DefaultConstructorMarker | 520 |
| kotlin.coroutines.Continuation | 518 |
| Modifier | 454 |
| Composer | 405 |
| java.util.List | 378 |
| LayoutNode | 333 |
| kotlin.Unit | 310 |
| Function2 | 297 |
| CoroutineContext | 268 |
| AndroidComposeView | 251 |
| Placeable | 232 |
| Transition | 231 |

## Method Distribution

| Type | Count | Description |
|---|---|---|
| HSP (Hot + Startup + Post) | 15,726 | 앱 시작 + 런타임 모두에서 핫 |
| HP (Hot + Post-startup) | 256 | 런타임에서만 핫 |
| S (Startup only) | 82 | 시작 시에만 사용 |
| P (Post-startup only) | 1,083 | 시작 후 사용 |

## Delta vs Previous

첫 프로파일 생성 — 비교 대상 없음.

## Notes

- HomePresenter 가 앱 클래스 중 가장 많은 규칙 (158) — 4개 차트 동시 로드 + YouTube 비디오 로직 집중
- PipState 41 규칙 — swipe-to-dismiss 로직 포함
- Hilt DI 컴포넌트 (120 규칙) 가 두 번째로 높음 — 앱 시작 시 의존성 그래프 구축
- Compose 프레임워크 클래스가 전체 규칙의 대부분 차지 (정상)
