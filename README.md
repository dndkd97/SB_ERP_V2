# SBerp v2 (spring-breeze-erp-v2)

> Team Spring Breeze의 전사적 자원 관리(ERP) 시스템 — 2차 프로젝트
> 1차 프로젝트(JSP · MySQL)를 Spring Boot · Thymeleaf · Oracle 18c · AI/API 기반으로 리팩토링 및 고도화

## 📌 프로젝트 개요

| 항목 | 내용 |
|---|---|
| 프로젝트명 | SBerp v2 (spring-breeze-erp-v2) |
| 팀명 | Spring Breeze (Team 04) |
| 개발 기간 | 2026.07.02 ~ 2026.07.15 (14일) |
| 팀 인원 | 4명 |
| 도메인 | Enterprise Resource Planning |
| 핵심 도메인 | 6개 (회사 · 부서 · 자원 예약 / 인사 · 평가 · 권한 / 전자결재 / 프로젝트 · 태스크 · 공지) |
| 신규 기능 | 8종 이상 (AI/외부 API 연동) |
| 대상 사용자 | 중소 규모 기업 관리자 / 임직원 |

### 기획 의도
- **REAL**: 결재 · 인사 · 자원 · 프로젝트 등 실무에서 매일 마주치는 도메인 구조를 학습
- **COMPLEX**: 사원 · 부서 정보를 공유하는 4개 파트를 처음부터 협업 구조로 설계
- **FULLSTACK**: 백엔드 · DB · 뷰 · AI · 외부 API를 하나의 서비스에서 통합 구현

## 🛠 기술 스택
<table>
<tr>
<td valign="top" width="50%">

**Backend**
![Java](https://img.shields.io/badge/Java%2017-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203.5.16-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![MyBatis](https://img.shields.io/badge/MyBatis-000000?style=flat-square)
> Spring Security로 인증 · 인가, MyBatis로 SQL Mapper 관리

**Frontend (View Layer)**
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=flat-square&logo=thymeleaf&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap%205-7952B3?style=flat-square&logo=bootstrap&logoColor=white)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=flat-square&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=flat-square&logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=flat-square&logo=javascript&logoColor=black)
> SSR 기반, thymeleaf-layout-dialect로 레이아웃 관리

**Database**
![Oracle](https://img.shields.io/badge/Oracle%2018c-F80000?style=flat-square&logo=oracle&logoColor=white)

</td>
<td valign="top" width="50%">

**External API / AI**
![OpenAI](https://img.shields.io/badge/GPT--4o--mini-412991?style=flat-square&logo=openai&logoColor=white)
![Naver](https://img.shields.io/badge/Naver%20OCR-03C75A?style=flat-square&logo=naver&logoColor=white)
![data.go.kr](https://img.shields.io/badge/국세청%20공공데이터-0B6E4F?style=flat-square)
![Discord Webhook](https://img.shields.io/badge/Discord%20Webhook-5865F2?style=flat-square&logo=discord&logoColor=white)
![SMTP](https://img.shields.io/badge/SMTP%20Mail-EA4335?style=flat-square&logo=gmail&logoColor=white)
![Google Docs](https://img.shields.io/badge/Google%20Docs%20API-4285F4?style=flat-square&logo=googledocs&logoColor=white)
![PDFBox](https://img.shields.io/badge/Apache%20PDFBox-D22128?style=flat-square&logo=apache&logoColor=white)
> GPT: 결재 양식 생성 · 평가 리포트 요약 · 부서 이관 추천 · 태스크 리스크 판정
> Naver OCR: 사업자등록증 인식 · 국세청 API: 사업자 진위확인
> Discord: 실시간 알림 · Google Docs: 주간 보고서 자동 저장 · PDFBox: 개인 리포트 PDF

**협업 도구**
![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white)
![Discord](https://img.shields.io/badge/Discord-5865F2?style=flat-square&logo=discord&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=flat-square&logo=notion&logoColor=white)

</td>
</tr>
</table>

## 👥 팀원 역할 분담

| 담당자 | 역할 | 담당 도메인 |
|---|---|---|
| 최윤정 (팀장) | 회사 · 부서 · 자원 예약 | 사업자등록증 OCR 등록, 국세청 API 검증, AI 부서 이관 추천, 자원 예약 동시성 제어 |
| 정수정 (팀원) | 인사 · 평가 · 권한 | 온보딩 메일 자동화, AI 평가 리포트 요약 |
| 김주엽 (팀원) | 전자결재 | AI 결재 양식 생성, 동적 스키마 렌더링, 순차 승인 시스템 |
| 최다영 (팀원) | 프로젝트 · 태스크 · 공지 | 재귀 CTE 기반 태스크 트리, AI 리스크 판정, 주간 보고서 자동화 |

## 🗂 ERD (Entity Relationship Diagram)

`company`를 중심으로 통합된 멀티 테넌시 구조입니다.

- **ORGANIZATION / HR**: `company`, `department`, `employee`, `emp_position`, `authority`, `emp_auth`, `dept_transfer_log`
- **APPROVAL**: `appr_form`, `appr_doc`, `appr_line`
- **PROJECT / TASK / NOTICE**: `project`, `project_member`, `task`, `notice`
- **RESOURCE**: `com_resource`, `reservation`
- **EVALUATION**: `evaluation_period`, `performance_evaluation`, `evaluation_ai_report`

## 🔄 시스템 구성도 (Layered Architecture)

```
[ L1 CLIENT ] → [ L2 PRESENTATION ] → [ L3 APPLICATION ] → [ L4 PERSISTENCE ] → [ L5 EXTERNAL ]
   Browser         Thymeleaf(View)         @Service            MyBatis            OpenAI
   Web UI          Controller(Spring MVC)  Spring Security      Oracle 18c         Discord · OCR
```

1. **CLIENT**: 브라우저에서 요청 발생
2. **PRESENTATION**: Thymeleaf 뷰 렌더링, Spring MVC 컨트롤러가 요청 매핑
3. **APPLICATION**: Service 계층에서 비즈니스 로직 처리, Spring Security로 인가 검증
4. **PERSISTENCE**: MyBatis Mapper를 통해 Oracle 18c와 통신
5. **EXTERNAL**: 필요 시 OpenAI, Discord, Naver OCR, 국세청 API 등 외부 서비스 연동

## 📦 모듈별 주요 기능

### 01. 회사 · 부서 · 자원 예약 (@최윤정)
- **사업자등록증 OCR 등록**: Naver OCR로 이미지 1장에서 사업자번호 · 상호명 · 대표자명 · 개업일자 · 업종 등 6개 필드 자동 인식, 인식 실패 시 수동 입력으로 전환하여 등록 소요시간을 약 70% 단축
- **국세청 실시간 검증**: 사업자번호 · 대표자명 · 개업일자 · 휴폐업 여부를 공공데이터 API로 3중 검증, 타임아웃 발생 시 Fallback 응답으로 서버 스레드 보호
- **AI 부서 이관 추천**: 부서 삭제 시 유사 부서를 AI가 추천, 관리자가 최종 승인하는 구조. 전원 이관 완료 전에는 삭제 비활성화, 트랜잭션 실패 시 전체 롤백
- **자원 예약 동시성 제어**: 실시간 잔여 수량 조회와 기간 겹침(Overlap) 검증으로 동시 예약 충돌 방지

### 02. 인사 · 평가 · 권한 (@정수정)
- **온보딩 메일 자동화**: 사원 등록 즉시 온보딩 메일, 입사 3일 뒤 적응 확인 메일을 자동 발송. 발송 실패 시 재확인 후 재발송
- **AI 평가 리포트 요약**: 평가 마감 시 OpenAI가 사원별 코멘트를 요약. 평가 진행 단계를 6단계로 세분화해 실패 상태를 명시적으로 관리하고, 이상 응답 시 안전한 기본값으로 대체

### 03. 전자결재 (@김주엽)
- **AI 결재 양식 생성**: 관리자가 원하는 양식을 문장으로 설명하면 GPT JSON mode로 구조화된 필드를 생성
- **동적 스키마 렌더링**: 생성된 스키마를 텍스트 · 날짜 · 셀렉트 입력 폼으로 실시간 렌더링, 필수 필드는 프론트엔드에서 실시간 검증
- **버전 관리 · 순차 승인**: 양식 수정/삭제 시 버전을 분리(`for_version`)하고 소프트 삭제(`is_deleted`)로 과거 문서를 보존. 결재선은 직급 순서 기반으로 자동 필터링되며 동시 승인이 구조적으로 차단됨

### 04. 프로젝트 · 태스크 · 공지 (@최다영)
- **재귀 CTE 태스크 트리**: Oracle 재귀 CTE(`START WITH ... CONNECT BY PRIOR`)로 태스크 트리 깊이 제한 없이 한 번에 조회, 순환 참조는 CHECK 제약과 자바 검증으로 이중 방어
- **AI 리스크 판정**: 정량 지표를 종합해 AI가 HIGH · MID · LOW로 리스크를 판정하고, HIGH 등급은 Discord Webhook으로 실시간 알림
- **주간 보고서 자동화**: 프로젝트 데이터를 집계해 AI가 요약, 팀장용은 Google Docs에 자동 저장(스케줄러), 개인용은 PDF로 즉시 다운로드

## 📅 개발 일정 (14일)

| 단계 | 기간 | 내용 |
|---|---|---|
| Phase 1 | 07.02 ~ 07.07 | 리팩토링 — Spring Boot · Thymeleaf · Oracle 마이그레이션 |
| Phase 2 | 07.08 ~ 07.12 | 신규 기능 — 팀원별 신규 페이지 · AI/API 통합 (M1: 1차 개발 완료) |
| Phase 3 | 07.13 ~ 07.15 | QA & 시연 — 통합 테스트 · 리허설 · 시연 준비 (M2: 2차 개발 완료) |

## ✅ 회고

- 기술 적용 자체보다 사용자에게 실제로 필요한 기능을 고민하는 과정이 더 중요하다는 점을 확인했습니다.
- 양식 수정 · 삭제가 기존 문서에 미치는 영향을 고려해 소프트 삭제와 버전 관리를 설계하는 습관이 생겼습니다.
- 하나의 기능 변경이 연관 데이터 전체에 미치는 영향을 먼저 검토하는 것의 중요성을 배웠습니다.
- 단순해 보이는 CRUD에도 FK 정합성 · 트랜잭션 · 예외 처리 등 다양한 안전장치가 필요하다는 것을 체감했습니다.

## 📁 주요 설정 / 기술 포인트

- `pom.xml` — Spring Boot 3.5.16 · Java 17, MyBatis, Spring Security, Oracle JDBC(ojdbc11), Thymeleaf Layout Dialect, OAuth2 Client, PDFBox, jsoup 등
- MyBatis Mapper XML 기반 SQL 관리
- Spring Security 기반 인증 · 인가, 세션 관리
- Oracle 18c 재귀 CTE, 복합 인덱스 등을 활용한 성능 최적화

## 📄 라이선스

본 프로젝트는 Spring Breeze 팀의 교육용 협업 프로젝트입니다.
