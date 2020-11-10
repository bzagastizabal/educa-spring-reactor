package com.mitocode.controller;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;
import static reactor.function.TupleUtils.function;

import java.net.URI;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.mitocode.dto.PlatoClienteDTO;
import com.mitocode.model.Cliente;
import com.mitocode.model.Plato;
import com.mitocode.pagination.PageSupport;
import com.mitocode.service.IClienteService;
import com.mitocode.service.IPlatoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/platos")
public class PlatoController {
	
	private static final Logger log = LoggerFactory.getLogger(PlatoController.class);
	
	@Autowired
	private IPlatoService service;
	
	@Autowired
	private IClienteService clienteService;
	
	@GetMapping
	public Mono<ResponseEntity<Flux<Plato>>> listar(){
		Flux<Plato> fxPlatos = service.listar();
		
		service.listar().subscribe(i -> log.info(i.toString()));
		//service.listar().subscribeOn(Schedulers.single());
		//service.listar().parallel().runOn(Schedulers.parallel()).subscribe(i -> log.info(i.toString()));
		//service.listar().subscribeOn(Schedulers.parallel()).subscribe(i -> log.info(i.toString()));
		
		return Mono.just(ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(fxPlatos)
				);
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Plato>> listarPorId(@PathVariable("id") String id){
		return service.listarPorId(id) 				//Mono<Plato>
					.map(p -> ResponseEntity.ok()  //Mono<ResponseEntity> 
							.contentType(MediaType.APPLICATION_JSON)
							.body(p)
					)
					.defaultIfEmpty(ResponseEntity.notFound().build());		
	}

	@PostMapping
	public Mono<ResponseEntity<Plato>> registrar(@Valid @RequestBody Plato plato, final ServerHttpRequest req){
		//localhost:8080/platos/123
		return service.registrar(plato)
				.map(p -> ResponseEntity.created(URI.create(req.getURI().toString().concat("/").concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(p)
				);
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Plato>> modificar(@Valid @RequestBody Plato plato, @PathVariable("id") String id){
		
		Mono<Plato> monoPlato = Mono.just(plato);
		Mono<Plato> monoBD = service.listarPorId(id);
		
		return monoBD
				.zipWith(monoPlato, (bd, p) -> {
					bd.setId(id);
					bd.setNombre(p.getNombre());
					bd.setPrecio(p.getPrecio());
					bd.setEstado(p.getEstado());
					return bd;
				})
				.flatMap(service::modificar) //platoNuevo -> service.modificar(platoNuevo)
				.map(p -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(p)
				).defaultIfEmpty(new ResponseEntity<Plato>(HttpStatus.NOT_FOUND));
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> eliminar(@PathVariable("id") String id){
		return service.listarPorId(id)
				.flatMap(p -> {
					return service.eliminar(p.getId())
							.then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
				})
				.defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}
	
	private Plato platoHateoas;
	
	@GetMapping("/hateoas/{id}")
	public Mono<EntityModel<Plato>> listarHateoasPorId(@PathVariable("id") String id){
		//  /platos/{5}
		Mono<Link> link1 = linkTo(methodOn(PlatoController.class).listarPorId(id)).withSelfRel().toMono();
		Mono<Link> link2 = linkTo(methodOn(PlatoController.class).listarPorId(id)).withSelfRel().toMono();
		
		//PRACTICA NO RECOMENDADA
		/*return service.listarPorId(id)
				.flatMap(p -> {
					this.platoHateoas = p;
					return link1;
				})
				.map(lk -> {
					return EntityModel.of(this.platoHateoas, lk);
				});*/
		
		//PRACTICA INTEMERDIA
		/*return service.listarPorId(id)
				.flatMap(p -> {
					return link1.map(lk -> EntityModel.of(p, lk));
				});*/
		
		//PRACTICA IDEAL
		/*return service.listarPorId(id)
				.zipWith(link1, (p, lk) -> EntityModel.of(p, lk));*/
		
		//Más de 1 link
		return link1.zipWith(link2)
				.map(function((left, right) -> Links.of(left, right)))				
				.zipWith(service.listarPorId(id), (lk, p) -> EntityModel.of(p, lk));		
	}
	
	@GetMapping("/pageable")
	public Mono<ResponseEntity<PageSupport<Plato>>> listarPagebale(
			@RequestParam(name = "page", defaultValue = "0") int page,
		    @RequestParam(name = "size", defaultValue = "10") int size
			){
		
		Pageable pageRequest = PageRequest.of(page, size);
		
		return service.listarPage(pageRequest)
				.map(p -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(p)	
						)
				.defaultIfEmpty(ResponseEntity.noContent().build());	
	}
	
	@GetMapping("/client1")
	public Flux<Plato> listarClient1(){
		Flux<Plato> fx = WebClient.create("http://localhost:8080/platos")
									.get()
									.retrieve()
									.bodyToFlux(Plato.class);
		return fx;
	}
	
	@GetMapping("/client2")
	public Mono<ResponseEntity<PlatoClienteDTO>> listarClient2() {
		Mono<Plato> plato = service.listarPorId("5f90ee5993e5423323c38fcb").defaultIfEmpty(new Plato());
		Mono<Cliente> cliente = clienteService.listarPorId("5f9a18096beb341771335c3").defaultIfEmpty(new Cliente());
		
		return Mono.zip(cliente,  plato, PlatoClienteDTO::new)
	    		.map(pc -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(pc)
				).defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
}
