package com.example.demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Product;
import com.example.demo.entities.ProductImage;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>{
	List<Product> findByCategory_CategoryId(Integer categoryId);
	
	@Query("SELECT p.category.categoryName FROM Product p WHERE p.productId = :productId")
	String findByCategoryNameByProductId(Integer productId);

	List<ProductImage> findByProductId(Integer productId);
}
