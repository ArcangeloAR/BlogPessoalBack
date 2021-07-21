package org.generation.blogPessoal.service;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.generation.blogPessoal.model.UserLogin;
import org.generation.blogPessoal.model.Usuario;
import org.generation.blogPessoal.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository repository;

	public Optional<Usuario> cadastrarUsuario(Usuario usuario) {
		
		if(repository.findByUsuario(usuario.getUsuario()).isPresent()) {
			return null;
		} else {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

			String senhaEncoder = encoder.encode(usuario.getSenha());
			usuario.setSenha(senhaEncoder);

			return Optional.of(repository.save(usuario));
		}
	
	}
	
	public Optional<Usuario> modificarUsuario(Usuario usuario) {
		
		if(repository.findByUsuario(usuario.getUsuario()).isPresent()) {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

			String senhaEncoder = encoder.encode(usuario.getSenha());
			usuario.setSenha(senhaEncoder);

			return Optional.of(repository.save(usuario));
		} else {
			return null;
		}
	}	

	public Optional<UserLogin> logar(Optional<UserLogin> user) {

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		Optional<Usuario> usuario = repository.findByUsuario(user.get().getUsuario());

		if(usuario.isPresent()) {
			if(encoder.matches(user.get().getSenha(), usuario.get().getSenha())) {

				String auth = user.get().getUsuario() + ":" + user.get().getSenha();
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodedAuth);

				user.get().setToken(authHeader);
				user.get().setNome(usuario.get().getNome());
				user.get().setId(usuario.get().getId());
				user.get().setFoto(usuario.get().getFoto());
				user.get().setTipo(usuario.get().getTipo());
				user.get().setSenha("");
				
				return user;

			}
		}
		return null;
	}
	
	public ResponseEntity<List<Usuario>> findAll() {
		List<Usuario> listaDeUsuarios = repository.findAll().stream().map(usuario -> {
			usuario.setSenha("");
			return usuario;
		}).collect(Collectors.toList());
		
		if(listaDeUsuarios.isEmpty()) {
			return ResponseEntity.status(204).build();
		} else {
			return ResponseEntity.status(200).body(listaDeUsuarios);
		}
	}
	
	public ResponseEntity<Usuario> getByIdUsuario(long id) {
		return repository.findById(id)
				.map(resp -> { resp.setSenha("");
					return ResponseEntity.status(200).body(resp);
				})
				.orElse(ResponseEntity.status(204).build());
	}
}
