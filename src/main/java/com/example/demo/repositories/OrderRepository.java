package com.example.demo.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, String>{
	
	@Query("SELECT o FROM Order o WHERE MONTH(o.createdAt) = :month AND YEAR(o.createdAt) = :year")
	List<Order> findSuccessfulOrdersByMonthAndYear(int month, int year);
	
	@Query("SELECT o FROM Order o WHERE DATE(o.createdAt) = :date AND o.status = 'SUCCESS'")
	List<Order> findOrdersByDate(LocalDate date);
	
	@Query("SELECT o FROM Order o WHERE YEAR(o.createdAt) = :year AND o.status = 'SUCCESS'")
	List<Order> findOrdersByYear(int year);
	
	@Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'SUCCESS'")
	BigDecimal calculateOverallBuissness();
	
	@Query("SELECT o FROM Order o WHERE o.status = 'SUCCESS'")
	List<Order> findByStatus(String status);
}
