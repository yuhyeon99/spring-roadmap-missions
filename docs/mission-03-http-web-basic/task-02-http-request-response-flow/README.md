# HTTP 요청과 응답 흐름 이해하기

`mission-03-http-web-basic`의 `task-02-http-request-response-flow` 보고서입니다. 브라우저 주소창에 URL을 입력했을 때 브라우저, 네트워크, 서버가 어떤 순서로 동작하는지 단계별로 정리했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-03-http-web-basic` / `task-02-http-request-response-flow`
- 목표:
  - URL 입력부터 응답 렌더링까지의 흐름을 브라우저 관점에서 순서대로 이해한다.
  - DNS 조회, 연결 수립, HTTP 메시지 전송, 서버 처리, 브라우저 렌더링의 역할을 구분해 설명한다.
  - 개발자도구와 `curl`로 요청/응답 흐름을 직접 확인할 수 있는 검증 절차를 정리한다.
- 관찰 예시 URL: `http://localhost:8080/mission03/task01/notes`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Doc | `docs/mission-03-http-web-basic/task-02-http-request-response-flow/README.md` | 요청/응답 흐름 단계 설명, 검증 방법, 학습 내용 정리 |
| Diagram | `docs/mission-03-http-web-basic/task-02-http-request-response-flow/http-request-response-flow.mmd` | URL 입력 후 응답 렌더링까지의 전체 흐름 Mermaid 다이어그램 |
| Etc | `docs/mission-03-http-web-basic/task-02-http-request-response-flow/sample-http-message.txt` | 브라우저가 보내는 HTTP 요청과 서버 응답의 예시 메시지 |

## 3. 구현 단계와 주요 코드 해설

### 3.1 전체 흐름 다이어그램

```mermaid
flowchart TD
    A["1. 브라우저 주소창에 URL 입력<br/>예: http://localhost:8080/mission03/task01/notes"] --> B["2. 브라우저가 URL 파싱<br/>scheme / host / port / path / query 분리"]
    B --> C{"3. 캐시 또는 DNS 정보 존재?"}
    C -- "예" --> D["IP 주소 확보"]
    C -- "아니오" --> E["DNS 조회로 도메인을 IP 주소로 변환"]
    E --> D
    D --> F{"4. HTTPS 인가?"}
    F -- "아니오" --> G["TCP 연결 수립<br/>3-way handshake"]
    F -- "예" --> G
    G --> H["5. HTTPS면 TLS handshake 수행<br/>인증서 검증 + 암호화 키 협상"]
    H --> I["6. 브라우저가 HTTP 요청 메시지 생성<br/>Request Line + Headers + Body"]
    I --> J["7. 서버(웹 서버/애플리케이션)가 요청 수신"]
    J --> K["8. 라우팅 후 컨트롤러/핸들러 실행<br/>필요 시 서비스/DB 호출"]
    K --> L["9. 서버가 HTTP 응답 메시지 생성<br/>Status Line + Headers + Body"]
    L --> M["10. 브라우저가 응답 해석<br/>상태 코드, 헤더, 본문 처리"]
    M --> N["11. HTML/CSS/JS/이미지 등 추가 리소스 요청 여부 판단"]
    N --> O["12. 화면 렌더링 또는 데이터 표시"]
```

### 3.2 단계별 설명

1. **URL 입력**
   - 사용자가 브라우저 주소창에 `http://localhost:8080/mission03/task01/notes` 같은 URL을 입력합니다.
   - 이 시점에는 아직 서버로 바이트가 전송되지 않았고, 브라우저가 먼저 입력값을 해석할 준비를 합니다.

2. **URL 파싱**
   - 브라우저는 URL을 `scheme(http)`, `host(localhost)`, `port(8080)`, `path(/mission03/task01/notes)`로 나눕니다.
   - `#fragment`가 있다면 이는 브라우저 내부 이동 용도이므로 서버로 전달되지 않습니다.

3. **캐시 확인과 DNS 조회**
   - 브라우저는 먼저 자체 DNS 캐시, 운영체제 캐시, 로컬 hosts 정보를 확인합니다.
   - 도메인 이름의 IP 주소를 모르면 DNS 조회를 수행합니다.
   - `localhost`는 보통 로컬 루프백 주소 `127.0.0.1` 또는 `::1`로 바로 해석됩니다.

4. **TCP 연결 수립**
   - HTTP/1.1, HTTP/2 모두 전송 기반 연결이 먼저 필요합니다.
   - 브라우저는 서버와 TCP 3-way handshake를 수행해 신뢰성 있는 연결을 만듭니다.

5. **TLS handshake(HTTPS일 때)**
   - URL이 `https://`라면 TCP 연결 후 TLS handshake가 이어집니다.
   - 이 과정에서 서버 인증서를 검증하고, 이후 데이터를 암호화할 키를 협상합니다.
   - `http://`라면 이 단계 없이 바로 HTTP 요청으로 넘어갑니다.

6. **HTTP 요청 메시지 생성**
   - 브라우저는 요청 라인, 헤더, 본문을 조합해 HTTP 요청 메시지를 만듭니다.
   - 단순 조회라면 보통 `GET` 메서드와 헤더 중심 요청이 전송되고, `POST`나 `PUT`은 본문이 함께 전달됩니다.
   - 예: `GET /mission03/task01/notes HTTP/1.1`, `Host: localhost:8080`, `Accept: application/json`

7. **서버 수신과 라우팅**
   - 웹 서버 또는 애플리케이션 서버가 요청을 수신합니다.
   - 스프링 애플리케이션이라면 `DispatcherServlet`이 요청을 받고, 어떤 컨트롤러 메서드가 처리할지 매핑합니다.

8. **비즈니스 로직 실행**
   - 컨트롤러는 요청값을 읽고 서비스 계층을 호출합니다.
   - 필요하면 저장소나 DB와 통신한 뒤, 응답에 필요한 데이터를 준비합니다.

9. **HTTP 응답 메시지 생성**
   - 서버는 처리 결과를 상태 코드, 헤더, 본문으로 구성합니다.
   - 예를 들어 정상 조회라면 `200 OK`, `Content-Type: application/json`, 응답 본문 `[]` 같은 형태가 됩니다.

10. **브라우저의 응답 해석**
   - 브라우저는 응답 상태 코드가 성공인지, 리다이렉트인지, 오류인지 먼저 확인합니다.
   - 이어서 헤더를 보고 본문 타입(JSON, HTML, 이미지 등), 캐시 가능 여부, 압축 여부를 판단합니다.

11. **추가 리소스 요청**
   - 응답이 HTML 문서라면 브라우저는 내부의 CSS, JS, 이미지 링크를 다시 읽고 추가 HTTP 요청을 만듭니다.
   - 그래서 사용자가 한 번 URL을 입력해도 실제 네트워크 탭에는 여러 요청이 보일 수 있습니다.

12. **렌더링 또는 데이터 표시**
   - HTML이면 화면을 렌더링하고, JSON이면 브라우저가 원문을 보여주거나 자바스크립트가 데이터를 활용합니다.
   - 사용자가 실제로 보는 최종 화면은 이 마지막 단계의 결과입니다.

### 3.3 예시 요청/응답 메시지

아래 예시는 `GET /mission03/task01/notes` 요청이 오갈 때 메시지가 어떤 모습인지 단순화해 보여줍니다.

```text
GET /mission03/task01/notes HTTP/1.1
Host: localhost:8080
Accept: application/json
User-Agent: Mozilla/5.0

HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 2

[]
```

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `http-request-response-flow.mmd`

- 파일 경로: `docs/mission-03-http-web-basic/task-02-http-request-response-flow/http-request-response-flow.mmd`
- 역할: 요청/응답 흐름을 시각적으로 요약하는 Mermaid 다이어그램
- 상세 설명:
  - 브라우저 입력부터 렌더링까지의 순서를 하나의 플로우로 연결합니다.
  - DNS 조회, TCP/TLS 연결, HTTP 요청 생성, 서버 처리, 응답 해석 단계를 분리해 보여줍니다.
  - README 본문에 동일한 다이어그램을 삽입할 때 재사용할 원본 파일 역할도 합니다.

<details>
<summary><code>http-request-response-flow.mmd</code> 전체 코드</summary>

```text
flowchart TD
    A["1. 브라우저 주소창에 URL 입력<br/>예: http://localhost:8080/mission03/task01/notes"] --> B["2. 브라우저가 URL 파싱<br/>scheme / host / port / path / query 분리"]
    B --> C{"3. 캐시 또는 DNS 정보 존재?"}
    C -- "예" --> D["IP 주소 확보"]
    C -- "아니오" --> E["DNS 조회로 도메인을 IP 주소로 변환"]
    E --> D
    D --> F{"4. HTTPS 인가?"}
    F -- "아니오" --> G["TCP 연결 수립<br/>3-way handshake"]
    F -- "예" --> G
    G --> H["5. HTTPS면 TLS handshake 수행<br/>인증서 검증 + 암호화 키 협상"]
    H --> I["6. 브라우저가 HTTP 요청 메시지 생성<br/>Request Line + Headers + Body"]
    I --> J["7. 서버(웹 서버/애플리케이션)가 요청 수신"]
    J --> K["8. 라우팅 후 컨트롤러/핸들러 실행<br/>필요 시 서비스/DB 호출"]
    K --> L["9. 서버가 HTTP 응답 메시지 생성<br/>Status Line + Headers + Body"]
    L --> M["10. 브라우저가 응답 해석<br/>상태 코드, 헤더, 본문 처리"]
    M --> N["11. HTML/CSS/JS/이미지 등 추가 리소스 요청 여부 판단"]
    N --> O["12. 화면 렌더링 또는 데이터 표시"]
```

</details>

### 4.2 `sample-http-message.txt`

- 파일 경로: `docs/mission-03-http-web-basic/task-02-http-request-response-flow/sample-http-message.txt`
- 역할: HTTP 요청/응답 메시지 예시 제공
- 상세 설명:
  - 브라우저가 서버에 보내는 요청 줄과 주요 헤더, 서버가 반환하는 상태 줄과 응답 헤더를 간단히 보여줍니다.
  - 실제 패킷 전체를 그대로 복사한 것은 아니고, 학습용으로 핵심 필드만 남긴 샘플입니다.
  - 문서의 단계 설명을 텍스트 메시지 형태로 다시 확인할 때 사용합니다.

<details>
<summary><code>sample-http-message.txt</code> 전체 코드</summary>

```text
GET /mission03/task01/notes HTTP/1.1
Host: localhost:8080
Accept: application/json
User-Agent: Mozilla/5.0
Connection: keep-alive

HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 2

[]
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **URL 구성 요소**
  - 핵심: URL은 `scheme`, `host`, `port`, `path`, `query`, `fragment`로 나뉘며, 서버에는 보통 `fragment`를 제외한 정보가 전달됩니다.
  - 왜 쓰는가: 브라우저가 어디에 어떤 방식으로 연결할지 정확히 판단하려면 URL을 구조적으로 해석해야 합니다.
  - 참고 링크:
    - RFC 3986: https://datatracker.ietf.org/doc/html/rfc3986
    - MDN URL 이해하기: https://developer.mozilla.org/ko/docs/Learn_web_development/Howto/Web_mechanics/What_is_a_URL

- **DNS 조회**
  - 핵심: 사람이 읽는 도메인 이름을 실제 통신 가능한 IP 주소로 변환하는 과정입니다.
  - 왜 쓰는가: 네트워크 연결은 IP 주소를 기준으로 이루어지므로, DNS 없이 대부분의 웹 서버에 도달할 수 없습니다.
  - 참고 링크:
    - MDN DNS 동작 방식: https://developer.mozilla.org/ko/docs/Learn_web_development/Howto/Web_mechanics/How_DNS_works
    - RFC 1034: https://datatracker.ietf.org/doc/html/rfc1034

- **TCP와 TLS handshake**
  - 핵심: TCP는 신뢰성 있는 바이트 전송 채널을 만들고, TLS는 HTTPS 통신을 암호화하고 서버 신원을 검증합니다.
  - 왜 쓰는가: HTTP 메시지가 중간에서 깨지지 않고, 필요할 때는 안전하게 전달되도록 보장해야 하기 때문입니다.
  - 참고 링크:
    - RFC 9293(TCP): https://datatracker.ietf.org/doc/html/rfc9293
    - RFC 8446(TLS 1.3): https://datatracker.ietf.org/doc/html/rfc8446

- **HTTP 메시지 구조**
  - 핵심: 요청은 Request Line/Headers/Body, 응답은 Status Line/Headers/Body로 이루어집니다.
  - 왜 쓰는가: 클라이언트와 서버가 같은 규약으로 메시지를 이해해야 서로 다른 기술 스택끼리도 통신할 수 있습니다.
  - 참고 링크:
    - RFC 9110(HTTP Semantics): https://datatracker.ietf.org/doc/html/rfc9110
    - RFC 9112(HTTP/1.1): https://datatracker.ietf.org/doc/html/rfc9112

- **브라우저 렌더링과 추가 요청**
  - 핵심: 브라우저는 첫 응답을 받은 뒤 끝나는 것이 아니라, HTML 안의 CSS/JS/이미지 링크를 읽고 추가 요청을 발생시킵니다.
  - 왜 쓰는가: 사용자가 보는 최종 화면은 단일 응답이 아니라 여러 리소스가 합쳐져 만들어지기 때문입니다.
  - 참고 링크:
    - MDN 브라우저의 동작 방식: https://developer.mozilla.org/ko/docs/Web/Performance/How_browsers_work
    - web.dev 렌더링 성능 기초: https://web.dev/learn/performance/

## 6. 실행·검증 방법

### 6.1 애플리케이션 실행

```bash
./gradlew bootRun
```

예상 결과:
- 스프링 부트 애플리케이션이 `http://localhost:8080`에서 실행됩니다.

### 6.2 브라우저 개발자도구로 흐름 확인

1. 브라우저에서 `http://localhost:8080/mission03/task01/notes`에 접속합니다.
2. 개발자도구(`F12`)를 열고 `Network` 탭을 선택합니다.
3. 페이지를 새로고침한 뒤 첫 요청을 클릭해 `Headers`, `Response`, `Timing`을 확인합니다.

성공 기준:
- 요청 URL, 요청 메서드, 상태 코드(`200 OK`)가 보입니다.
- Request Headers와 Response Headers를 각각 확인할 수 있습니다.

### 6.3 `curl`로 요청/응답 메시지 확인

```bash
curl -v http://localhost:8080/mission03/task01/notes
```

예상 결과:
- `> GET /mission03/task01/notes HTTP/1.1` 형태의 요청 라인이 출력됩니다.
- `< HTTP/1.1 200` 형태의 응답 상태 줄과 주요 헤더가 출력됩니다.
- 본문으로 `[]` 또는 현재 저장된 노트 목록 JSON이 출력됩니다.

### 6.4 테스트 실행

이 태스크는 문서 중심 학습이므로 별도 테스트 코드를 추가하지 않았습니다. 대신 기존 미션 3 task01 API가 정상 동작하는지 확인하려면 아래 명령으로 관련 테스트를 실행할 수 있습니다.

```bash
./gradlew test --tests "*HttpMethodsDemoControllerTest"
```

예상 결과:
- HTTP 메서드 데모 API 테스트가 통과하며, 요청/응답 관찰용 엔드포인트가 정상 동작함을 확인할 수 있습니다.

## 7. 결과 확인 방법(스크린샷 포함)

- 성공 기준:
  - 브라우저 네트워크 탭에서 요청 URL, 상태 코드, 요청/응답 헤더, 응답 본문이 확인된다.
  - `curl -v` 출력에서 요청 라인과 응답 상태 줄이 순서대로 보인다.
  - 문서의 다이어그램 단계와 실제 관찰 결과가 크게 어긋나지 않는다.
- 스크린샷 파일명과 저장 위치:
  - 브라우저 네트워크 탭 캡처: `docs/mission-03-http-web-basic/task-02-http-request-response-flow/network-tab-request-response.png`
  - `curl -v` 실행 결과 캡처: `docs/mission-03-http-web-basic/task-02-http-request-response-flow/curl-verbose-result.png`
- 권장 캡처 포인트:
  - `Headers` 탭에서 Request URL, Request Method, Status Code
  - `Response` 탭에서 JSON 본문
  - 터미널에서 요청 라인(`>`)과 응답 상태 줄(`<`)이 동시에 보이는 화면

## 8. 학습 내용

- 브라우저에 URL을 입력했다고 해서 즉시 애플리케이션 코드가 실행되는 것은 아닙니다. URL 해석, 주소 확인, 연결 수립 같은 네트워크 준비 단계가 먼저 진행됩니다.
- HTTP는 메시지 규약이고, TCP/TLS는 그 메시지를 실제로 전달하는 기반입니다. 둘의 역할을 구분하면 요청 흐름을 더 정확히 이해할 수 있습니다.
- 서버가 응답을 한 번 보냈다고 해서 화면 구성이 끝나는 것은 아닙니다. HTML 안에 포함된 다른 리소스 때문에 브라우저는 추가 요청을 계속 만들 수 있습니다.
- 개발자도구의 Network 탭과 `curl -v`를 함께 보면 추상적인 설명이 실제 요청/응답 메시지와 바로 연결됩니다.
