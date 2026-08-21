# 📰 Team Sync - Backend (sync_BE)

> **유사 뉴스 클러스터링 및 생성형 AI 기반 맞춤형 뉴스 요약·제공 서비스**

---

## 📌 1. 프로젝트 소개
중복되거나 파편화된 기사를 일일이 확인할 필요 없이, 유사한 주제의 기사들을 군집화(Clustering)하고 이를 기반으로 통합 요약 기사를 생성하여 제공하는 AI 뉴스 콘텐츠 자동화 플랫폼의 백엔드 API 서버입니다.

* **배포 도메인:** `https://newsync.kr`

---

## 🛠 2. 기술 스택 (Tech Stack)

* **Backend Framework:** Java 17, Spring Boot, Spring Data JPA, Spring Security
* **Database:** 
  * Relational: MySQL (회원, 댓글, 설정 데이터 관리)
  * NoSQL: MongoDB (뉴스 원문 데이터 및 클러스터링 요약본)
  * Cache & Token Store: Redis (JWT Blacklist 및 인증 토큰 관리)
* **Authentication:** JWT (JSON Web Token), Mail Verification
* **Infra & DevOps:** AWS EC2, Docker, GitHub Actions (CI/CD)
* **Documentation & Tools:** Swagger, Gradle

---

## ✨ 3. 주요 기능 (Key Features)

* **뉴스 군집화 및 AI 요약 기사 제공 (`NewsService`)**: 클러스터링된 다분야 뉴스 데이터 및 통합 요약본 조회
* **기사 기반 AI 채팅 및 RAG 지원 (`ChatService`)**: 뉴스 데이터 기반 실시간 문답 API
* **TTS 음성 변환 (`TTSService`)**: 요약 뉴스 음성 합성 및 스트리밍 지원
* **회원 관리 & 보안 (`UserService`, `TokenService`)**: 이메일 인증, 회원가입/로그인, Redis 기반 JWT 블랙리스트 처리
* **뉴스 댓글 및 활동 로그 (`CommentService`)**: 유저 인터랙션 및 읽기 로그 기록

---

## 🏗 4. 시스템 아키텍처 & 디렉토리 구조

```text
src/main/java/com/sync_BE/
├── api/             # 공통 API Response 규격 및 Global Exception Handler
├── config/          # Security, Redis, CORS, Swagger, TTS 설정
├── converter/       # Entity <-> DTO 변환 레이어
├── domain/          # JPA 엔티티 및 도메인 객체
├── repository/      # JPA / MongoDB 리포지토리
├── security/        # CustomUserDetails, JWT Provider & Filter
├── service/         # 비즈니스 로직 (News, User, Token, Mail, TTS 등)
└── web/             # Controller 및 DTO 계층

```

---


## 📜 5. 커밋 컨벤션 (Git Commit Convention)

| Prefix | Description |
| :--- | :--- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 수정 및 추가 |
| `style` | 코드 포맷팅, 세미콜론 누락 등 (비즈니스 로직 영향 없음) |
| `refactor` | 코드 리팩토링 |
| `test` | 테스트 코드 작성 및 수정 |
| `cicd` | CI/CD 파이프라인 및 배포 설정 관련 수정 |
| `chore` | 빌드 업무, 패키지 매니저 수정 등 기타 작업 |
