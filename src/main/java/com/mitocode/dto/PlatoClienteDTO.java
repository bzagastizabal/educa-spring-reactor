package com.mitocode.dto;

import com.mitocode.model.Cliente;
import com.mitocode.model.Plato;

public class PlatoClienteDTO {

	private Cliente cliente;
	private Plato plato;	
	
	public PlatoClienteDTO(Cliente cliente, Plato plato) {		
		this.cliente = cliente;
		this.plato = plato;
	}
	public Cliente getCliente() {
		return cliente;
	}
	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	public Plato getPlato() {
		return plato;
	}
	public void setPlato(Plato plato) {
		this.plato = plato;
	}
	
	
}
