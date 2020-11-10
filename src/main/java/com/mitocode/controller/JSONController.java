package com.mitocode.controller;

import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mitocode.model.Plato;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/json")
public class JSONController {
	
	@GetMapping(path = "/noStream", produces = MediaType.APPLICATION_JSON_VALUE)
	public Flux<Plato> noStream(){
		return Flux.interval(Duration.ofMillis(100))
				.map(tick -> new Plato("1", "ARROZ", 20.0, true));
		//[ {}, {}, {} ]
	}
	
	@GetMapping(path = "/stream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
	public Flux<Plato> stream() {
		return Flux.interval(Duration.ofMillis(100))
				.map(tick -> new Plato("1", "ARROZ", 20.0, true));
		//{} {} {} {} {} {} {} {} {} {}
	}
	
	   @GetMapping(path = "/noStreamFinito", produces = MediaType.APPLICATION_JSON_VALUE)
	   public Flux<Plato> fluxFinitonoStream() {
	       return Flux.range(0, 5000)
	               .map(tick -> new Plato("1", "ARROZ", 20.0, true));
	    }
		
		@GetMapping(path = "/streamFinito", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
	    public Flux<Plato> fluxFinitoStream() {
	        return Flux.range(0, 5000)
	                .map(tick -> new Plato("1", "ARROZ", 20.0, true));
	    }
	
	//[ {}, {}, {} ]
	//{} {} {} {} {} {} {} {} {} {}
    @GetMapping(value= "/buffer", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Integer> testContrapresion(){
    	return Flux.range(1, 100) 
    			.log() 
    			//.limitRate(10) // %75 de data drenada/emitida, se pide los restantes
    			//.limitRate(10, 0) //dreno todo y pido 10 más
    			.limitRate(10, 2); // dreno 8 y pido 2 más, capacidad maxima 10
    			
    }

}
