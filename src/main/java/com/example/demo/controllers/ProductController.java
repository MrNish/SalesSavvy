package com.example.demo.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entities.Product;
import com.example.demo.entities.User;
import com.example.demo.services.ProductService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/products")
public class ProductController {
	
	ProductService productService;
	
	public ProductController(ProductService productService) {
		super();
		this.productService = productService;
	}



	@GetMapping()
	public ResponseEntity<Map<String, Object>> getProducts(@RequestParam String category, HttpServletRequest request) {
		try {
			User authenticatedUser = (User) request.getAttribute("authenticatedUser");
			if (authenticatedUser == null) {
				return ResponseEntity.status(401).body(Map.of("Error", "Unauthorized Access"));
			}
			
			List<Product> products = productService.getProductByCategory(category);
			
			Map<String, Object> response = new HashMap<>();
			
			Map<String, String> userInfo = new HashMap<>();
			userInfo.put("name", authenticatedUser.getUsername());
			userInfo.put("role", authenticatedUser.getRole().name());
			
			response.put("user", userInfo);
			
			List<Map<String, Object>> productList = new ArrayList<>();
			for (Product product: products) {
				Map<String, Object> productDetails = new HashMap<>();
				productDetails.put("productId", product.getProductId());
				productDetails.put("name", product.getProductName());
				productDetails.put("description", product.getDescription());
				productDetails.put("price", product.getPrice());
				productDetails.put("stock", product.getStock());
				
				List<String> images = productService.getProductImages(product.getProductId());
				productDetails.put("images", images);
				productList.add(productDetails);
				
			}
			
			response.put("products", productList);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));
		}
	}
	
	
}
