package com.mitocode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.mitocode.model.Plato;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SpringReactorApplicationTests {

	@Autowired
	private WebTestClient clienteWeb;
	
	@Test
	void listarTest() {
		clienteWeb.get()
		.uri("/platos")
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBodyList(Plato.class)
		.hasSize(7);
	}
	
	@Test
	void registrarTest() {
		Plato plato = new Plato();
		plato.setNombre("CEVICHE");
		plato.setPrecio(20.0);
		plato.setEstado(true);
		
		clienteWeb.post()
		.uri("/platos")
		.body(Mono.just(plato), Plato.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.nombre").isNotEmpty()
		.jsonPath("$.precio").isNumber();
	}
	
	@Test
	void modificarTest() {
		Plato plato = new Plato();
		plato.setId("5f4ae59c594d0e4b1cf3408a");
		plato.setNombre("CEVICHEX");
		plato.setPrecio(25.0);
		plato.setEstado(false);
		
		clienteWeb.put()
		.uri("/platos/" + plato.getId())
		.body(Mono.just(plato), Plato.class)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.nombre").isNotEmpty()
		.jsonPath("$.precio").isNumber();
	}
	
	
	@Test
	void eliminarTest() {
		Plato plato = new Plato();
		plato.setId("5f4ae5ffe66cc14f351e780a");
		
		clienteWeb.delete()
		.uri("/platos/" + plato.getId())
		.exchange()
		.expectStatus().isNoContent();
	}
	
}
