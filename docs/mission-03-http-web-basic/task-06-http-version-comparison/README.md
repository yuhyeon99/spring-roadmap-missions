# HTTP/2와 HTTP/3의 개선점 비교

`mission-03-http-web-basic`의 `task-06-http-version-comparison` 보고서입니다. HTTP/1.1, HTTP/2, HTTP/3의 주요 특징을 비교하고, HTTP/1.1에서 성능 병목이 생기던 지점을 HTTP/2와 HTTP/3이 어떻게 보완했는지 정리했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-03-http-web-basic` / `task-06-http-version-comparison`
- 목표:
  - HTTP/1.1, HTTP/2, HTTP/3의 핵심 차이를 비교한다.
  - 성능 개선이 필요한 영역에서 HTTP/2와 HTTP/3이 어떤 방식으로 문제를 완화했는지 이해한다.
  - HTTP 버전 비교표와 주요 개선점을 문서로 정리한다.

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Doc | `docs/mission-03-http-web-basic/task-06-http-version-comparison/README.md` | HTTP 버전 비교표, 성능 개선 설명, 검증 방법, 학습 내용을 정리한 보고서 |

## 3. 구현 단계와 주요 코드 해설

### 3.1 왜 HTTP 버전이 계속 개선되었는가

- HTTP/1.1은 오랫동안 널리 사용되었지만, 웹 페이지가 복잡해지면서 하나의 페이지를 구성하기 위해 많은 CSS, JS, 이미지 요청이 동시에 필요해졌습니다.
- 이때 텍스트 기반 메시지, 요청 직렬 처리, 반복되는 헤더 전송, 연결 관리 비용이 성능 병목으로 드러났습니다.
- HTTP/2는 같은 TCP 연결 안에서 여러 요청을 동시에 처리하도록 개선했고, HTTP/3는 여기서 더 나아가 TCP 자체가 가지는 지연 문제를 QUIC 기반으로 보완했습니다.

### 3.2 HTTP 버전 비교표

| 비교 항목 | HTTP/1.1 | HTTP/2 | HTTP/3 |
|---|---|---|---|
| 메시지 형식 | 텍스트 기반 | 바이너리 프레이밍 | 바이너리 프레이밍 |
| 전송 계층 | TCP | TCP | QUIC(UDP 기반) |
| 멀티플렉싱 | 사실상 제한적 | 하나의 연결에서 여러 스트림 동시 처리 | 하나의 연결에서 여러 스트림 동시 처리 |
| 헤더 압축 | 없음 또는 매우 제한적 | HPACK 사용 | QPACK 사용 |
| Head-of-Line Blocking | 애플리케이션 레벨에서 큼 | 애플리케이션 레벨 완화, TCP 레벨은 남음 | 스트림 단위로 완화되어 TCP 수준 문제를 크게 줄임 |
| 연결 수립 비용 | TCP + TLS handshake | TCP + TLS handshake | QUIC handshake, 0-RTT 가능 |
| 연결 재사용 | keep-alive 중심 | 단일 연결 효율 증가 | 연결 유지 + 네트워크 변경 시 연결 마이그레이션 지원 |
| 대표 장점 | 단순하고 범용적 | 멀티플렉싱, 헤더 압축, 성능 개선 | 지연 감소, 패킷 손실 대응 개선, 모바일 환경 적응력 향상 |
| 대표 한계 | 다수 리소스 요청 시 비효율적 | TCP 손실 영향은 여전히 존재 | 인프라와 도구 지원이 더 필요할 수 있음 |

### 3.3 HTTP/1.1의 병목과 HTTP/2의 보완

| HTTP/1.1의 문제 | 왜 느려지는가 | HTTP/2의 보완 방식 |
|---|---|---|
| 요청을 효율적으로 병렬 처리하기 어려움 | 여러 리소스를 받기 위해 여러 연결을 열거나 순서를 기다려야 했습니다. | 멀티플렉싱으로 하나의 연결 안에서 여러 스트림을 동시에 주고받습니다. |
| 헤더가 요청마다 반복 전송됨 | 쿠키, User-Agent 같은 긴 헤더가 계속 반복되면 불필요한 바이트가 늘어납니다. | HPACK으로 헤더를 압축해 중복 전송 비용을 줄입니다. |
| 텍스트 기반 메시지 파싱 부담 | 메시지를 문자열 기준으로 처리해야 해 효율이 떨어질 수 있습니다. | 바이너리 프레이밍으로 메시지를 더 구조적으로 전달합니다. |

### 3.4 HTTP/2의 한계와 HTTP/3의 보완

| HTTP/2에서 남은 문제 | 왜 문제가 되는가 | HTTP/3의 보완 방식 |
|---|---|---|
| TCP 기반 Head-of-Line Blocking | 하나의 패킷이 손실되면 뒤에 온 데이터도 TCP 재전송 완료 전까지 영향을 받을 수 있습니다. | QUIC은 스트림 단위 독립성을 높여 특정 스트림 손실이 다른 스트림에 미치는 영향을 줄입니다. |
| 연결 수립 지연 | TCP와 TLS 절차가 겹치면 초기 연결 지연이 체감될 수 있습니다. | QUIC은 전송과 보안을 통합하고, 재연결 시 0-RTT를 활용할 수 있습니다. |
| 네트워크 변경에 취약 | 모바일 환경에서 Wi-Fi에서 LTE로 바뀌면 연결이 끊기기 쉽습니다. | Connection Migration으로 네트워크가 바뀌어도 연결을 이어가기 쉽습니다. |

### 3.5 버전별로 이해하면 좋은 핵심 한 줄

1. HTTP/1.1은 웹의 기본을 널리 보급했지만, 많은 리소스를 한 번에 처리하는 데 한계가 있었습니다.
2. HTTP/2는 "하나의 연결을 더 효율적으로 쓰는 방법"에 집중했습니다.
3. HTTP/3는 "전송 계층까지 포함해 지연과 손실에 더 강하게 만드는 방법"으로 한 단계 더 나아갔습니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `README.md`

- 파일 경로: `docs/mission-03-http-web-basic/task-06-http-version-comparison/README.md`
- 역할: HTTP/1.1, HTTP/2, HTTP/3의 차이와 개선 흐름을 비교표 중심으로 설명합니다.
- 실제 동작 중심 상세 설명:
  - HTTP 버전별 메시지 구조, 전송 계층, 멀티플렉싱, 헤더 압축, Head-of-Line Blocking 차이를 한 표에서 비교합니다.
  - HTTP/1.1의 병목을 HTTP/2가 어떻게 줄였는지, HTTP/2의 남은 문제를 HTTP/3가 어떤 전송 방식으로 보완했는지를 별도 표로 정리합니다.
  - `curl`과 브라우저 개발자도구로 실제 사용된 프로토콜 버전을 확인하는 방법을 함께 안내합니다.

<details>
<summary><code>README.md</code> 원문 안내 및 핵심 발췌</summary>

이 파일은 문서 자신을 설명하는 자기참조 구조라서, 동일한 `README.md` 전체를 다시 안에 중첩하면 내용이 반복적으로 길어집니다.
따라서 현재 보고서 전체를 원문으로 보고, 아래에는 비교표와 검증 명령만 다시 발췌했습니다.

````md
### HTTP 버전 비교표

| 비교 항목 | HTTP/1.1 | HTTP/2 | HTTP/3 |
|---|---|---|---|
| 메시지 형식 | 텍스트 기반 | 바이너리 프레이밍 | 바이너리 프레이밍 |
| 전송 계층 | TCP | TCP | QUIC(UDP 기반) |
| 멀티플렉싱 | 사실상 제한적 | 하나의 연결에서 여러 스트림 동시 처리 | 하나의 연결에서 여러 스트림 동시 처리 |
| 헤더 압축 | 없음 또는 매우 제한적 | HPACK 사용 | QPACK 사용 |

### HTTP/2와 HTTP/3의 핵심 개선

| 개선 대상 | HTTP/2 | HTTP/3 |
|---|---|---|
| 다중 요청 처리 | 멀티플렉싱 | 멀티플렉싱 유지 |
| 헤더 전송 비용 | HPACK 압축 | QPACK 압축 |
| 손실 대응 | TCP 영향 남음 | QUIC으로 더 유연하게 대응 |

### 실행 명령

```bash
curl -I --http1.1 https://nghttp2.org/httpbin/get
curl -I --http2 https://nghttp2.org/httpbin/get
curl -I --http3 https://nghttp2.org/httpbin/get
```
````

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **HTTP/1.1**
  - 핵심: 텍스트 기반 HTTP 메시지와 keep-alive를 중심으로 동작하는 오랜 표준 버전입니다.
  - 왜 쓰는가: 구현이 넓게 보급되어 있고, 많은 서버와 클라이언트가 기본적으로 지원하기 때문입니다.
  - 참고 링크:
    - RFC 9112 HTTP/1.1: https://datatracker.ietf.org/doc/html/rfc9112
    - MDN HTTP overview: https://developer.mozilla.org/ko/docs/Web/HTTP/Overview

- **HTTP/2**
  - 핵심: 하나의 연결에서 여러 요청과 응답을 동시에 처리하는 멀티플렉싱과 헤더 압축을 지원합니다.
  - 왜 쓰는가: 다수의 정적 리소스를 동시에 요청하는 현대 웹 환경에서 연결 효율과 지연 시간을 줄이기 위해서입니다.
  - 참고 링크:
    - RFC 9113 HTTP/2: https://datatracker.ietf.org/doc/html/rfc9113
    - MDN HTTP/2: https://developer.mozilla.org/en-US/docs/Glossary/HTTP_2

- **HTTP/3**
  - 핵심: QUIC 위에서 동작하며 TCP 기반 전송에서 남아 있던 지연과 손실 영향을 줄이는 데 초점을 둡니다.
  - 왜 쓰는가: 모바일 네트워크나 손실이 있는 환경에서 더 빠르고 끊김 적은 통신 경험을 제공하기 위해서입니다.
  - 참고 링크:
    - RFC 9114 HTTP/3: https://datatracker.ietf.org/doc/html/rfc9114
    - RFC 9000 QUIC: https://datatracker.ietf.org/doc/html/rfc9000

- **멀티플렉싱**
  - 핵심: 하나의 연결 안에서 여러 요청과 응답을 스트림 단위로 동시에 주고받는 방식입니다.
  - 왜 쓰는가: 리소스 하나가 끝날 때까지 다음 요청이 기다리는 비효율을 줄일 수 있기 때문입니다.
  - 참고 링크:
    - RFC 9113 HTTP/2: https://datatracker.ietf.org/doc/html/rfc9113

- **HPACK과 QPACK**
  - 핵심: HPACK은 HTTP/2의 헤더 압축 방식이고, QPACK은 HTTP/3의 헤더 압축 방식입니다.
  - 왜 쓰는가: 반복되는 헤더 전송량을 줄여 네트워크 효율을 높이기 위해서입니다.
  - 참고 링크:
    - RFC 7541 HPACK: https://datatracker.ietf.org/doc/html/rfc7541
    - RFC 9204 QPACK: https://datatracker.ietf.org/doc/html/rfc9204

## 6. 실행·검증 방법

### 6.1 브라우저 개발자도구로 프로토콜 확인

1. 브라우저에서 임의의 웹 사이트에 접속합니다.
2. 개발자도구(`F12`)의 `Network` 탭에서 `Protocol` 컬럼을 활성화합니다.
3. 각 요청이 `http/1.1`, `h2`, `h3` 중 어떤 프로토콜로 처리되는지 확인합니다.

예상 결과:
- 서비스와 브라우저 지원 상태에 따라 `h2` 또는 `h3`가 보일 수 있습니다.
- 동일한 사이트라도 요청 종류에 따라 다른 프로토콜이 쓰일 수 있습니다.

### 6.2 `curl`로 HTTP/1.1과 HTTP/2 확인

```bash
curl -I --http1.1 https://nghttp2.org/httpbin/get
curl -I --http2 https://nghttp2.org/httpbin/get
```

예상 결과:
- 각 요청이 지정한 HTTP 버전으로 협상되거나, 서버가 지원하지 않으면 실패 메시지가 출력될 수 있습니다.
- `curl -I -v`를 함께 사용하면 실제 협상된 프로토콜 정보를 더 자세히 볼 수 있습니다.

### 6.3 `curl`로 HTTP/3 확인

```bash
curl -I --http3 https://nghttp2.org/httpbin/get
```

예상 결과:
- 현재 설치된 `curl`이 HTTP/3을 지원하고, 대상 서버도 HTTP/3을 지원하면 헤더가 반환됩니다.
- 환경에 따라 `curl: option --http3` 관련 오류가 날 수 있는데, 이 경우 클라이언트 도구가 HTTP/3을 지원하지 않는 것입니다.

### 6.4 테스트 실행

이 태스크는 문서 중심 학습이라 별도 테스트 코드를 추가하지 않았습니다.

## 7. 결과 확인 방법(스크린샷 포함)

- 성공 기준:
  - HTTP/1.1, HTTP/2, HTTP/3의 차이를 표를 보지 않고도 핵심 문장으로 설명할 수 있다.
  - HTTP/2가 멀티플렉싱과 헤더 압축으로 HTTP/1.1의 병목을 줄였다는 점을 이해할 수 있다.
  - HTTP/3가 QUIC을 통해 TCP 기반 한계를 완화했다는 점을 설명할 수 있다.
- 스크린샷 파일명과 저장 위치:
  - 브라우저 프로토콜 컬럼 캡처: `docs/mission-03-http-web-basic/task-06-http-version-comparison/browser-protocol-column.png`
  - `curl --http2` 실행 결과 캡처: `docs/mission-03-http-web-basic/task-06-http-version-comparison/curl-http2-check.png`
  - `curl --http3` 실행 결과 캡처: `docs/mission-03-http-web-basic/task-06-http-version-comparison/curl-http3-check.png`
- 권장 캡처 포인트:
  - 브라우저 `Network` 탭의 `Protocol` 컬럼
  - 터미널에서 `curl -I -v --http2` 결과
  - 터미널에서 `curl -I -v --http3` 결과 또는 지원하지 않을 때의 안내 메시지

## 8. 학습 내용

- HTTP/2와 HTTP/3은 단순히 버전 숫자가 올라간 것이 아니라, 웹 성능 병목이 어디서 생기는지에 대한 대응 방향이 다릅니다.
- HTTP/2는 "하나의 연결을 얼마나 효율적으로 쓸 것인가"에 집중했고, HTTP/3는 "전송 계층까지 포함해 지연과 손실을 얼마나 줄일 것인가"에 더 집중했습니다.
- HTTP/1.1이 아직도 널리 쓰이지만, 리소스 수가 많고 지연이 민감한 현대 웹 환경에서는 HTTP/2와 HTTP/3의 이점이 더 크게 드러납니다.
- 실무에서는 프로토콜 차이를 암기하는 것보다, 왜 멀티플렉싱, 헤더 압축, QUIC 같은 개념이 등장했는지를 병목 관점에서 이해하는 편이 더 중요합니다.
