package com.mitocode.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mitocode.model.Cliente;
import com.mitocode.repo.IClienteRepo;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.service.IClienteService;

@Service
public class ClienteServiceImpl extends CRUDImpl<Cliente, String> implements IClienteService{

	@Autowired
	private IClienteRepo repo;

	@Override
	protected IGenericRepo<Cliente, String> getRepo() {
		return repo;
	}
	
	

}
