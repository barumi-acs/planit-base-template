# 🚀 PlanIt Base Template

본 레포지토리는 PlanIt MSA 프로젝트의 **공통 기반(Base Template)** 입니다.
모든 마이크로서비스(User, Schedule 등)는 이 템플릿을 복사하여 개발을 시작합니다. 
아래의 **[팀원 필수 개발 규칙]** 을 반드시 숙지하고 개발해 주시기 바랍니다.

---

## 📚 1. 기술 스택 및 라이브러리 (Tech Stack)

공통으로 세팅된 라이브러리 목록입니다. 임의로 버전을 변경하거나 외부 라이브러리를 추가하기 전 반드시 팀과 논의하세요.

* **Spring Web**: REST API 통신 및 내장 톰캣 서버 엔진
* **Spring Data JPA**: SQL 작성 없이 DB를 조작하는 자바 표준 기술
* **MariaDB Driver**: 자바와 MariaDB를 연결하는 전용 통신 케이블
* **Lombok**: Getter, 생성자, 로그 변수 등 반복 코드를 줄여주는 자동화 툴
* **Spring Boot Actuator**: 서버 생존 여부(UP/DOWN)를 확인하는 헬스체크 기능
* **Micrometer Tracing**: 여러 서버를 거치는 로그를 하나로 묶어주는 추적기 (Trace ID 자동 부여)

---

## 📦 2. 공통 응답 및 에러 규격

### ✅ 공통 응답 구조 (ApiResponse)
모든 API 응답은 `ApiResponse` 객체로 감싸서 반환해야 합니다.

```json
{
  "code": "C2001",
  "message": "성공",
  "data": { "userId": 1, "name": "test" },
  "timestamp": "2026-02-23T10:30:00"
}
```
## 🚨 공통 에러 규격 (ErrorCode & CustomException)
에러 발생 시 **throw new CustomException(ErrorCode.코드)** 형태로 던지면 **GlobalExceptionHandler**가 가로채서 공통 포맷으로 변환합니다.
### [ 도메인별 에러 코드 규칙 ]
* **C(Common)** : C4001 (파라미터 누락), C4011 (토큰 만료), C5001 (서버 에러)
* **U(Common)** : U4001 (중복된 아이디), U4041 (유저 찾을 수 없음)
* **S(Common)** : S4031 (권한 없는 할 일 접근), S4041 (할 일 없음)
* **AI(Common)** : AI5001 (Bedrock 타임아웃), AI4001 (잘못된 프롬프트)
* **IS(Common)** : IS4041 (분석할 통계 데이터 부족)

---

## ⏰ 3. DB 시간 자동화 (JPA Auditing)
**[ 규칙 ]** 모든 엔티티(Entity)는 무조건 **extends BaseTimeEntity**를 상속받아야 합니다.
상속 시 DB 테이블에 **created_at(생성일)**, **updated_at(수정일)** 컬럼이 자동으로 생성되고 기록됩니다.
```java
@Entity
@Getter
@Table(name = "users")
public class UserEntity extends BaseTimeEntity { // 👈 필수 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // createdAt, updatedAt 변수를 직접 적을 필요 없음!
}
```

---

## 🏥 4. 헬스체크 및 모니터링 (Actuator)
서버 생존 여부 확인을 위한 Actuator가 연동되어 있습니다.
* **planit-base-template/build.gradle**에 라이브러리 추가
```
dependencies {
    ......
	  //헬스체크 및 모니터링 (Actuator)
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    ......
}
```
* **planit-base-template/src/main/resources/application.yml**에 추가
```yaml
# /actuator/health 로 접속하면 서버 생존 여부 반환
management:
  endpoints:
    web:
      base-path: /api/v1/base/actuator
      exposure:
        include: health, info
```
* **접속 경로** : http://localhost:8080/api/v1/base/actuator/health
* **정상 응답** : {"status":"UP"}

---

## 📝 5. 로깅 및 트레이싱 (Micrometer & Logback)
SA 환경에서 요청을 추적하기 위해 모든 로그에는 **고유 식별자(TraceID)** 가 자동으로 부여되며, **logback-spring.xml**을 통해 포맷이 통일되어 있습니다.
* **planit-base-template/build.gradle**에 라이브러리 추가
```

dependencies {
    ......
    //로깅 및 분산 추적 (Micrometer Tracing) - 로그에 [TraceID] 자동 부여
    implementation 'io.micrometer:micrometer-tracing-bridge-brave'
    ......
}
```
### [ 로깅 규칙 ]
* **System.out.println** 절대 금지 ❌
* 클래스 상단에 **@Slf4j**를 붙이고 **log.info()**, **log.error()**를 사용 ⭕
* 변수 바인딩 시 **+** 연산자 대신 **{}** 사용
### [ 사용 예시 ]
```java
@Slf4j
@RestController
public class UserController {
    @PostMapping("/signup")
    public String signup(String userId) {
        log.info("회원가입 요청 들어옴. 요청 아이디: {}", userId);
        return "OK";
    }
}
```
### [ 로그 출력 예시 (TraceID 포함) ]
```bash
2026-02-23 16:30:00 INFO  [a1b2c3d4e5f6g7h8] [http-nio-8080-exec-1] c.p.UserController : 회원가입 요청 들어옴. 요청 아이디: abc
```

---

## 🐳 6. 빌드 및 배포 환경 (Dockerfile)
각 서비스 컨테이너화를 위한 공통 **Dockerfile**이 프로젝트 루트에 구성되어 있습니다.
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/*SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```
