package com.mitocode.model;

public class FacturaItem {

	private Integer cantidad;
	
	//@DBRef
	private Plato plato;

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public Plato getPlato() {
		return plato;
	}

	public void setPlato(Plato plato) {
		this.plato = plato;
	}

}
