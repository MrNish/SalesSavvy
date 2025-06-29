package com.example.demo.adminservice;

import java.math.BigDecimal;
import java.nio.channels.IllegalChannelGroupException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.entities.Category;
import com.example.demo.entities.Product;
import com.example.demo.entities.ProductImage;
import com.example.demo.repositories.CategoryRepository;
import com.example.demo.repositories.ProductImageRepository;
import com.example.demo.repositories.ProductRepository;

@Service
public class AdminProductService {
	
	ProductRepository productRepository;
	ProductImageRepository productImageRepository;
	CategoryRepository categoryRepository;
	

	public AdminProductService(ProductRepository productRepository, ProductImageRepository productImageRepository,
			CategoryRepository categoryRepository) {
		this.productRepository = productRepository;
		this.productImageRepository = productImageRepository;
		this.categoryRepository = categoryRepository;
	}
	
	public Product addProductWithImage(String name, String description, double price, Integer categoryId, Integer stock, String imageUrl) {
		Optional<Category> category = categoryRepository.findById(categoryId);	
		if (category.isEmpty()) {
			throw new IllegalArgumentException("Invalid Category Id");
		}
		
		Product product = new Product(stock, name, description, BigDecimal.valueOf(price), stock, category.get(), LocalDateTime.now(), LocalDateTime.now());
		Product savedProduct = productRepository.save(product);
		
		if (!imageUrl.isEmpty() && imageUrl != null) {
			ProductImage productImage = new ProductImage(savedProduct, imageUrl);
			productImageRepository.save(productImage);
		} else {
			throw new IllegalArgumentException("Proudct Image can't be NULL");
		}
		
		return savedProduct;
	}
	
	public void deleteProduct(int productId) {
		productImageRepository.deleteByProductId(productId);
		productRepository.deleteById(productId);
	}
}
