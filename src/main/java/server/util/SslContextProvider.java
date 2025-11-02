package server.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import server.config.ServerConfig;

/**
 * HTTPS 지원을 위한 SSLContext/Factory 생성 도우미.
 */
public final class SslContextProvider {

    private SslContextProvider() {}

    public static SSLServerSocketFactory serverSocketFactory() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ServerConfig.KEYSTORE_TYPE);
            try (InputStream in = Files.newInputStream(ServerConfig.KEYSTORE_PATH)) {
                keyStore.load(in, ServerConfig.KEYSTORE_PASSWORD.toCharArray());
            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, ServerConfig.KEYSTORE_PASSWORD.toCharArray());

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(kmf.getKeyManagers(), null, null);
            return context.getServerSocketFactory();
        } catch (IOException e) {
            throw new IllegalStateException("키스토어를 읽을 수 없습니다: " + ServerConfig.KEYSTORE_PATH, e);
        } catch (Exception e) {
            throw new IllegalStateException("SSL 컨텍스트 초기화에 실패했습니다.", e);
        }
    }
}
