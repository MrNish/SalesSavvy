package com.example.demo.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.entities.CartItem;
import com.example.demo.entities.Product;
import com.example.demo.entities.ProductImage;
import com.example.demo.entities.User;
import com.example.demo.repositories.CartRepository;
import com.example.demo.repositories.ProductImageRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.UserRepository;

@Service
public class CartService {
	
	CartRepository cartRepository;
	UserRepository userRepository;
	ProductRepository productRepository;
	ProductImageRepository productImageRepository;
	
	public CartService(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository, ProductImageRepository productImageRepository) {
		this.cartRepository = cartRepository;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
		this.productImageRepository = productImageRepository;
	}
	
	public int getCartItemCount(int userId) {
		return cartRepository.countTotalItems(userId);
	}
	
	public void addToCart(int userId, int productId, int quantity) {
		User user = userRepository.findById(userId).orElseThrow(() ->new IllegalArgumentException("User Not Found With ID: " + userId));
		Product product = productRepository.findById(productId).orElseThrow(() ->new IllegalArgumentException("Product Not Found With ID: " + productId));	
		
		// fetch cartItem using userId and productId to check if already userId with productId entry exist
		Optional<CartItem> existingItem = cartRepository.findByUserAndProduct(userId, productId);
		
		if (existingItem.isPresent()) {
			
			// updating quantity of cartItem with the product
			CartItem cartItem = existingItem.get();
			cartItem.setQuantity(cartItem.getQuantity() + 1);
			cartRepository.save(cartItem); 
		} else {
			
			// adding new Item in the cartItem with userId and product
			CartItem newItem = new CartItem(user, product, quantity);
			cartRepository.save(newItem);
		}
	}
	
	public Map<String, Object> getCartItems(int userId) {
		
		List<CartItem> cartItems = cartRepository.findCartItemWithProductDetails(userId);
		
		Map<String, Object> response = new HashMap<>();
		User user = userRepository.findById(userId).orElseThrow(() ->new IllegalArgumentException("User Not Found"));
		
		response.put("username", user.getUsername());
		response.put("role", user.getRole().name());
		
		List<Map<String, Object>> products = new ArrayList<>();
		
		int overallTotalPrice = 0;
		
		for (CartItem cartItem: cartItems) {
			Map<String, Object> productDetails = new HashMap<>();
			Product product = cartItem.getProduct();
			List<ProductImage> productImages =
					productImageRepository.findByProduct_ProductId(product.getProductId());

			String imageUrls = null;
			
			if (productImages!= null && !productImages.isEmpty()) {
				imageUrls = productImages.get(0).getImageUrl();
				
			} else {
				imageUrls = "default-image-url";
			}
			
			productDetails.put("productId", product.getProductId());
			productDetails.put("image_url", imageUrls);
			productDetails.put("name", product.getProductName());
			productDetails.put("description", product.getDescription());
			productDetails.put("price_per_unit", product.getPrice());
			productDetails.put("quantity", cartItem.getQuantity());
			productDetails.put("totalPrice", cartItem.getQuantity() * product.getPrice().doubleValue());
			
			products.add(productDetails);
			
			overallTotalPrice += cartItem.getQuantity() * product.getPrice().doubleValue();
		}
		
		Map<String, Object> cart = new HashMap<>();
		cart.put("products", products);
		cart.put("overall_total_price", overallTotalPrice);
		response.put("cart", cart);
		return response;
	}
	
	
	public void updateCartItemQuantity(int userId, int productId, int quantity) {
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User Not Found"));
		Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product Not Found"));
		
		Optional<CartItem> existingItem = cartRepository.findByUserAndProduct(userId, productId);
		if (existingItem.isPresent()) {
			CartItem cartItem = existingItem.get();
			if (quantity == 0) {
				deleteCartItem(userId, productId);
			} else {
				cartItem.setQuantity(quantity);
				cartRepository.save(cartItem);
			}
		}
		
	}

	public void deleteCartItem(int userId, int productId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User Not Found"));
		Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product Not Found"));
		
		cartRepository.deleteCartItem(userId, productId);
	}
}
