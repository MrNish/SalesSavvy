package com.example.demo.adminservice;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.repositories.JWTTokenRepository;
import com.example.demo.repositories.UserRepository;

@Service
public class AdminUserService {
	UserRepository userRepository;
	JWTTokenRepository jwtTokenRepository;
	
	public AdminUserService(UserRepository userRepository, JWTTokenRepository jwtTokenRepository) {
		this.userRepository = userRepository;
		this.jwtTokenRepository = jwtTokenRepository;
	}
	
	public User getByUserId(Integer userId) {
		return userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("User with this userId is not found"));
	}
	
	public User modifyUser(Integer userId, String name, String email, String role) {
		Optional<User> userOptional = userRepository.findById(userId);
		if (userOptional.isEmpty()) {
			throw new IllegalArgumentException("User with ID not found");
		}
		
		User existingUser = userOptional.get();
		if (name != null && !name.isEmpty()) {
			existingUser.setUsername(name);
		}
		
		if (email != null && !email.isEmpty()) {
			existingUser.setEmail(email);
		}
		
		if (role != null && !role.isEmpty()) {
			existingUser.setRole(Role.valueOf(role));
		}
		
		jwtTokenRepository.deleteById(existingUser.getUserId());
		return userRepository.save(existingUser);
	}
}
