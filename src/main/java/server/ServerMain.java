package server;

/* 전부 직접 작성한 커스텀 라이브러리 */
import java.io.IOException; /* 서버 설정 파일 import*/
import java.nio.file.Files; /* 네트워크 수락기 import */

import server.config.ServerConfig; /* 라우터 import */
import server.core.NetAcceptor; /* POST 핸들러 import */
import server.route.AuthHandler; /* 정적 파일 핸들러 import */
import server.route.Router; /* 로그인/회원가입 핸들러 import - 로그인 기능용 */
import server.route.SimplePostHandler; /* 로거 유틸리티 import */
import server.route.StaticFileHandler;
import server.util.Logger;

/**
 * 서버 실행 진입점.
 * 정적 파일 루트를 준비하고, 라우터/네트워크 수락기를 초기화한 뒤 서버 루프를 시작한다.
 */
public final class ServerMain {

    public static void main(String[] args) {
        ensureWebRoot(); // 기본 www 디렉터리와 index.html 생성
        
        // === 로그인 기능 추가 (팀원 작업) ===
        // 정적 파일을 처리하는 핸들러
        StaticFileHandler staticHandler = new StaticFileHandler(ServerConfig.WEB_ROOT); // www 디렉토리가 루트가 됨
        SimplePostHandler postHandler = new SimplePostHandler(); // POST 요청을 단순히 에코해주는 핸들러
        
        // 기존 Router 생성자에 로그인 핸들러도 함께 전달
        AuthHandler authHandler = new AuthHandler(); // 로그인/회원가입 처리용
        Router router = new Router(staticHandler, postHandler, authHandler); // 로그인 기능이 추가된 라우터
        // NetAcceptor가 실질적으로 소켓 수락과 워커 스케줄링을 담당한다.
        NetAcceptor acceptor = new NetAcceptor(router);
        // JVM 종료 시점에도 서버가 깔끔히 내려가도록 훅을 등록한다.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                acceptor.stop();
            } catch (IOException e) {
                Logger.error("Error during shutdown", e);
            }
        }));
        try {
            acceptor.start();
        } catch (IOException e) {
            Logger.error("Failed to start server", e);
        }
    }

    /**
     * www 디렉터리가 없으면 생성하고, 기본 index.html 이 없을 경우 생성한다.
     */
    private static void ensureWebRoot() {
        try {
            Files.createDirectories(ServerConfig.WEB_ROOT);
            var index = ServerConfig.WEB_ROOT.resolve("index.html");
            if (Files.notExists(index)) {
                Files.writeString(index, """
                        <!DOCTYPE html>
                        <html>
                        <head>
                          <meta charset="UTF-8">
                          <title>Simple Java Server</title>
                        </head>
                        <body>
                          <h1>Simple Java Server</h1>
                          <p>Your server is up and serving files from the <code>www</code> directory.</p>
                        </body>
                        </html>
                        """);
            }
        } catch (IOException e) {
            Logger.error("Could not create web root directory", e);
        }
    }
}
