package com.mitocode.controller;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;
import static reactor.function.TupleUtils.function;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.Map;

import javax.validation.Valid;

import org.cloudinary.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mitocode.model.Cliente;
import com.mitocode.service.IClienteService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
	
	@Autowired
	private IClienteService service;
	
	@GetMapping
	public Mono<ResponseEntity<Flux<Cliente>>> listar(){
		Flux<Cliente> fxClientes = service.listar();
		
		return Mono.just(ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(fxClientes)
				);
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Cliente>> listarPorId(@PathVariable("id") String id){
		return service.listarPorId(id) 				//Mono<Cliente>
					.map(p -> ResponseEntity.ok()  //Mono<ResponseEntity> 
							.contentType(MediaType.APPLICATION_JSON)
							.body(p)
					)
					.defaultIfEmpty(ResponseEntity.notFound().build());		
	}

	@PostMapping
	public Mono<ResponseEntity<Cliente>> registrar(@Valid @RequestBody Cliente Cliente, final ServerHttpRequest req){
		//localhost:8080/Clientes/123
		return service.registrar(Cliente)
				.map(p -> ResponseEntity.created(URI.create(req.getURI().toString().concat("/").concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(p)
				);
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Cliente>> modificar(@Valid @RequestBody Cliente Cliente, @PathVariable("id") String id){
		
		Mono<Cliente> monoCliente = Mono.just(Cliente);
		Mono<Cliente> monoBD = service.listarPorId(id);
		
		return monoBD
				.zipWith(monoCliente, (bd, p) -> {
					bd.setId(id);
					bd.setNombres(p.getNombres());
					bd.setApellidos(p.getApellidos());
					bd.setFechaNac(p.getFechaNac());
					bd.setUrlFoto(p.getUrlFoto());					
					return bd;
				})
				.flatMap(service::modificar) //ClienteNuevo -> service.modificar(ClienteNuevo)
				.map(p -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(p)
				).defaultIfEmpty(new ResponseEntity<Cliente>(HttpStatus.NOT_FOUND));
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
	
	private Cliente ClienteHateoas;
	
	@GetMapping("/hateoas/{id}")
	public Mono<EntityModel<Cliente>> listarHateoasPorId(@PathVariable("id") String id){
		//  /Clientes/{5}
		Mono<Link> link1 = linkTo(methodOn(ClienteController.class).listarPorId(id)).withSelfRel().toMono();
		Mono<Link> link2 = linkTo(methodOn(ClienteController.class).listarPorId(id)).withSelfRel().toMono();
		
		//PRACTICA NO RECOMENDADA
		/*return service.listarPorId(id)
				.flatMap(p -> {
					this.ClienteHateoas = p;
					return link1;
				})
				.map(lk -> {
					return EntityModel.of(this.ClienteHateoas, lk);
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
	
	//Cloudinary, subir archivos a un repo externo, similar a AWS S3
	@PostMapping("subir/{id}")
	public Mono<ResponseEntity<Cliente>> subir(@PathVariable String id, @RequestPart FilePart file){
	
		Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
				"cloud_name", "TU_CLOUD_NAME",
				"api_key", "TU_API_KEY",
				"api_secret", "TU_SECRET"));   
		
		return service.listarPorId(id)
				.flatMap(c -> {
					try {
						File f= Files.createTempFile("temp", file.filename()).toFile();
				        file.transferTo(f);
				        Map response= cloudinary.uploader().upload(f, ObjectUtils.asMap("resource_type", "auto"));
				        
				        JSONObject json=new JSONObject(response);
			            String url=json.getString("url");
			            
				        c.setUrlFoto(url);
				        return service.modificar(c).then(Mono.just(ResponseEntity.ok().body(c)));
					}catch(Exception e) {
						e.printStackTrace();
					}					
			        return Mono.just(ResponseEntity.ok().body(c));
				})
				.defaultIfEmpty(ResponseEntity.notFound().build());		
	}
}
