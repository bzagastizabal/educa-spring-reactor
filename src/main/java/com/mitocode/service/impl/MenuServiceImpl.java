package com.mitocode.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mitocode.model.Menu;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.repo.IMenuRepo;
import com.mitocode.service.IMenuService;

import reactor.core.publisher.Flux;

@Service
public class MenuServiceImpl extends CRUDImpl<Menu, String> implements IMenuService{

	@Autowired
	private IMenuRepo repo;

	@Override
	protected IGenericRepo<Menu, String> getRepo() {		
		return repo; 
	}
	
	@Override
	public Flux<Menu> obtenerMenus(String[] roles) {
		return repo.obtenerMenus(roles);
	}
	
}
