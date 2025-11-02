package server.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 서버에서 공통으로 사용하는 기본 설정 값들을 모아둔 클래스.
 * 모든 값은 상수이므로 어디서든 직접 참조하면 된다.
 */
public final class ServerConfig {

    private ServerConfig() {}

    /** 서버가 바인딩할 포트 번호 */
    public static final int PORT = 443;

    /** 대기열에 쌓을 수 있는 최대 연결 수 */
    public static final int ACCEPT_BACKLOG = 128;

    /** 워커 스레드 개수(코어 수 * 2, 최소 4개) */
    public static final int WORKER_THREADS = Math.max(4, Runtime.getRuntime().availableProcessors() * 2);

    /** 소켓 읽기 타임아웃(밀리초) */
    public static final int SOCKET_TIMEOUT_MILLIS = 15_000;

    /** 요청 라인(첫 줄)의 최대 길이 */
    public static final int MAX_REQUEST_LINE_LENGTH = 8 * 1024;

    /** 헤더 한 줄의 최대 길이 */
    public static final int MAX_HEADER_LINE_LENGTH = 8 * 1024;

    /** 허용되는 헤더 항목 수 */
    public static final int MAX_HEADERS = 100;

    /** 헤더 전체 크기 상한 */
    public static final int MAX_HEADER_SECTION_SIZE = 32 * 1024;

    /** 본문(Content-Length) 최대 허용 용량 */
    public static final int MAX_BODY_SIZE = 1 * 1024 * 1024;

    /** keep-alive 지속 시간(밀리초) */
    public static final int KEEP_ALIVE_TIMEOUT_MILLIS = 15_000;

    /** 한 연결에서 처리할 최대 요청 수 */
    public static final int KEEP_ALIVE_MAX_REQUESTS = 100;

    /** 정적 파일을 제공할 루트 디렉터리 */
    public static final Path WEB_ROOT = Paths.get("www");

    /** Server 헤더에 노출할 서버 식별자 */
    public static final String SERVER_NAME = "SimpleJavaServer/0.1";

    /** HTTPS 사용 여부 */
    public static final boolean HTTPS_ENABLED = true;

    /** TLS 서버에 사용할 키스토어 정보 */
    public static final String KEYSTORE_TYPE = "PKCS12";
    public static final Path KEYSTORE_PATH = Paths.get("config", "simple-server.p12");
    public static final String KEYSTORE_PASSWORD = "123123";

    /** 허용할 TLS 프로토콜 목록 */
    public static final String[] ENABLED_PROTOCOLS = {"TLSv1.3", "TLSv1.2"};
}
