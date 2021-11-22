package org.generation.redesocial.conecteme.service;

import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.generation.redesocial.conecteme.dtos.UserLoginDTO;
import org.generation.redesocial.conecteme.model.UsuarioModel;
import org.generation.redesocial.conecteme.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
	
	@Autowired
	private UsuarioRepository usuarioRepository;

	public Optional<UsuarioModel> cadastrarUsuario(UsuarioModel usuario)  {
		
		if(usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já existe", null);
		
		usuario.setSenha(criptografarSenha(usuario.getSenha()));
		return Optional.of(usuarioRepository.save(usuario));
				
	}
	
    public Optional<UsuarioModel> atualizarUsuario(UsuarioModel usuario) {
		
		if(usuarioRepository.findById(usuario.getId()).isPresent()) {
			Optional<UsuarioModel> buscaUsuario = usuarioRepository.findByUsuario(usuario.getUsuario());
			
			if (buscaUsuario.isPresent()) {
				if(buscaUsuario.get().getId() != usuario.getId())
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já existe", null);
			}
			
			usuario.setSenha(criptografarSenha(usuario.getSenha()));
			return Optional.of(usuarioRepository.save(usuario));
			
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado", null);
			
	}
	
	public Optional<UserLoginDTO> logarUsuario(Optional<UserLoginDTO> userLogin){
		Optional<UsuarioModel> usuario = usuarioRepository.findByUsuario(userLogin.get().getUsuario());
		
		if(usuario.isPresent()) {
			if(compararSenhas(userLogin.get().getSenha(), usuario.get().getSenha())) {
				
				userLogin.get().setId(usuario.get().getId());				
				userLogin.get().setSenha(usuario.get().getSenha());
				userLogin.get().setToken(generatorBasicToken(userLogin.get().getUsuario(), userLogin.get().getSenha()));
				
				return userLogin;
				
			}	
		}
		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos", null);
			
	}
	
	private String criptografarSenha(String senha) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String senhaEncoder = encoder.encode(senha);
		return senhaEncoder;
	
	}
	
	private boolean compararSenhas(String senhaDigitada, String senhaBanco) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder.matches(senhaDigitada, senhaBanco);
		
	}	
	
	private String generatorBasicToken(String usuario, String password) {
		String structure = usuario + ":" + password;
		byte[] structureBase64 = Base64.encodeBase64(structure.getBytes(Charset.forName("US-ASCII")));
		return "Basic " + new String(structureBase64);
				
	}
}
