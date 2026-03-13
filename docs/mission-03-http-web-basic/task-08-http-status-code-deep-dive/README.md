# HTTP 상태 코드 심화 이해하기

`mission-03-http-web-basic`의 `task-08-http-status-code-deep-dive` 보고서입니다. HTTP 상태 코드를 1xx, 2xx, 3xx, 4xx, 5xx 그룹으로 나누어 보고, 각 그룹이 어떤 역할을 가지는지와 실무에서 자주 만나는 주요 상태 코드 사례를 정리했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-03-http-web-basic` / `task-08-http-status-code-deep-dive`
- 목표:
  - HTTP 상태 코드 그룹별 의미를 구분한다.
  - 주요 상태 코드가 어떤 상황에서 사용되는지 사례와 함께 이해한다.
  - 상태 코드 설명표와 요청/응답 예시를 통해 실제 API 응답 해석 방식을 정리한다.

## 2. 주요 해설

### 2.1 HTTP 상태 코드 그룹 설명표

| 그룹 | 범위 | 역할 | 언제 보게 되는가 |
|---|---|---|---|
| `1xx` | `100` ~ `199` | 요청 처리 중간 상태를 알리는 정보 응답 | 본문 전송 전 승인, 프로토콜 전환 같은 특수 상황 |
| `2xx` | `200` ~ `299` | 요청이 성공적으로 처리되었음을 의미 | 조회 성공, 생성 성공, 삭제 성공 |
| `3xx` | `300` ~ `399` | 추가 동작이 필요함을 알림 | 리다이렉트, 캐시 재검증 결과 |
| `4xx` | `400` ~ `499` | 클라이언트 요청에 문제가 있음을 의미 | 잘못된 요청, 인증 실패, 권한 부족, 리소스 없음 |
| `5xx` | `500` ~ `599` | 서버 내부 처리 중 문제가 발생했음을 의미 | 예외 발생, 게이트웨이 오류, 서버 과부하 |

### 2.2 주요 상태 코드 설명표

| 상태 코드 | 의미 | 대표 사례 | 핵심 포인트 |
|---|---|---|---|
| `100 Continue` | 요청 본문을 계속 보내도 된다는 중간 응답 | 큰 파일 업로드 전에 헤더만 먼저 보낸 경우 | 최종 응답이 아니라 중간 신호입니다. |
| `101 Switching Protocols` | 프로토콜 전환 | HTTP에서 WebSocket 업그레이드 | 연결 방식이 바뀌는 특수 상황입니다. |
| `200 OK` | 일반적인 성공 | 목록 조회, 단건 조회 성공 | 가장 자주 보는 성공 응답입니다. |
| `201 Created` | 새 리소스 생성 성공 | `POST /notes`로 새 노트 생성 | 보통 `Location` 헤더와 함께 새 URI를 알려줄 수 있습니다. |
| `204 No Content` | 성공했지만 본문 없음 | 삭제 성공, 본문 없는 업데이트 응답 | 응답 바디가 없다는 점이 핵심입니다. |
| `301 Moved Permanently` | 영구 이동 | 예전 URL을 새 URL로 완전히 이전 | 검색 엔진과 캐시가 새 주소를 기준으로 학습할 수 있습니다. |
| `302 Found` | 임시 이동 | 로그인 후 다른 페이지로 잠시 이동 | 원래 URL이 장기적으로 바뀌었다는 뜻은 아닙니다. |
| `304 Not Modified` | 캐시 재사용 가능 | 브라우저가 `If-Modified-Since`로 재검증한 경우 | 본문 없이 캐시된 응답을 다시 써도 된다는 뜻입니다. |
| `400 Bad Request` | 잘못된 요청 | 필수 값 누락, 잘못된 JSON 형식 | 서버가 요청 자체를 이해하거나 처리하기 어렵다는 의미입니다. |
| `401 Unauthorized` | 인증 필요 또는 인증 실패 | 토큰 없음, 만료된 토큰 | 보통 로그인이나 인증 수단이 먼저 필요합니다. |
| `403 Forbidden` | 인증은 되었지만 권한 없음 | 일반 사용자가 관리자 자원 접근 | "누구인지"는 알지만 허용하지 않는 경우입니다. |
| `404 Not Found` | 리소스를 찾지 못함 | 존재하지 않는 노트 ID 조회 | 잘못된 경로나 없는 자원을 요청했을 때 자주 봅니다. |
| `409 Conflict` | 현재 상태와 충돌 | 중복 생성, 버전 충돌 | 요청 형식은 맞지만 현재 서버 상태와 충돌하는 경우입니다. |
| `500 Internal Server Error` | 서버 내부 예외 | 처리 중 NullPointerException 등 발생 | 서버 쪽 오류를 대표하는 일반적인 상태 코드입니다. |
| `502 Bad Gateway` | 게이트웨이/프록시 오류 | API Gateway 뒤의 upstream 서버 응답 이상 | 중간 서버가 정상 응답을 받지 못한 상황입니다. |
| `503 Service Unavailable` | 일시적 서비스 불가 | 점검 중, 과부하 | 잠시 후 재시도하라는 의미로 쓰이는 경우가 많습니다. |

### 2.3 주요 코드 예시

#### 2xx 예시: 조회 성공

```http
GET /mission03/task01/notes HTTP/1.1
Host: localhost:8080
Accept: application/json
```

```http
HTTP/1.1 200 OK
Content-Type: application/json

[]
```

- 서버가 요청을 정상 처리했고, 본문에 조회 결과를 담아 반환합니다.

#### 2xx 예시: 생성 성공

```http
POST /mission03/task01/notes HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "title": "HTTP",
  "content": "created"
}
```

```http
HTTP/1.1 201 Created
Location: /mission03/task01/notes/1
Content-Type: application/json

{
  "id": 1,
  "title": "HTTP",
  "content": "created"
}
```

- 새 리소스가 만들어졌고, `Location` 헤더로 생성 위치를 알려주는 흐름입니다.

#### 4xx 예시: 없는 리소스 조회

```http
GET /mission03/task01/notes/999 HTTP/1.1
Host: localhost:8080
Accept: application/json
```

```http
HTTP/1.1 404 Not Found
Content-Type: application/json
```

- 요청 형식은 맞지만, 찾으려는 리소스가 서버에 없습니다.

#### 4xx 예시: 잘못된 요청

```http
POST /mission03/task01/notes HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "title": "",
  "content": ""
}
```

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json
```

- 필수 값 검증에 실패하면 클라이언트 요청이 잘못되었다고 판단할 수 있습니다.

#### 5xx 예시: 서버 내부 오류

```http
HTTP/1.1 500 Internal Server Error
Content-Type: application/json
```

- 요청 자체보다 서버 내부 처리 실패가 원인이라는 점이 핵심입니다.

### 2.4 상태 코드를 읽는 순서

1. 먼저 첫 자리 숫자로 성공인지, 리다이렉트인지, 클라이언트 오류인지, 서버 오류인지 큰 범주를 판단합니다.
2. 그다음 정확한 코드(`201`, `404`, `503` 등)로 세부 상황을 읽습니다.
3. 상태 코드만 보지 말고 `Location`, `Retry-After`, `Content-Type` 같은 헤더와 본문 메시지도 함께 확인해야 원인을 더 정확히 알 수 있습니다.
4. 같은 실패라도 `400`, `401`, `403`, `404`, `409`는 원인이 다르므로 구분해서 이해해야 API 설계와 디버깅이 쉬워집니다.

## 3. 새로 나온 개념 정리 + 참고 링크

- **1xx Informational**
  - 핵심: 요청 처리가 아직 끝나지 않았고, 중간 상태를 알려주는 응답입니다.
  - 왜 쓰는가: 큰 요청 본문 전송이나 프로토콜 전환처럼 최종 응답 전에 추가 안내가 필요할 수 있기 때문입니다.
  - 참고 링크:
    - RFC 9110 HTTP Semantics: https://datatracker.ietf.org/doc/html/rfc9110

- **2xx Successful**
  - 핵심: 요청이 정상 처리되었음을 의미하는 성공 응답 그룹입니다.
  - 왜 쓰는가: 조회, 생성, 수정, 삭제가 어떤 형태로 성공했는지 구분해서 전달할 수 있기 때문입니다.
  - 참고 링크:
    - RFC 9110 HTTP Semantics: https://datatracker.ietf.org/doc/html/rfc9110
    - MDN HTTP response status codes: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status

- **3xx Redirection**
  - 핵심: 요청을 완료하려면 클라이언트가 다른 위치로 이동하거나 캐시를 재사용해야 함을 의미합니다.
  - 왜 쓰는가: URL 변경, 인증 흐름, 캐시 최적화 같은 상황을 표준 방식으로 표현할 수 있기 때문입니다.
  - 참고 링크:
    - RFC 9110 HTTP Semantics: https://datatracker.ietf.org/doc/html/rfc9110

- **4xx Client Error**
  - 핵심: 요청을 보낸 쪽의 입력, 인증, 권한, 대상 리소스에 문제가 있음을 나타냅니다.
  - 왜 쓰는가: 서버 오류와 구분해야 클라이언트가 어떤 부분을 고쳐야 하는지 판단할 수 있기 때문입니다.
  - 참고 링크:
    - RFC 9110 HTTP Semantics: https://datatracker.ietf.org/doc/html/rfc9110
    - MDN HTTP response status codes: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status

- **5xx Server Error**
  - 핵심: 요청은 들어왔지만 서버 내부 처리에 실패했음을 의미합니다.
  - 왜 쓰는가: 클라이언트 잘못과 서버 장애를 분리해야 모니터링과 장애 대응이 가능하기 때문입니다.
  - 참고 링크:
    - RFC 9110 HTTP Semantics: https://datatracker.ietf.org/doc/html/rfc9110
    - MDN HTTP response status codes: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status

## 4. 학습 내용

- HTTP 상태 코드는 숫자 하나가 아니라, 클라이언트와 서버가 처리 결과를 공유하는 약속입니다.
- `2xx`는 성공, `4xx`는 클라이언트 문제, `5xx`는 서버 문제라는 큰 구분을 먼저 잡으면 디버깅 속도가 빨라집니다.
- 특히 `400`, `401`, `403`, `404`, `409`는 모두 실패처럼 보여도 원인이 다르기 때문에 정확히 구분해야 API 설계 품질이 좋아집니다.
- 생성은 `201`, 삭제는 `204`, 캐시 재검증은 `304`처럼 상황에 맞는 상태 코드를 쓰면 응답 의미가 훨씬 명확해집니다.
