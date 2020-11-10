package com.mitocode.service;

import com.mitocode.model.Usuario;
import com.mitocode.security.User;

import reactor.core.publisher.Mono;

public interface IUsuarioService extends ICRUD<Usuario, String>{
	
	Mono<User> buscarPorUsuario(String usuario);
}
