# HTTP 메서드의 실용적 활용

`mission-03-http-web-basic`의 `task-07-http-methods-practical-usage` 보고서입니다. CRUD 기능을 구현하는 예제를 기준으로 GET, POST, PUT, DELETE가 실제로 어떻게 활용되는지 정리하고, 각 요청이 어떤 구조를 가지는지 설명했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-03-http-web-basic` / `task-07-http-methods-practical-usage`
- 목표:
  - CRUD 동작을 HTTP 메서드와 연결해 이해한다.
  - 각 HTTP 메서드가 요청 라인, 헤더, 본문에서 어떤 차이를 가지는지 설명한다.
  - 기존 노트 CRUD 예제를 기준으로 실무적인 메서드 사용 사례를 문서로 정리한다.

## 2. 주요 해설

### 2.1 CRUD와 HTTP 메서드 매핑

- 이번 문서는 기존 노트 예제 API인 `/mission03/task01/notes`를 기준으로 설명합니다.
- 이 예제에서는 조회(Read)에 `GET`, 생성(Create)에 `POST`, 수정(Update)에 `PUT`, 삭제(Delete)에 `DELETE`를 사용합니다.
- 리소스 중심 설계에서는 "행동 이름"보다 "리소스 + HTTP 메서드 조합"으로 의도를 표현하는 편이 더 자연스럽습니다.

| CRUD 동작 | HTTP 메서드 | 예시 경로 | 왜 이렇게 매핑하는가 |
|---|---|---|---|
| Create | `POST` | `/mission03/task01/notes` | 서버가 새 리소스를 만들고 새 ID를 결정하기 때문입니다. |
| Read List | `GET` | `/mission03/task01/notes` | 목록 조회는 서버 상태를 바꾸지 않는 읽기 작업이기 때문입니다. |
| Read One | `GET` | `/mission03/task01/notes/{id}` | 특정 리소스를 식별해서 조회하기 때문입니다. |
| Update | `PUT` | `/mission03/task01/notes/{id}` | 지정한 리소스 상태를 새 표현으로 대체하는 데 적합하기 때문입니다. |
| Delete | `DELETE` | `/mission03/task01/notes/{id}` | 지정한 리소스를 제거하겠다는 의도를 직접적으로 표현하기 때문입니다. |

### 2.2 HTTP 메서드 사용 사례표

| 메서드 | 사용 사례 | 대표 요청 구조 | 응답 핵심 포인트 |
|---|---|---|---|
| `GET` | 노트 목록 조회, 단건 조회 | URI에 경로 변수나 쿼리 문자열을 실어 조회 | 보통 `200 OK`, 본문에 조회 결과 반환 |
| `POST` | 새 노트 생성 | 요청 본문(JSON)에 생성할 데이터 전달 | 보통 `201 Created`, 새 리소스와 `Location` 헤더 반환 가능 |
| `PUT` | 특정 ID 노트 전체 교체 | 대상 ID는 경로에, 새 상태는 본문에 전달 | 보통 `200 OK`, 동일 요청 반복 시 멱등성 기대 |
| `DELETE` | 특정 노트 삭제 | 대상 ID를 경로로 지정 | 보통 `204 No Content`, 본문 없이 성공 처리 |

### 2.3 요청 구조 설명

#### GET 요청 예시: 목록 조회

```http
GET /mission03/task01/notes HTTP/1.1
Host: localhost:8080
Accept: application/json
User-Agent: HeaderStudyClient/1.0
```

- 요청 라인에 메서드와 경로가 들어갑니다.
- 조회 목적이므로 보통 본문은 사용하지 않습니다.
- `Accept` 헤더로 JSON 응답을 기대한다는 점을 표현할 수 있습니다.

#### GET 요청 예시: 단건 조회

```http
GET /mission03/task01/notes/1 HTTP/1.1
Host: localhost:8080
Accept: application/json
```

- 리소스 ID는 URI 경로에 포함됩니다.
- 서버는 `1`번 노트를 찾아 있으면 `200`, 없으면 `404`를 반환합니다.

#### POST 요청 예시: 생성

```http
POST /mission03/task01/notes HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: application/json

{
  "title": "HTTP",
  "content": "POST request"
}
```

- 생성할 데이터는 요청 본문에 들어갑니다.
- 서버가 본문을 JSON으로 해석할 수 있게 `Content-Type: application/json`이 필요합니다.
- 성공하면 새 리소스를 만들고 `201 Created`와 `Location` 헤더를 줄 수 있습니다.

#### PUT 요청 예시: 수정

```http
PUT /mission03/task01/notes/1 HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: application/json

{
  "title": "HTTP Updated",
  "content": "PUT request"
}
```

- 수정 대상 ID는 경로에, 바꿀 상태는 본문에 들어갑니다.
- 같은 요청을 여러 번 보내도 최종 상태가 같게 유지되는 점이 `PUT`의 핵심입니다.

#### DELETE 요청 예시: 삭제

```http
DELETE /mission03/task01/notes/1 HTTP/1.1
Host: localhost:8080
```

- 삭제 대상만 정확히 지정하면 되므로 보통 본문 없이 보냅니다.
- 성공 시 `204 No Content`처럼 본문 없는 응답이 자주 사용됩니다.

### 2.4 요청을 읽는 순서

1. 요청 라인에서 메서드와 대상 URI를 먼저 봅니다.
2. 조회인지 생성/수정인지에 따라 본문이 필요한지 판단합니다.
3. `Content-Type`, `Accept` 같은 헤더로 본문 형식과 기대 응답 형식을 확인합니다.
4. 경로 변수와 본문이 각각 어떤 책임을 가지는지 구분하면 CRUD 요청 구조가 더 쉽게 읽힙니다.

## 3. 새로 나온 개념 정리 + 참고 링크

- **HTTP 메서드와 리소스 중심 설계**
  - 핵심: URI는 리소스를 표현하고, HTTP 메서드는 그 리소스에 대해 어떤 동작을 할지 나타냅니다.
  - 왜 쓰는가: 같은 `/notes` 경로라도 `GET`, `POST`, `PUT`, `DELETE`에 따라 의도가 명확하게 구분되기 때문입니다.
  - 참고 링크:
    - RFC 9110 HTTP Semantics: https://datatracker.ietf.org/doc/html/rfc9110
    - MDN HTTP request methods: https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods

- **안전성과 멱등성**
  - 핵심: `GET`은 안전하고, `PUT`과 `DELETE`는 멱등성을 기대할 수 있으며, `POST`는 일반적으로 멱등하지 않습니다.
  - 왜 쓰는가: 재시도, 캐시, 장애 복구 같은 상황에서 메서드별 특성을 이해해야 올바르게 설계할 수 있기 때문입니다.
  - 참고 링크:
    - RFC 9110 HTTP Semantics: https://datatracker.ietf.org/doc/html/rfc9110
    - MDN Safe (HTTP Methods): https://developer.mozilla.org/en-US/docs/Glossary/Safe/HTTP

- **Content-Type과 요청 본문**
  - 핵심: `Content-Type`은 요청 본문이 어떤 형식인지 서버에 알려 줍니다.
  - 왜 쓰는가: 서버가 JSON인지 폼 데이터인지 구분해야 요청 값을 올바르게 파싱할 수 있기 때문입니다.
  - 참고 링크:
    - MDN Content-Type: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Type

- **Location 헤더와 생성 응답**
  - 핵심: 새 리소스를 생성한 뒤 `Location` 헤더로 해당 리소스 URI를 알려줄 수 있습니다.
  - 왜 쓰는가: 클라이언트가 생성 결과를 바로 추적하거나 이후 조회 요청을 보낼 수 있기 때문입니다.
  - 참고 링크:
    - MDN Location: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Location
    - RFC 9110 HTTP Semantics: https://datatracker.ietf.org/doc/html/rfc9110

## 4. 학습 내용

- CRUD 예제에서 HTTP 메서드를 연결해 보면, 메서드는 단순한 문법이 아니라 "서버 상태를 어떻게 다룰 것인가"를 표현하는 약속이라는 점이 보입니다.
- 조회는 URI와 헤더 중심으로 읽고, 생성·수정은 URI와 본문을 함께 읽는 식으로 접근하면 요청 구조가 훨씬 명확해집니다.
- `POST`는 생성, `PUT`은 지정한 상태로의 교체, `DELETE`는 제거라는 기본 역할을 지키면 API 의도가 더 잘 드러납니다.
- 실무에서는 URI를 동사처럼 만들기보다 리소스를 명사로 두고 HTTP 메서드로 동작을 구분하는 방식이 유지보수에 유리합니다.
