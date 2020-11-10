package com.mitocode.repo;

import java.time.LocalDate;

import org.springframework.data.mongodb.repository.Query;

import com.mitocode.model.Factura;

import reactor.core.publisher.Flux;

public interface IFacturaRepo extends IGenericRepo<Factura, String>{

	@Query("{'cliente' : { _id : ?0 }}")
	Flux<Factura> obtenerFacturasPorCliente(String cliente);
	
	@Query("{'creadoEn' : { $gte: ?0, $lt: ?1} }") //Between 02-11-20 >= 04-11-20
	Flux<Factura> obtenerFacturasPorFecha(LocalDate fechaInicio, LocalDate fechaFin);
}
