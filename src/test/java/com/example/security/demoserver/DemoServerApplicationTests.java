package com.example.security.demoserver;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DemoServerApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Value("${spring.ssl.bundle.pem.client.truststore.certificate}")
	private String trustStorePath;

	@Value("${spring.ssl.bundle.pem.client.truststore.private-key-password}")
	private String trustStorePass;

	@Value("${spring.ssl.bundle.pem.client.truststore.type}")
	private String trustStoreType;

	@LocalServerPort
	private int port;

	@Test
	void contextLoads() {
		webTestClient
				.get()
				.uri("/hello")
				.exchange()
				.expectStatus().is4xxClientError()
				.expectBody(String.class);
	}

	@Test
	void contextSecureLoads() throws Exception {

		KeyStore ks;
		try (InputStream kis = new FileInputStream(ResourceUtils.getFile(trustStorePath))) {
			ks = KeyStore.getInstance(trustStoreType);
			char[] keyPass = trustStorePass.toCharArray();

			ks.load(kis, keyPass);

		}

		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(ks);

		SslContext sslContext = SslContextBuilder.forClient()
//		This line accepts all certificates, which is a bad practice but ok for dev phase
//				.trustManager(InsecureTrustManagerFactory.INSTANCE)
				.trustManager(trustManagerFactory)
				.build();

		HttpClient httpClient = HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

		ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

		WebClient client = WebClient.builder()
				.clientConnector(connector)
				.build();

		String response = client.get()
				.uri("https://localhost:" + port + "/hello")
				.retrieve()
				.bodyToMono(String.class)
				.block();

		assertThat(response).isEqualToIgnoringCase("hello world");


	}

}
