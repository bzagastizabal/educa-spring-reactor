package com.mitocode.service;

import com.mitocode.model.Menu;

import reactor.core.publisher.Flux;

public interface IMenuService extends ICRUD<Menu, String>{

	Flux<Menu> obtenerMenus(String[] rol);
}
