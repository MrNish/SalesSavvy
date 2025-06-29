package com.example.demo.adminservice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.entities.OrderStatus;
import com.example.demo.repositories.OrderItemRepository;
import com.example.demo.repositories.OrderRepository;
import com.example.demo.repositories.ProductRepository;

@Service
public class AdminBuissnessService {
	
	OrderRepository orderRepository;
	OrderItemRepository orderItemRepository;
	ProductRepository productRepository;
		
	public AdminBuissnessService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository) {
		this.orderRepository = orderRepository;
		this.orderItemRepository=  orderItemRepository;
		this.productRepository = productRepository;
	}

	public Map<String, Object> calculateMontlyBuissness(int month, int year) {
		if (year < 2000 || year > 2025) {
			throw new IllegalArgumentException("Invalid Year: " + year);
		} 
		
		List<Order> successfullOrders = orderRepository.findSuccessfulOrdersByMonthAndYear(month, year);
		
		Double totalBuissness = 0.0;
		Map<String, Integer> categorySales = new HashMap<>();
		
		for (Order order: successfullOrders) {
			totalBuissness += order.getTotalAmount().doubleValue();
			List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());
			
			for (OrderItem item: orderItems) {
				String categoryName = productRepository.findByCategoryNameByProductId(item.getProductId());
				categorySales.put("categoryName", categorySales.getOrDefault(categoryName, 0) + item.getQuantity());
				
			}
		}
		
		Map<String, Object> buissnessReport = new HashMap<>();
		buissnessReport.put("totalBuissness", totalBuissness);
		buissnessReport.put("categorySales", categorySales);
		
		return buissnessReport;
	}
	
	public Map<String, Object> calculateDailyBuissness(LocalDate date) {
		if (date== null) {
			throw new IllegalArgumentException("Invalid Date as date can't be null");
		} 
		
		List<Order> successfullOrders = orderRepository.findOrdersByDate(date);
		
		Double totalBuissness = 0.0;
		Map<String, Integer> categorySales = new HashMap<>();
		
		for (Order order: successfullOrders) {
			totalBuissness += order.getTotalAmount().doubleValue();
			List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());
			
			for (OrderItem item: orderItems) {
				String categoryName = productRepository.findByCategoryNameByProductId(item.getProductId());
				categorySales.put("categoryName", categorySales.getOrDefault(categoryName, 0) + item.getQuantity());
				
			}
		}
		
		Map<String, Object> buissnessReport = new HashMap<>();
		buissnessReport.put("totalBuissness", totalBuissness);
		buissnessReport.put("categorySales", categorySales);
		
		return buissnessReport;
	}
	
	public Map<String, Object> calculateYearlyBuissness(int year) {
		if (year < 2000 || year > 2025) {
			throw new IllegalArgumentException("Invalid Year: " + year);
		} 
		
		List<Order> successfullOrders = orderRepository.findOrdersByYear(year);
		
		Double totalBuissness = 0.0;
		Map<String, Integer> categorySales = new HashMap<>();
		
		for (Order order: successfullOrders) {
			totalBuissness += order.getTotalAmount().doubleValue();
			List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());
			
			for (OrderItem item: orderItems) {
				String categoryName = productRepository.findByCategoryNameByProductId(item.getProductId());
				categorySales.put("categoryName", categorySales.getOrDefault(categoryName, 0) + item.getQuantity());
				
			}
		}
		
		Map<String, Object> buissnessReport = new HashMap<>();
		buissnessReport.put("totalBuissness", totalBuissness);
		buissnessReport.put("categorySales", categorySales);
		
		return buissnessReport;
	}
	
	public Map<String, Object> calculateOverallBuissness() {
		BigDecimal totalOverallBuissness = orderRepository.calculateOverallBuissness();
		List<Order> successfullOrders = orderRepository.findByStatus(OrderStatus.SUCCESS.name());
		
		Map<String, Integer> categorySales = new HashMap<>();
		
		for (Order order: successfullOrders) {
			List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());
			
			for (OrderItem item: orderItems) {
				String categoryName = productRepository.findByCategoryNameByProductId(item.getProductId());
				categorySales.put("categoryName", categorySales.getOrDefault(categoryName, 0) + item.getQuantity());
				
			}
		}
		
		Map<String, Object> buissnessReport = new HashMap<>();
		buissnessReport.put("totalBuissness", totalOverallBuissness.doubleValue());
		buissnessReport.put("categorySales", categorySales);
		
		return buissnessReport;
	}
}
