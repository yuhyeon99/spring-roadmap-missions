# Spring MVC 기본 구조 설계

이 문서는 Spring 입문 미션(`mission-01-spring-intro`)의 두 번째 테스크(`task-02-mvc`)에서 MVC 패턴을 사용해 컨트롤러, 서비스, 리포지토리 계층을 구성한 과정을 정리합니다. 가상의 회원 정보를 In-Memory 리포지토리에 저장하고, REST API로 생성/조회/목록 기능을 제공합니다.

## 1. MVC 구조 설계

- **패키지 구조**: `com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc` 아래에 `controller`, `service`, `repository`, `domain`, `dto`로 계층을 분리했습니다.
- **모델**: `Member` 엔티티는 `id`, `name`, `email`을 가집니다.
- **리포지토리**: `InMemoryMemberRepository`로 간단한 저장소를 구현하고, `MemberRepository` 인터페이스로 추상화했습니다.
- **서비스**: `MemberService`가 비즈니스 로직(생성, 조회, 목록)을 담당합니다.
- **컨트롤러**: `MemberController`가 REST API 엔드포인트를 노출합니다.

## 2. 레이어별 구현

### 컨트롤러

**파일: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/controller/MemberController.java`**

```java
@RestController
@RequestMapping("/mission01/task02/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse createMember(@RequestBody MemberRequest request) {
        Member created = memberService.createMember(request.getName(), request.getEmail());
        return MemberResponse.from(created);
    }

    @GetMapping
    public List<MemberResponse> listMembers() {
        return memberService.listMembers().stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public MemberResponse findMember(@PathVariable Long id) {
        Member member = memberService.findMember(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        return MemberResponse.from(member);
    }
}
```

### 서비스

**파일: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/service/MemberService.java`**

```java
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member createMember(String name, String email) {
        Member member = new Member(name, email);
        return memberRepository.save(member);
    }

    public List<Member> listMembers() {
        return memberRepository.findAll();
    }

    public Optional<Member> findMember(Long id) {
        return memberRepository.findById(id);
    }
}
```

### 리포지토리

**파일: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/repository/InMemoryMemberRepository.java`**

```java
public class InMemoryMemberRepository implements MemberRepository {

    private final ConcurrentMap<Long, Member> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public Member save(Member member) {
        Long id = member.getId();
        if (id == null) {
            id = sequence.incrementAndGet();
            member.setId(id);
        }
        store.put(id, member);
        return member;
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }
}
```

### 단위 테스트

**파일: `src/test/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/MemberServiceTest.java`**

```java
class MemberServiceTest {

    private final MemberService memberService = new MemberService(new InMemoryMemberRepository());

    @Test
    void createAndFindMember() {
        Member created = memberService.createMember("Alice", "alice@example.com");

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Alice");

        Member found = memberService.findMember(created.getId()).orElseThrow();
        assertThat(found.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void listMembers() {
        memberService.createMember("Bob", "bob@example.com");
        memberService.createMember("Carol", "carol@example.com");

        List<Member> members = memberService.listMembers();
        assertThat(members).hasSize(2);
    }
}
```

## 3. 실행 및 테스트 방법

### 애플리케이션 실행

```bash
./gradlew bootRun
```

애플리케이션이 기동되면 기본 포트 `8080`에서 아래 엔드포인트를 확인할 수 있습니다.

### API 호출 예시 (`/mission01/task02/members`)

1. 회원 생성 (POST)
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com"}' \
  http://localhost:8080/mission01/task02/members
```

2. 회원 전체 조회 (GET)
```bash
curl http://localhost:8080/mission01/task02/members
```

3. 특정 회원 조회 (GET)
```bash
curl http://localhost:8080/mission01/task02/members/1
```

### 테스트 실행

```bash
./gradlew test --tests "*task02_mvc*"
```

## 학습 내용

- **MVC 분리**: 컨트롤러는 HTTP 요청/응답을 다루고, 서비스는 비즈니스 로직을, 리포지토리는 데이터 접근을 담당합니다. 역할을 나누면 변경 영향 범위가 작아집니다.
- **의존성 주입(DI)**: `@RestController`, `@Service`, `@Repository`로 Bean을 등록하고 생성자 주입으로 연결했습니다. 의존 관계가 코드에 명시되고 테스트 시 다른 구현체로 교체하기 쉽습니다.
- **In-Memory 리포지토리**: DB 없이도 저장소를 흉내 내기 위해 `ConcurrentHashMap`과 `AtomicLong`을 사용했습니다. 실제 환경에서는 JPA 리포지토리 등으로 교체하면 서비스/컨트롤러 코드는 그대로 재사용할 수 있습니다.
- **DTO 사용**: 요청/응답을 위한 `MemberRequest`, `MemberResponse`를 분리하여 도메인 모델(`Member`)을 보호하고, API 스펙을 명확히 합니다.
- **예외 처리**: 조회 결과가 없으면 `ResponseStatusException`으로 404를 반환해 API 클라이언트가 상황을 알 수 있게 했습니다.
