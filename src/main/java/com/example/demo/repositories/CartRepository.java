package com.example.demo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entities.CartItem;

import jakarta.transaction.Transactional;

public interface CartRepository extends JpaRepository<CartItem, Integer>{
	
	@Query("SELECT c FROM CartItem c WHERE c.user.userId = :userId AND c.product.productId = :productId")
	Optional<CartItem> findByUserAndProduct(int userId, int productId);
	
	@Query("SELECT COALESCE(SUM(c.quantity), 0) FROM CartItem c WHERE c.user.userId = :userId")
	int countTotalItems(int userId);
	
	@Query("SELECT c FROM CartItem c JOIN FETCH c.product p LEFT JOIN FETCH ProductImage pi ON p.productId = pi.product.productId WHERE c.user.userId = :userId")
	List<CartItem> findCartItemWithProductDetails(int userId);
	
	@Query("UPDATE CartItem c SET c.quantity = :quantity WHERE c.id = :cartItemId")
	public void updateCartItemQuantity(int cartItemId, int quantity);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM CartItem c WHERE c.user.userId = :userId AND c.product.productId = :productId")
	public void deleteCartItem(int userId, int productId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM CartItem c WHERE c.user.userId = :userId")
	public void deleteAllCartItemsByUser(int userId);

//	List<CartItem> findCartItemsWithUserAndProduct(int userId);
}
