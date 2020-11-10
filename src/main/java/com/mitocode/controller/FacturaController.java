package com.mitocode.controller;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;
import static reactor.function.TupleUtils.function;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;

import com.mitocode.dto.FiltroDTO;
import com.mitocode.model.Factura;
import com.mitocode.service.IFacturaService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/facturas")
public class FacturaController {
	
	@Autowired
	private IFacturaService service;
	
	@GetMapping
	public Mono<ResponseEntity<Flux<Factura>>> listar(){
		Flux<Factura> fxFacturas = service.listar();
		
		return Mono.just(ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(fxFacturas)
				);
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Factura>> listarPorId(@PathVariable("id") String id){
		return service.listarPorId(id) 				//Mono<Factura>
					.map(p -> ResponseEntity.ok()  //Mono<ResponseEntity> 
							.contentType(MediaType.APPLICATION_JSON)
							.body(p)
					)
					.defaultIfEmpty(ResponseEntity.notFound().build());		
	}

	@PostMapping
	public Mono<ResponseEntity<Factura>> registrar(@Valid @RequestBody Factura Factura, final ServerHttpRequest req){
		//localhost:8080/Facturas/123
		return service.registrar(Factura)
				.map(p -> ResponseEntity.created(URI.create(req.getURI().toString().concat("/").concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(p)
				);
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Factura>> modificar(@Valid @RequestBody Factura Factura, @PathVariable("id") String id){
		
		Mono<Factura> monoFactura = Mono.just(Factura);
		Mono<Factura> monoBD = service.listarPorId(id);
		
		return monoBD
				.zipWith(monoFactura, (bd, p) -> {
					bd.setId(id);
					bd.setCliente(p.getCliente());
					bd.setDescripcion(p.getDescripcion());
					bd.setObservacion(p.getObservacion());
					bd.setItems(p.getItems());					
					return bd;
				})
				.flatMap(service::modificar) //FacturaNuevo -> service.modificar(FacturaNuevo)
				.map(p -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(p)
				).defaultIfEmpty(new ResponseEntity<Factura>(HttpStatus.NOT_FOUND));
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
	
	private Factura FacturaHateoas;
	
	@GetMapping("/hateoas/{id}")
	public Mono<EntityModel<Factura>> listarHateoasPorId(@PathVariable("id") String id){
		//  /Facturas/{5}
		Mono<Link> link1 = linkTo(methodOn(FacturaController.class).listarPorId(id)).withSelfRel().toMono();
		Mono<Link> link2 = linkTo(methodOn(FacturaController.class).listarPorId(id)).withSelfRel().toMono();
		
		//PRACTICA NO RECOMENDADA
		/*return service.listarPorId(id)
				.flatMap(p -> {
					this.FacturaHateoas = p;
					return link1;
				})
				.map(lk -> {
					return EntityModel.of(this.FacturaHateoas, lk);
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
	
	@PostMapping("/buscar")
	public Mono<ResponseEntity<Flux<Factura>>> buscar(@RequestBody FiltroDTO filtro){		
		Flux<Factura> fxPlatos = service.obtenerFacturasPorFiltro(filtro);
		
		return Mono.just(ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(fxPlatos)
				);
	}
	
	@GetMapping("/generarReporte/{id}")
	public Mono<ResponseEntity<byte[]>> generarReporte(@PathVariable("id") String id){		
		
		Mono<byte[]> monoReporte = service.generarReporte(id); 		
	
		return monoReporte
				.map(bytes -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(bytes)
				).defaultIfEmpty(new ResponseEntity<byte[]>(HttpStatus.NO_CONTENT));
	}
	
}
