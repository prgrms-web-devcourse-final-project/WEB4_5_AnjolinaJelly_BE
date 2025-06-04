# ⚡찌릿(https://zzirit.vercel.app/sign-in)

> 한정된 시간, 한정된 수량으로 만나는 특가 전자기기  
> 실시간 트래픽 대응과 상용 수준의 커머스 플랫폼을 경험하는 **기술 중심 프로젝트**

---

## 📝 프로젝트 개요

### 🎯 주제
**시간 한정 타임 딜**을 지원하는 전자기기 구매 플랫폼

### 🧠 기획 의도
- 희소성과 긴급성을 기반으로 한 마케팅 효과 체험
- 상품 등록, 재고 관리, 타임 딜 이벤트 등 **운영 중심 기능** 구현
- **트래픽 급증 대응** 아키텍처 설계 및 성능 개선 실습
- 캐싱, 비동기 메시징, 대규모 사용자 처리 등 **실무형 기술 습득**

### 💡 기대 효과
- 실시간 처리 및 고부하 트래픽에 강한 **아키텍처 설계 경험**
- **캐싱, 메시지 큐, 동시성 처리** 중심 백엔드 실무 역량 강화
- 팀 협업을 통한 **문서화·프로젝트 관리·역할 분담 경험**
- 유사한 기술적 요구 사항이 있는 프로젝트 설계 및 구현에 응용 가능

---

## 📸 시연 영상

[![프로젝트 소개 영상](https://img.youtube.com/vi/yxeq4f6ZK6g/0.jpg)](https://youtu.be/yxeq4f6ZK6g)

---

## 🚀 핵심 기능

- 🔍 상품 검색 및 조회
- ⏱ 타임 딜 기반 특가 상품 제공
- 💳 주문·결제·환불 프로세스
- 🛒 장바구니
- 🔐 로그인, 회원가입, 마이 페이지
- 🛠 상품 및 타임 딜 관리

---

## ⚙️ 기술 스택

| 항목 | 사용 기술 |
|------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.5 |
| Build Tool | Gradle + Jacoco |
| ORM & Data access | JPA, Hibernate, Spring Data JPA, QueryDSL 5 (Jakarta) |
| Database | MySQL, Redis (Docker), S3, H2 |
| Messaging | RabbitMQ |
| 인증 & 인가 | Spring Security, OAuth2 (Google/Kakao/Naver), JWT |
| API 문서화 | Spring REST Docs, Swagger UI (OpenAPI 3) |
| 모니터링 & 부하 테스트 | Actuator, Prometheus, nGrinder |
| 테스트 | JUnit5, RestAssured, Testcontainers |
| Frontend |	Next.js 15, TypeScript, MUI, Tailwind CSS |
| Infra & DevOps | AWS EC2, S3, GitHub Actions, Docker |
| 협업 도구 | GitHub Projects, Figma, Notion, Slack, Discord |

---

## 🏗️ 아키텍처

![아키텍처](https://github.com/user-attachments/assets/f9b4794a-de2d-4f70-964e-cd8df458e3f8)

---

## 🔧 엔티티 다이어그램

![ERD](https://github.com/user-attachments/assets/028ffd45-1bf3-4040-9ceb-75d80bd5682e)

---

## 📆 프로젝트 일정

| 단계 | 기간 | 주요 활동 |
|------|------|-----------|
| 기획 | 4/21 ~ 4/29 | 주제 선정, 기획안 작성 |
| 설계 | 4/29 ~ 5/3 | 와이어프레임, ERD, API 명세서 작성 |
| 개발 1차 | 5/3 ~ 5/13 | 기능 개발 및 테스트 |
| 배포 설정 | 4/21 ~ 6/1 | CI/CD 및 서버 배포 |
| 고도화 | 5/14 ~ 6/1 | 기능 개선 및 고도화 |
| **총 개발 기간** | **4/21 ~ 6/1 (6주)** |  |

---

## 👥 팀 구성 및 역할 

| 항목 | [강웅빈](https://github.com/Woongbin06) | [김지수](https://github.com/j1suk1m) | [김채은](https://github.com/huipadyam) |
|:------:|:------------------:|:-------------------:|:--------------------:|
| 역할 | Backend Leader | Project Owner | AWS Administrator |
| 담당 | <ul><li>상품 조회 및 검색</li><li>쿼리 카운터 개발</li><li>커스텀 응답 구조 자동 변환</li><li>CI/CD 구축</li><li>모니터링 시스템 구축</li><li>서버 관리</li><li>Rest Docs + Swagger UI 설정</li><li>상품 수정 동시성 처리</li></ul> | <ul><li>팀 리딩 및 일정 관리</li><li>프론트엔드 커뮤니케이션 주도</li><li>주문 내역 조회</li><li>주문 취소 및 환불</li><li>주문 상태 변경 스케줄링</li><li>상품 수정 동시성 처리</li><li>타임 딜 상태 변경 고도화</li></ul> | <ul><li>상품 조회, 등록, 수정, 삭제</li><li>CD 구축</li><li>타임 딜 상태 변경 고도화</li><li>이미지 업로드 고도화</li><li>AWS 관리</li><li>AWS 사용 현황 문서화</li></ul> |

---

| 항목 | [소진영](https://github.com/Jinyoung0718) | [이현지](https://github.com/cloudmato) | [한지성](https://github.com/Hanjise0ng) |
|:------:|:------------------:|:-------------------:|:--------------------:|
| 역할 | Team Member | Team Member | Team Member |
| 담당 | <ul><li>로그인 및 회원가입</li><li>시큐리티 설정</li><li>결제 프로세스</li><li>마이 페이지</li><li>공통 예외 처리</li><li>인증 코드 이메일 발송</li><li>S3 전송 비동기 처리</li><li>분산 락 AOP 적용</li></ul> | <ul><li>타임 딜 생성</li><li>타임 딜 조회 및 검색</li><li>타임 딜 상태 변경 고도화</li><li>상품 조회 쿼리 튜닝</li><li>문서화 및 발표 자료 준비</li></ul> | <ul><li>장바구니</li><li>상품 이미지 업로드</li><li>상품 조회 쿼리 튜닝</li><li>문서화 및 발표 자료 준비</li><li>이슈 템플릿 관리</li></ul> |


---

## 📦 실행 방법

```bash
# 1. 리포지토리 클론
git clone https://github.com/prgrms-web-devcourse-final-project/WEB4_5_AnjolinaJelly_BE.git

# 2. `.env` 파일 작성 (루트 디렉토리)
MYSQL_DEV_PASSWORD=
MYSQL_TEST_PASSWORD=
...

# 3. `application-secret.yml` 파일 작성 (src/main/resources)
spring:
  datasource:
    username: ...
    password: ...
  rabbitmq:
    username: ...
    password: ...
  jwt:
    secret: ...
...

# 4. 도커 실행
docker-compose -f docker-compose.dev.yaml up -d
```
