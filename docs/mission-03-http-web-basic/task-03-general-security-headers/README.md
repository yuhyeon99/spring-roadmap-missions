# 일반 HTTP 헤더와 보안 헤더 분석

`mission-03-http-web-basic`의 `task-03-general-security-headers` 보고서입니다. HTTP 통신에서 자주 보이는 일반 헤더와 보안 관련 헤더를 구분하고, 각 헤더가 어떤 문제를 해결하는지 표 중심으로 정리했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-03-http-web-basic` / `task-03-general-security-headers`
- 목표:
  - 일반적으로 자주 보는 HTTP 헤더인 `Cache-Control`, `Expires`, `Cookie`의 역할과 차이를 이해한다.
  - 보안 관점에서 중요한 `Authorization`, `Content-Security-Policy` 헤더의 역할과 주의점을 정리한다.
  - 브라우저 개발자도구와 `curl`로 요청/응답 헤더를 직접 확인하는 방법을 문서화한다.

## 2. 주요 해설

### 2.1 이번 태스크에서 보는 헤더 분류 기준

- 이번 문서의 "일반 헤더"는 RFC의 엄격한 분류라기보다, 웹 개발에서 자주 마주치는 실무 중심 헤더 묶음으로 보았습니다.
- `Cache-Control`, `Expires`는 캐시 정책을 다루는 응답 헤더이고, `Cookie`는 브라우저가 서버에 상태 정보를 다시 보내기 위해 사용하는 요청 헤더입니다.
- `Authorization`은 인증 정보를 담는 요청 헤더이고, `Content-Security-Policy`는 브라우저가 어떤 리소스를 신뢰할지 제한하는 보안 응답 헤더입니다.

### 2.2 일반 HTTP 헤더 설명표

| 헤더 | 주로 보내는 쪽 | 핵심 역할 | 어떻게 동작하는가 | 실무에서 볼 포인트 |
|---|---|---|---|---|
| `Cache-Control` | 서버 응답, 필요 시 클라이언트 요청 | 캐시 사용 규칙 제어 | `max-age`, `no-cache`, `no-store`, `public`, `private` 같은 지시어로 캐시 저장 여부와 재검증 방식을 지정합니다. | 최신 캐시 정책의 중심 헤더입니다. `Expires`보다 우선해서 해석되는 경우가 많습니다. |
| `Expires` | 서버 응답 | 캐시 만료 시각 지정 | 절대 시각(날짜/시간)으로 "이 시점까지는 캐시를 사용해도 된다"는 기준을 줍니다. | 오래된 HTTP 캐시 호환용 성격이 강합니다. `Cache-Control: max-age`가 함께 있으면 보통 `Cache-Control`이 더 우선합니다. |
| `Cookie` | 브라우저 요청 | 상태 정보 전달 | 서버가 이전 응답의 `Set-Cookie`로 저장시킨 값을, 브라우저가 이후 요청마다 `Cookie: name=value` 형태로 다시 보냅니다. | 로그인 세션, 장바구니, 사용자 설정 유지에 자주 쓰입니다. 값 자체는 암호화가 아니므로 민감 정보는 직접 넣지 않는 편이 안전합니다. |

### 2.3 보안 헤더 설명표

| 헤더 | 주로 보내는 쪽 | 핵심 역할 | 어떻게 동작하는가 | 실무에서 볼 포인트 |
|---|---|---|---|---|
| `Authorization` | 클라이언트 요청 | 인증 정보 전달 | `Basic`, `Bearer` 같은 스킴을 사용해 서버에 인증 수단을 전달합니다. 예: `Authorization: Bearer <token>` | 토큰이나 자격 증명이 들어가므로 HTTPS와 함께 써야 합니다. 프록시/로그에 평문으로 남지 않도록 주의해야 합니다. |
| `Content-Security-Policy` | 서버 응답 | 브라우저가 허용할 리소스 범위 제한 | 스크립트, 스타일, 이미지, 프레임 등을 어떤 출처에서 로드할지 정책으로 선언합니다. 인라인 스크립트 허용 여부도 제어할 수 있습니다. | XSS 완화에 매우 중요합니다. 너무 느슨하면 효과가 약하고, 너무 강하면 정상 리소스도 막을 수 있어 단계적 조정이 필요합니다. |

### 2.4 헤더를 함께 보면 이해가 쉬운 흐름

1. 사용자가 로그인하면 서버는 보통 응답에서 `Set-Cookie`를 내려 세션 식별자를 저장하게 합니다.
2. 이후 브라우저는 같은 사이트 요청에 `Cookie` 헤더를 자동으로 붙여 서버에 보냅니다.
3. 토큰 기반 인증을 쓰는 API는 브라우저나 앱이 `Authorization` 헤더에 액세스 토큰을 넣어 보냅니다.
4. 서버는 응답에 `Cache-Control`, `Expires`를 넣어 재사용 가능 여부를 알려 주고, 필요하면 `Content-Security-Policy`로 브라우저의 리소스 실행 범위를 제한합니다.

## 3. 새로 나온 개념 정리 + 참고 링크

- **HTTP 캐시 제어**
  - 핵심: `Cache-Control`은 캐시 가능 여부, 재사용 시간, 재검증 필요 여부를 지시어로 제어합니다.
  - 왜 쓰는가: 같은 응답을 다시 내려 보내지 않아도 되면 서버 부하와 응답 시간을 줄일 수 있기 때문입니다.
  - 참고 링크:
    - RFC 9111 HTTP Caching: https://datatracker.ietf.org/doc/html/rfc9111
    - MDN Cache-Control: https://developer.mozilla.org/ko/docs/Web/HTTP/Reference/Headers/Cache-Control

- **만료 기반 캐시**
  - 핵심: `Expires`는 캐시의 만료 시점을 절대 시간으로 알려 줍니다.
  - 왜 쓰는가: 오래된 캐시 동작과의 호환이 필요할 때 기준 시각을 명시할 수 있습니다.
  - 참고 링크:
    - RFC 9111 HTTP Caching: https://datatracker.ietf.org/doc/html/rfc9111
    - MDN Expires: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Expires

- **쿠키와 상태 유지**
  - 핵심: 쿠키는 서버가 브라우저에 저장시킨 값을 이후 요청에 다시 실어 보내는 방식으로 상태를 유지합니다.
  - 왜 쓰는가: HTTP는 기본적으로 무상태이므로 로그인 상태나 사용자 설정을 계속 식별하려면 별도 정보 전달 수단이 필요합니다.
  - 참고 링크:
    - RFC 6265 HTTP State Management Mechanism: https://datatracker.ietf.org/doc/html/rfc6265
    - MDN Cookie: https://developer.mozilla.org/ko/docs/Web/HTTP/Reference/Headers/Cookie

- **Authorization 헤더**
  - 핵심: `Authorization`은 클라이언트가 서버에 자신의 인증 수단을 전달하는 표준 헤더입니다.
  - 왜 쓰는가: API 서버가 요청 주체를 식별하고 접근 허용 여부를 판단하려면 인증 정보가 필요하기 때문입니다.
  - 참고 링크:
    - RFC 9110 HTTP Semantics: https://datatracker.ietf.org/doc/html/rfc9110
    - MDN Authorization: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Authorization

- **Content-Security-Policy**
  - 핵심: CSP는 브라우저가 허용할 스크립트, 스타일, 이미지 출처를 선언해 악성 코드 실행 범위를 좁힙니다.
  - 왜 쓰는가: XSS 같은 클라이언트 측 공격은 브라우저가 스크립트를 실행해 버릴 때 문제가 커지므로, 사전에 허용 범위를 제한할 필요가 있습니다.
  - 참고 링크:
    - CSP Level 3: https://www.w3.org/TR/CSP3/
    - MDN Content-Security-Policy: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy

## 4. 학습 내용

- HTTP 헤더는 전부 같은 성격이 아니라, 캐시 제어, 상태 유지, 인증, 브라우저 보안 정책처럼 해결하려는 문제가 다릅니다.
- `Cookie`와 `Authorization`은 둘 다 사용자 식별과 관련이 있지만 방식이 다릅니다. 쿠키는 브라우저가 자동으로 붙이는 상태 정보에 가깝고, `Authorization`은 클라이언트가 명시적으로 인증 수단을 전달하는 방식입니다.
- `Cache-Control`과 `Expires`는 비슷해 보여도 표현 방식이 다릅니다. 실무에서는 상대 시간 기반의 `Cache-Control`이 더 중심이 되고, `Expires`는 호환성을 보완하는 경우가 많습니다.
- `Content-Security-Policy`는 서버 내부 인증 로직이 아니라, 브라우저가 어떤 코드를 실행해도 되는지 제한하는 방어선입니다. 그래서 서버 보안과 프런트엔드 보안이 만나는 지점으로 이해하면 좋습니다.
