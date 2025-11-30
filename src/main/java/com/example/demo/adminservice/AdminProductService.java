package com.example.demo.adminservice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.entities.Category;
import com.example.demo.entities.Product;
import com.example.demo.entities.ProductImage;
import com.example.demo.repositories.CategoryRepository;
import com.example.demo.repositories.ProductImageRepository;
import com.example.demo.repositories.ProductRepository;

@Service
public class AdminProductService {

	private static final Logger logger = LoggerFactory.getLogger(AdminProductService.class);

	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final CategoryRepository categoryRepository;


	public AdminProductService(ProductRepository productRepository,
							   ProductImageRepository productImageRepository,
							   CategoryRepository categoryRepository) {
		this.productRepository = productRepository;
		this.productImageRepository = productImageRepository;
		this.categoryRepository = categoryRepository;
	}

	public Product addProductWithImage(String name, String description, double price,
									   Integer categoryId, Integer stock, String imageUrl) {
		// Validate inputs
		validateProductInputs(name, price, categoryId, stock, imageUrl);

		// Find category
		Category category = findCategoryById(categoryId);

		// Create and save product
		Product product = createProduct(name, description, price, stock, category);
		Product savedProduct = productRepository.save(product);
		logger.info("Product created successfully with ID: {}", savedProduct.getProductId());

		// Save product image
		saveProductImage(savedProduct, imageUrl);

		return savedProduct;
	}

	public void deleteProduct(int productId) {
		productImageRepository.deleteByProductId(productId);
		productRepository.deleteById(productId);
		logger.info("Product deleted successfully with ID: {}", productId);
	}

	private void validateProductInputs(String name, double price, Integer categoryId, Integer stock, String imageUrl) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Product name is required");
		}
		if (price <= 0) {
			throw new IllegalArgumentException("Price must be greater than 0");
		}
		if (categoryId == null) {
			throw new IllegalArgumentException("Category ID is required");
		}
		if (stock == null || stock < 0) {
			throw new IllegalArgumentException("Stock must be a non-negative number");
		}
		if (imageUrl == null || imageUrl.trim().isEmpty()) {
			throw new IllegalArgumentException("Product image URL is required");
		}
	}

	private Category findCategoryById(Integer categoryId) {
		Optional<Category> category = categoryRepository.findById(categoryId);
		if (category.isEmpty()) {
			throw new IllegalArgumentException("Invalid Category ID: " + categoryId);
		}
		return category.get();
	}

	private Product createProduct(String name, String description, double price, Integer stock, Category category) {
		return new Product(
				name,
				description,
				BigDecimal.valueOf(price),
				stock,
				category,
				LocalDateTime.now(),
				LocalDateTime.now()
		);
	}

	private void saveProductImage(Product product, String imageUrl) {
		ProductImage productImage = new ProductImage(product, imageUrl);
		productImageRepository.save(productImage);
		logger.info("Product image saved for product ID: {}", product.getProductId());
	}
}