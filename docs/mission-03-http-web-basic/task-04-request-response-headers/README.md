# HTTP 요청 헤더와 응답 헤더의 역할 분석

`mission-03-http-web-basic`의 `task-04-request-response-headers` 보고서입니다. HTTP 요청과 응답에서 자주 확인하는 주요 헤더를 나눠 보고, 각 헤더가 어떤 목적을 가지는지 표 중심으로 정리했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-03-http-web-basic` / `task-04-request-response-headers`
- 목표:
  - HTTP 요청 헤더와 응답 헤더가 각각 어떤 정보를 전달하는지 구분한다.
  - `Content-Type`, `Accept`, `User-Agent`를 포함한 대표 헤더의 목적과 실제 사용 위치를 정리한다.
  - 브라우저 개발자도구와 `curl`로 요청/응답 헤더를 직접 확인하는 절차를 문서화한다.

## 2. 주요 해설

### 2.1 요청 헤더와 응답 헤더를 나누는 기준

- 요청 헤더는 클라이언트가 서버에 "무엇을 원하고, 어떤 형식으로 받을 수 있으며, 어떤 상태인지"를 전달할 때 사용합니다.
- 응답 헤더는 서버가 클라이언트에 "무엇을 보냈고, 어떻게 해석해야 하며, 이후 어떻게 처리해야 하는지"를 알려줄 때 사용합니다.
- 같은 이름의 헤더라도 양쪽에서 다르게 쓰일 수 있는데, 대표적으로 `Content-Type`은 요청 본문 타입을 설명할 수도 있고, 응답 본문 타입을 설명할 수도 있습니다.

### 2.2 주요 요청 헤더 설명표

| 헤더 | 핵심 목적 | 언제 주로 보이는가 | 어떻게 동작하는가 | 확인 포인트 |
|---|---|---|---|---|
| `Host` | 요청 대상 서버 식별 | HTTP/1.1 이상 요청 대부분 | 같은 IP로 여러 도메인을 운영할 때 어떤 호스트를 요청한 것인지 서버가 구분할 수 있게 합니다. | `localhost:8080`, `example.com`처럼 도메인과 포트가 함께 보일 수 있습니다. |
| `Accept` | 받을 수 있는 응답 형식 전달 | API 호출, 브라우저 요청 | 클라이언트가 선호하는 미디어 타입을 서버에 알려 줍니다. 예: `application/json`, `text/html` | 서버가 어떤 포맷으로 응답할지 협상하는 기준이 됩니다. |
| `Content-Type` | 요청 본문 형식 설명 | `POST`, `PUT`, `PATCH`처럼 본문이 있는 요청 | 서버가 본문을 JSON, 폼 데이터, 텍스트 중 어떤 형식으로 해석해야 하는지 알려 줍니다. | `application/json`이 빠지면 서버가 요청 본문을 제대로 파싱하지 못할 수 있습니다. |
| `User-Agent` | 요청을 보낸 클라이언트 정보 전달 | 브라우저, 앱, 봇 요청 | 브라우저 종류, 운영체제, 버전 등 클라이언트 식별 정보를 문자열로 담습니다. | 서버 로그나 통계, 호환성 분기 처리에서 참고하지만 신뢰 가능한 보안 정보로 보지는 않습니다. |
| `Authorization` | 인증 수단 전달 | 로그인 이후 API 호출 | `Bearer`, `Basic` 같은 인증 스킴과 자격 증명을 함께 실어 서버가 사용자 또는 클라이언트를 인증할 수 있게 합니다. | 민감 정보가 들어가므로 HTTPS와 함께 사용해야 합니다. |
| `Cookie` | 브라우저 상태 정보 전달 | 세션 기반 로그인, 사용자 설정 유지 | 서버가 이전 응답의 `Set-Cookie`로 저장시킨 값을 브라우저가 이후 요청에 자동으로 붙여 보냅니다. | 세션 식별자, 사용자 설정 값이 실릴 수 있으므로 보안 속성과 함께 관리해야 합니다. |

### 2.3 주요 응답 헤더 설명표

| 헤더 | 핵심 목적 | 언제 주로 보이는가 | 어떻게 동작하는가 | 확인 포인트 |
|---|---|---|---|---|
| `Content-Type` | 응답 본문 형식 설명 | 거의 모든 정상 응답 | 브라우저나 클라이언트가 본문을 JSON, HTML, 이미지 등 어떤 타입으로 해석할지 알려 줍니다. | `application/json`, `text/html; charset=UTF-8`처럼 타입과 문자셋이 함께 올 수 있습니다. |
| `Content-Length` | 응답 본문 크기 전달 | 길이가 확정된 응답 | 바이트 단위 응답 크기를 알려 주어 클라이언트가 메시지 끝을 판단하는 데 도움을 줍니다. | 전송 방식이 chunked면 생략될 수 있습니다. |
| `Cache-Control` | 캐시 정책 제어 | 정적 리소스, API 응답 | 캐시 저장 여부, 재사용 시간, 재검증 필요 여부를 지시어로 내려 줍니다. | `no-store`, `no-cache`, `max-age=3600` 같은 값의 의미를 함께 봐야 합니다. |
| `Set-Cookie` | 브라우저에 쿠키 저장 지시 | 로그인 성공, 사용자 추적, 설정 저장 | 서버가 쿠키 값을 브라우저에 저장시키고 이후 요청에서 `Cookie`로 다시 보내게 만듭니다. | `HttpOnly`, `Secure`, `SameSite` 속성을 함께 확인하는 습관이 중요합니다. |
| `Location` | 이동할 URI 안내 | `201 Created`, `3xx Redirect` 응답 | 새로 생성된 리소스 위치나 리다이렉트 대상 주소를 알려 줍니다. | 상태 코드와 함께 봐야 의미가 정확해집니다. |
| `Content-Security-Policy` | 브라우저 리소스 실행 범위 제한 | 보안 헤더가 적용된 HTML 응답 | 어떤 출처의 스크립트, 스타일, 이미지, 프레임을 허용할지 정책으로 선언합니다. | XSS 완화 목적이며, 너무 느슨하면 효과가 약하고 너무 강하면 정상 리소스도 막을 수 있습니다. |

### 2.4 요청과 응답을 함께 봐야 이해되는 대표 헤더

| 헤더 조합 | 함께 봐야 하는 이유 | 예시 |
|---|---|---|
| `Accept` ↔ `Content-Type` | 클라이언트가 원하는 응답 형식과 서버가 실제로 보낸 형식이 맞는지 확인할 수 있습니다. | 클라이언트는 `Accept: application/json`을 보내고, 서버는 `Content-Type: application/json`으로 응답 |
| `Set-Cookie` ↔ `Cookie` | 서버가 저장시킨 쿠키가 다음 요청에 다시 전달되는 흐름을 볼 수 있습니다. | 로그인 응답의 `Set-Cookie: JSESSIONID=...` 이후 다음 요청의 `Cookie: JSESSIONID=...` |
| `Content-Type`(요청) ↔ `Content-Type`(응답) | 요청 본문 형식과 응답 본문 형식은 서로 다를 수 있어 양쪽을 따로 확인해야 합니다. | 요청은 JSON(`application/json`), 응답은 HTML(`text/html`) 또는 다시 JSON |

## 3. 새로 나온 개념 정리 + 참고 링크

- **Accept 헤더**
  - 핵심: `Accept`는 클라이언트가 어떤 미디어 타입의 응답을 받을 수 있는지 서버에 알리는 요청 헤더입니다.
  - 왜 쓰는가: 같은 리소스라도 JSON, HTML, XML 등 여러 형식으로 표현될 수 있으므로 서버와 응답 형식을 협상할 필요가 있기 때문입니다.
  - 참고 링크:
    - RFC 9110 HTTP Semantics: https://datatracker.ietf.org/doc/html/rfc9110
    - MDN Accept: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Accept

- **Content-Type 헤더**
  - 핵심: `Content-Type`은 메시지 본문이 어떤 형식인지 설명하는 헤더로, 요청과 응답 양쪽 모두에 등장할 수 있습니다.
  - 왜 쓰는가: 본문이 JSON인지 HTML인지 알 수 있어야 서버와 클라이언트가 올바르게 파싱하고 처리할 수 있기 때문입니다.
  - 참고 링크:
    - RFC 9110 HTTP Semantics: https://datatracker.ietf.org/doc/html/rfc9110
    - MDN Content-Type: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Type

- **User-Agent 헤더**
  - 핵심: `User-Agent`는 요청을 보낸 브라우저나 애플리케이션의 종류와 버전 정보를 담는 요청 헤더입니다.
  - 왜 쓰는가: 서버 로그 분석, 통계, 일부 호환성 처리에 도움을 줄 수 있기 때문입니다.
  - 참고 링크:
    - RFC 9110 HTTP Semantics: https://datatracker.ietf.org/doc/html/rfc9110
    - MDN User-Agent: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/User-Agent

- **Set-Cookie와 Cookie**
  - 핵심: `Set-Cookie`는 서버가 브라우저에 쿠키 저장을 지시하는 응답 헤더이고, `Cookie`는 이후 요청에서 그 값을 다시 보내는 요청 헤더입니다.
  - 왜 쓰는가: HTTP는 기본적으로 무상태이므로 로그인 세션이나 사용자 설정을 이어서 처리하려면 상태 식별 수단이 필요하기 때문입니다.
  - 참고 링크:
    - RFC 6265 HTTP State Management Mechanism: https://datatracker.ietf.org/doc/html/rfc6265
    - MDN Set-Cookie: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Set-Cookie
    - MDN Cookie: https://developer.mozilla.org/ko/docs/Web/HTTP/Reference/Headers/Cookie

- **Location 헤더**
  - 핵심: `Location`은 새 리소스의 주소나 이동할 다음 주소를 알려주는 응답 헤더입니다.
  - 왜 쓰는가: 리소스 생성 이후 새 주소를 알려주거나, 리다이렉트 흐름에서 다음 요청 대상을 명확히 전달해야 하기 때문입니다.
  - 참고 링크:
    - RFC 9110 HTTP Semantics: https://datatracker.ietf.org/doc/html/rfc9110
    - MDN Location: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Location

- **캐시와 보안 응답 헤더**
  - 핵심: `Cache-Control`은 캐시 정책을, `Content-Security-Policy`는 브라우저 리소스 실행 정책을 내려주는 응답 헤더입니다.
  - 왜 쓰는가: 응답을 얼마나 재사용할지와 브라우저가 어떤 리소스를 신뢰할지를 서버가 통제해야 성능과 보안을 함께 관리할 수 있기 때문입니다.
  - 참고 링크:
    - RFC 9111 HTTP Caching: https://datatracker.ietf.org/doc/html/rfc9111
    - MDN Cache-Control: https://developer.mozilla.org/ko/docs/Web/HTTP/Reference/Headers/Cache-Control
    - CSP Level 3: https://www.w3.org/TR/CSP3/
    - MDN Content-Security-Policy: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy

## 4. 학습 내용

- 요청 헤더는 서버가 요청을 이해하는 데 필요한 정보이고, 응답 헤더는 클라이언트가 응답을 해석하고 다음 동작을 결정하는 데 필요한 정보입니다.
- `Accept`와 응답의 `Content-Type`을 함께 보면 "무엇을 원했는지"와 "무엇을 받았는지"를 연결해서 이해할 수 있습니다.
- `Content-Type`은 한쪽 전용 헤더가 아니라, 요청 본문 설명과 응답 본문 설명 모두에 쓰인다는 점이 중요합니다.
- `User-Agent`는 유용한 정보이지만, 사용자가 조작할 수 있으므로 인증이나 권한 판단 기준으로 신뢰하면 안 됩니다.
- `Set-Cookie`와 `Cookie`, `Location`과 상태 코드처럼 헤더는 단독으로 보기보다 HTTP 흐름 안에서 연결해서 볼 때 더 잘 이해됩니다.
