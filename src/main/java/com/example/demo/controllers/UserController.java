package com.example.demo.controllers;

import java.util.Map;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entities.User;
import com.example.demo.services.UserService;

@RestController
@CrossOrigin(origins="http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/users")
public class UserController {
	
	private final UserService userService;
	
	public UserController(UserService userService) {
		this.userService = userService;
	}
	
	@PostMapping("/register")
	
	public ResponseEntity<?> registerUser(@RequestBody User user) {
		try {
			User registerUser = userService.registerUser(user);
			return ResponseEntity.ok(Map.of("message", "User registered successfully", "user", registerUser));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
	
}
