package com.municipioactivo.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
// Verifica que el contexto completo de Spring Boot pueda inicializarse correctamente.
class BackendApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	// Comprueba que la aplicación arranque sin errores de configuración.
	void contextLoads() {
	}

	@Test
	void servesLoginPageFromBackend() throws Exception {
		var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/login.html")).GET().build();
		var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("municipio-api-base-url"));
	}

	@Test
	void registersUserThroughHttp() throws Exception {
		String email = "usuario-" + UUID.randomUUID() + "@example.com";
		String body = "{\"nombre\":\"Usuario de prueba\",\"email\":\"" + email + "\",\"password\":\"Clave26\"}";
		var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/registro"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();
		var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(201, response.statusCode());
		assertTrue(response.body().contains(email));
	}

	@Test
	void noticiasCanBeManagedOnlyByAdministrativeUsers() throws Exception {
		HttpClient publicClient = HttpClient.newHttpClient();
		var publicListRequest = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/noticias"))
				.GET()
				.build();
		assertEquals(200, publicClient.send(publicListRequest, HttpResponse.BodyHandlers.ofString()).statusCode());

		String requestBody = """
				{"category":"Gestión","title":"Noticia de prueba","summary":"Resumen de prueba",
				"content":"Contenido de prueba","image":"","imageAlt":"","featured":false}
				""";
		var unauthorizedRequest = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/noticias"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();
		assertEquals(403, publicClient.send(unauthorizedRequest, HttpResponse.BodyHandlers.ofString()).statusCode());

		String email = "administrador-" + UUID.randomUUID() + "@example.com";
		String password = "Clave26";
		usuarioRepository.save(new Usuario(
				email, passwordEncoder.encode(password), "Administrador de prueba", "administrativo", ""));

		try {
			CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
			HttpClient adminClient = HttpClient.newBuilder().cookieHandler(cookies).build();
			var loginRequest = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/login"))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(
							"{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
					.build();
			assertEquals(200, adminClient.send(loginRequest, HttpResponse.BodyHandlers.ofString()).statusCode());

			var createRequest = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/noticias"))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(requestBody))
					.build();
			var createResponse = adminClient.send(createRequest, HttpResponse.BodyHandlers.ofString());
			assertEquals(201, createResponse.statusCode());
			Matcher idMatcher = Pattern.compile("\"id\":\"([^\"]+)\"").matcher(createResponse.body());
			assertTrue(idMatcher.find());
			String id = idMatcher.group(1);

			var updateRequest = HttpRequest.newBuilder(
					URI.create("http://localhost:" + port + "/api/noticias/" + id))
					.header("Content-Type", "application/json")
					.PUT(HttpRequest.BodyPublishers.ofString(requestBody.replace(
							"Noticia de prueba", "Noticia modificada")))
					.build();
			assertEquals(200, adminClient.send(updateRequest, HttpResponse.BodyHandlers.ofString()).statusCode());

			var deleteRequest = HttpRequest.newBuilder(
					URI.create("http://localhost:" + port + "/api/noticias/" + id))
					.DELETE()
					.build();
			assertEquals(204, adminClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString()).statusCode());
		} finally {
			usuarioRepository.findByEmailIgnoreCase(email).ifPresent(usuarioRepository::delete);
		}
	}

}
