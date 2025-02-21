# 🔐 Spring Boot JWT Authentication with Redis

**Spring Boot 기반의 JWT 인증 시스템 + Redis 기반 Refresh Token 관리**

- **Redis를 활용한 Refresh Token 관리 및 즉시 만료 처리**
- **JWT 기반 로그인, 인증, 로그아웃 구현**
- **Spring Security + JWT + Redis + MySQL 연동**
- **Docker Compose를 이용한 Redis, MySQL 컨테이너 설정 포함**

---

## 🛠️ **기술 스택**

- **Spring Boot 3.4.2**
- **Spring Security**
- **JWT (io.jsonwebtoken)**
- **Redis**
- **Spring Data JPA**
- **MySQL 8.0**
- **Gradle**
- **Docker & Docker Compose**

---

## 📌 **프로젝트 개요**

이 프로젝트는 **Spring Boot Security + JWT + Redis + MySQL** 기반의 인증 시스템입니다.  
Refresh Token을 **Redis에 저장**하여, 로그아웃 시 즉시 만료 처리하는 방식으로 보안을 강화합니다.

🔹 **기능 목록**

- ✅ JWT 기반 로그인 및 토큰 발급 (Access Token & Refresh Token)
- ✅ Refresh Token을 Redis에 저장 및 관리
- ✅ 로그아웃 시 Redis에서 Refresh Token 제거
- ✅ Spring Security를 통한 API 접근 제어
- ✅ MySQL을 통한 사용자 정보 저장
- ✅ Docker Compose로 Redis & MySQL 컨테이너 실행

---

## 📂 **프로젝트 폴더 구조**

```plaintext
├─ jwt
│  ├─ constants              # JWT 관련 상수 정의
│  │      ClaimKeys.java
│  │      TokenType.java
│  │
│  ├─ controller             # JWT 관련 API 컨트롤러
│  │      JwtController.java
│  │
│  ├─ dto                    # JWT DTO (토큰 발급, 갱신 관련)
│  │      ReissueDto.java
│  │      TokenDto.java
│  │
│  ├─ filters                # JWT 인증 필터
│  │      JwtAuthenticationFilter.java
│  │      JwtLoginFilter.java
│  │
│  ├─ helper                 # JWT 생성 및 검증 유틸리티
│  │      JwtHelper.java
│  │
│  └─ service                # JWT 서비스 로직
│         JwtService.java
│
├─ login
│  ├─ controller             # 로그인/로그아웃 API 컨트롤러
│  │      LoginController.java
│  │
│  ├─ dto                    # 로그인 요청 및 응답 DTO
│  │      LoginReqDto.java
│  │      LoginRespDto.java
│  │      LogoutReqDto.java
│  │
│  └─ service                # 로그인 서비스 로직
│         LoginService.java
│
├─ redis
│  ├─ config                 # Redis 설정
│  │      RedisConfig.java
│  │
│  ├─ entity                 # Redis에서 관리하는 Refresh Token 엔티티
│  │      RefreshTokenEntity.java
│  │
│  └─ repository             # Redis 데이터 관리 Repository
│         RefreshTokenRepository.java
│
├─ sample
│  └─ controller             # 샘플 API 컨트롤러
│         SampleController.java
│
├─ security
│  ├─ config                 # Spring Security 설정
│  │      SecurityConfig.java
│  │
│  └─ entryPoint             # 인증 실패 처리 엔트리 포인트
│         CustomAuthenticationEntryPoint.java
│
└─ user
    ├─ entity                # 사용자 엔티티
    │      UserEntity.java
    │
    ├─ repository            # 사용자 Repository
    │      UserRepository.java
    │
    ├─ service               # 사용자 서비스 로직
    │      CustomUserDetailsService.java
    │      UserService.java
    │
    └─ vo                    # 사용자 VO 객체
    │      CustomUserDetails.java
```

---

##  **Docker Compose 설정**

이 프로젝트는 **Redis & MySQL을 Docker 컨테이너에서 실행**할 수 있습니다.

### **🔹 `docker-compose.yml`**

```yaml
yaml

version: '3.8'

services:
  redis:
    image: redis:7.2.4
    container_name: redis
    restart: always
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    environment:
      - REDIS_PASSWORD=test@1234
    command: ["redis-server", "--requirepass", "test@1234"]

  mysql:
    image: mysql:8.0
    container_name: mysql
    restart: always
    ports:
      - "33062:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: test_db
      MYSQL_USER: test
      MYSQL_PASSWORD: test@1234
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  redis_data:
    driver: local
  mysql_data:
    driver: local

복사편집
```

---

##  **프로젝트 실행 방법**

### 1️⃣ **Docker Compose로 Redis & MySQL 실행**

```
docker-compose up -d
```

### 2️⃣ **프로젝트 실행 (Gradle)**

```
./gradlew bootRun
```

