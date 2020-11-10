package com.mitocode.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/json")
public class JSONController {

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
