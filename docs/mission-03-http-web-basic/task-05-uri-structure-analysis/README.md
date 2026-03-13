# URI의 구조 분석하기

`mission-03-http-web-basic`의 `task-05-uri-structure-analysis` 보고서입니다. URI와 URL의 차이를 정리하고, URI를 구성하는 각 요소가 어떤 의미를 가지는지 표와 예시 중심으로 분석했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-03-http-web-basic` / `task-05-uri-structure-analysis`
- 목표:
  - URI와 URL의 차이를 구분한다.
  - URI의 구성 요소인 스킴, 사용자 정보, 호스트, 포트, 경로, 쿼리, 프래그먼트를 분석한다.
  - 예시 URI를 실제로 분해해 각 부분이 어떤 역할을 하는지 설명한다.

## 2. 주요 해설

### 2.1 URI와 URL의 차이

- `URI(Uniform Resource Identifier)`는 인터넷 자원을 식별하는 더 큰 개념입니다.
- `URL(Uniform Resource Locator)`은 그 자원이 어디에 있고 어떻게 접근할지까지 포함하는 URI의 한 형태입니다.
- 즉, URL은 URI에 포함되는 개념으로 보면 이해하기 쉽습니다.
- 웹 개발에서 우리가 흔히 보는 `https://example.com/products/1` 같은 형태는 대부분 URL이면서 동시에 URI입니다.

### 2.2 URI 구성 요소 분석표

| 구성 요소 | 예시 값 | 역할 | 핵심 포인트 |
|---|---|---|---|
| `scheme` | `https` | 어떤 프로토콜 또는 접근 방식을 사용할지 나타냅니다. | `http`, `https`, `ftp`, `mailto`처럼 시작 부분에 옵니다. |
| `userinfo` | `user:pass` | 인증 정보가 URI 안에 포함될 때 사용됩니다. | 보안상 잘 쓰지 않으며, 실무에서는 헤더 기반 인증을 더 많이 사용합니다. |
| `host` | `example.com` | 자원이 위치한 서버의 도메인 또는 IP입니다. | 브라우저와 DNS가 실제 접속 대상을 찾을 때 핵심이 됩니다. |
| `port` | `8443` | 서버의 세부 서비스 포트 번호입니다. | 생략하면 스킴 기본값(`http`는 80, `https`는 443)을 사용합니다. |
| `path` | `/products/42` | 서버 내부에서 어떤 리소스를 요청하는지 나타냅니다. | 계층 구조처럼 보이지만 실제 해석은 서버 라우팅 규칙에 따라 달라집니다. |
| `query` | `sort=desc&page=2` | 추가 조건이나 필터 값을 전달합니다. | `?` 뒤에 오고, 여러 값은 `&`로 구분합니다. |
| `fragment` | `reviews` | 문서 내부의 특정 위치나 화면 상태를 가리킵니다. | `#` 뒤에 오며 보통 서버로 전송되지 않습니다. |

### 2.3 예시 URI 분해 설명

분석할 예시 URI:

```text
https://user:pass@example.com:8443/products/42/details?color=blue&sort=desc#reviews
```

| 구간 | 값 | 설명 |
|---|---|---|
| 전체 URI | `https://user:pass@example.com:8443/products/42/details?color=blue&sort=desc#reviews` | 하나의 자원을 식별하기 위한 전체 문자열입니다. |
| 스킴 | `https` | HTTPS 프로토콜을 사용하겠다는 의미입니다. |
| 사용자 정보 | `user:pass` | 서버 접속용 사용자 정보가 URI에 포함된 형태입니다. 실무에서는 잘 사용하지 않습니다. |
| 호스트 | `example.com` | 요청을 보낼 대상 서버의 도메인입니다. |
| 포트 | `8443` | 기본 HTTPS 포트 443이 아니라 8443 포트로 접속하겠다는 뜻입니다. |
| 경로 | `/products/42/details` | 상품 42번의 상세 정보 같은 리소스를 가리키는 경로로 볼 수 있습니다. |
| 쿼리 문자열 | `color=blue&sort=desc` | 색상과 정렬 기준 같은 추가 조건을 전달합니다. |
| 프래그먼트 | `reviews` | 응답 문서 내부에서 리뷰 섹션처럼 특정 위치를 가리킵니다. 브라우저가 주로 사용합니다. |

### 2.4 예시 URI를 읽는 순서

1. `https`를 보고 어떤 방식으로 통신할지 먼저 판단합니다.
2. `example.com:8443`을 보고 어느 서버와 포트에 접속할지 결정합니다.
3. `/products/42/details`를 보고 서버 안에서 어떤 리소스를 찾을지 판단합니다.
4. `?color=blue&sort=desc`를 보고 조회 조건이나 필터를 해석합니다.
5. `#reviews`는 브라우저가 페이지 내부 이동에 사용하며, 일반적인 HTTP 요청 메시지에는 포함되지 않습니다.

## 2. 새로 나온 개념 정리 + 참고 링크

- **URI와 URL의 관계**
  - 핵심: URI는 자원을 식별하는 상위 개념이고, URL은 자원의 위치와 접근 방식까지 포함하는 URI의 한 형태입니다.
  - 왜 쓰는가: 웹 주소를 볼 때 "무엇을 식별하는지"와 "어떻게 접근하는지"를 분리해서 이해하면 HTTP 요청 구조를 더 정확히 해석할 수 있기 때문입니다.
  - 참고 링크:
    - RFC 3986 URI Generic Syntax: https://datatracker.ietf.org/doc/html/rfc3986
    - MDN URL 이해하기: https://developer.mozilla.org/ko/docs/Learn_web_development/Howto/Web_mechanics/What_is_a_URL

- **스킴과 authority**
  - 핵심: 스킴은 접근 방식을, authority는 사용자 정보·호스트·포트처럼 접속 대상을 나타냅니다.
  - 왜 쓰는가: 브라우저나 클라이언트가 어떤 프로토콜로 어느 서버에 연결해야 할지 결정하는 출발점이기 때문입니다.
  - 참고 링크:
    - RFC 3986 URI Generic Syntax: https://datatracker.ietf.org/doc/html/rfc3986

- **경로와 쿼리 문자열**
  - 핵심: 경로는 리소스 위치를, 쿼리는 추가 조건이나 필터를 표현합니다.
  - 왜 쓰는가: 같은 리소스 그룹 안에서도 상세 대상과 조회 조건을 분리해 표현할 수 있기 때문입니다.
  - 참고 링크:
    - RFC 3986 URI Generic Syntax: https://datatracker.ietf.org/doc/html/rfc3986
    - MDN URLSearchParams: https://developer.mozilla.org/ko/docs/Web/API/URLSearchParams

- **프래그먼트**
  - 핵심: 프래그먼트는 문서 내부 위치나 클라이언트 상태를 가리키며, 일반적으로 서버에 전송되지 않습니다.
  - 왜 쓰는가: 긴 문서에서 특정 섹션으로 바로 이동하거나 SPA 라우팅처럼 브라우저 내부 상태를 표현할 수 있기 때문입니다.
  - 참고 링크:
    - RFC 3986 URI Generic Syntax: https://datatracker.ietf.org/doc/html/rfc3986
    - MDN URL API: https://developer.mozilla.org/ko/docs/Web/API/URL

## 3. 학습 내용

- URI는 자원을 식별하는 전체 개념이고, URL은 그중에서도 위치와 접근 방식을 담는 실용적인 형태입니다.
- 경로와 쿼리를 구분해서 보면 "무슨 자원인지"와 "어떤 조건으로 요청하는지"를 분리해 이해할 수 있습니다.
- 프래그먼트는 서버가 아니라 브라우저가 주로 사용하는 정보라는 점이 HTTP 요청 구조를 이해할 때 자주 헷갈리는 부분입니다.
- 실제 웹 주소를 볼 때 스킴, 호스트, 포트, 경로, 쿼리를 순서대로 나누는 습관을 들이면 이후 HTTP 요청/응답 흐름과 라우팅 개념을 이해하기 훨씬 쉬워집니다.
