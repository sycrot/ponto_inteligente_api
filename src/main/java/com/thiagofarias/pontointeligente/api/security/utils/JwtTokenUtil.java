package com.thiagofarias.pontointeligente.api.security.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.thiagofarias.pontointeligente.api.entities.Funcionario;
import com.thiagofarias.pontointeligente.api.repositories.FuncionarioRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenUtil {
	
	static final String CLAIM_KEY_USERNAME = "sub";
	static final String CLAIM_KEY_ROLE = "role";
	static final String CLAIM_KEY_AUDIENCE = "audience";
	static final String CLAIM_KEY_CREATED = "created";
	
	@Value("${jwt.secret")
	private String secret;
	
	@Value("${jwt.expiration}")
	private Long expiration;
	
	@Autowired
	private FuncionarioRepository funcionarioRepository;
	
	// gets the username (email) contained in the JWT token
	public String getUsernameFromToken(String token) {
		String username;
		
		try {
			Claims claims = getClaimsFromToken(token);
			username = claims.getSubject();
		} catch(Exception e) {
			username = null;
		}
		return username;
	}
	
	// Returns the expiration date of a JWT token
	public Date getExpirationDateFromToken(String token) {
		Date expiration;
		
		try {
			Claims claims = getClaimsFromToken(token);
			expiration = claims.getExpiration();
		} catch(Exception e) {
			expiration = null;
		}
		
		return expiration;
	}
	
	// Creates a new token(refresh)
	public String refreshToken(String token) {
		String refreshedToken;
		
		try {
			Claims claims = getClaimsFromToken(token);
			claims.put(CLAIM_KEY_CREATED, new Date());
			refreshedToken = gerarToken(claims);
		} catch(Exception e) {
			refreshedToken = null;
		}
		return refreshedToken;
	}
	
	// Checks and returns whether a jwt token is valid
	public boolean tokenValido(String token) {
		return !tokenExpirado(token);
	}
	
	// Returns a new JWT token based on user data
	public String obterToken(UserDetails userDetails) {
		
		Funcionario funcionario = funcionarioRepository.findByEmail(userDetails.getUsername());
		Map<String, Object> claims = new HashMap<>();
		claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
		userDetails.getAuthorities().forEach(authority -> claims.put(CLAIM_KEY_ROLE, authority.getAuthority()));
		claims.put(CLAIM_KEY_CREATED, new Date());
		
		if (funcionario != null) {
			claims.put("id", funcionario.getId());
			claims.put("empresaId", funcionario.getEmpresa().getId());
		}
		
		return gerarToken(claims);
		
		
	}
	
	// Returns the expiration date based on the current date
	private Date gerarDataExpiracao() {
		return new Date(System.currentTimeMillis() + expiration * 1000);
	}
	
	// Checks if a JWT token is expired
	private boolean tokenExpirado(String token) {
		
		Date dataExpiracao = this.getExpirationDateFromToken(token);
		if (dataExpiracao == null) {
			return false;
		}
		
		return dataExpiracao.before(new Date());
		
	}
	
	// Generates a new JWT token containing the data(claims) provided
	private String gerarToken(Map<String, Object> claims) {
		return Jwts.builder().setClaims(claims).setExpiration(gerarDataExpiracao()).signWith(SignatureAlgorithm.HS512, secret).compact();
	}
	
	// Parse the JWT token to extract the information contained in the his body
	private Claims getClaimsFromToken(String token) {
		Claims claims;
		
		try {
			claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
		} catch(Exception e) {
			claims = null;
		}
		
		return claims;
	}

}
