package com.example.demo.services;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entities.JWTToken;
import com.example.demo.entities.User;
import com.example.demo.repositories.JWTTokenRepository;
import com.example.demo.repositories.UserRepository;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class AuthService {

	private final Key SIGNING_KEY;
	UserRepository userRepository;
	JWTTokenRepository jwtTokenRepository;
	BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public AuthService(JWTTokenRepository jwtTokenRepository, UserRepository userRepository, @Value("${jwt.secret}") String jwtSecret) {
		this.jwtTokenRepository = jwtTokenRepository;
		this.userRepository = userRepository;
		this.SIGNING_KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}

	public User authenticate(String username, String password) {
		Optional<User> existingUser = userRepository.findByUsername(username);
		if(!existingUser.isEmpty()) {
			User user = existingUser.get();
			if (!passwordEncoder.matches(password, user.getPassword())) {
				throw new RuntimeException("Invalid Password");
			}
			return user;
		} else {
			throw new RuntimeException("Invalid Username");
		}
	}

	public String generateToken(User user) {
		String token;
		LocalDateTime currentTime = LocalDateTime.now();
		JWTToken existingToken = jwtTokenRepository.findByUser_id(user.getUserId());
		if (existingToken != null && currentTime.isBefore(existingToken.getExpiresAt())) {
			token = existingToken.getToken();
		} else {
			token = generateNewToken(user);
			if (existingToken != null) {
				jwtTokenRepository.delete(existingToken);
			}
			saveToken(user, token);
			System.out.println(token);
		}
		return token;
	}

	public String generateNewToken(User user) {
		JwtBuilder builder = Jwts.builder();
		builder.setSubject(user.getUsername());
		builder.claim("role", user.getRole().name());
		builder.setIssuedAt(new Date());
		builder.setExpiration(new Date(System.currentTimeMillis() + 3600000));
		builder.signWith(SIGNING_KEY);
		String token = builder.compact();
		System.out.println(token);
		return token;
	}

	public void saveToken(User user, String token) {
		LocalDateTime expireAt = LocalDateTime.now().plusHours(1);
		JWTToken jwtToken = new JWTToken(user, token, LocalDateTime.now(), expireAt);
		jwtTokenRepository.save(jwtToken);
	}

	public boolean validateToken(String token) {
		try {
			// Parse and validate token
			Jwts.parserBuilder()
					.setSigningKey(SIGNING_KEY)
					.build()
					.parseClaimsJws(token);

			// Check token is present in DB and not expired
			Optional<JWTToken> jwtToken = jwtTokenRepository.findByToken(token);
			if (jwtToken.isPresent()) {
				return jwtToken.get().getExpiresAt().isAfter(LocalDateTime.now());
			}
			return false;
		} catch(Exception e) {
			System.out.println("Token Validation Failed: " + e.getMessage());
			return false;
		}
	}

	public String extractUserName(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(SIGNING_KEY)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}
}
