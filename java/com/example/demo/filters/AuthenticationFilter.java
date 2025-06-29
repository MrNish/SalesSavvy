package com.example.demo.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.demo.entities.Role;
import com.example.demo.entities.User;

import com.example.demo.repositories.UserRepository;
import com.example.demo.services.AuthService;


import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = {"/api/*", "/admin/*"})
@Component
public class AuthenticationFilter implements Filter{
	
	AuthService authService;
	UserRepository userRepository;
	
	public AuthenticationFilter(AuthService authService, UserRepository userRepository) {
		System.out.println("Filter Started");
		this.authService = authService;
		this.userRepository = userRepository;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
	
	private static final String ALLOWED_ORIGIN = "http://localhost:5173";
	
	public static final String[] UNAUTHENTICATED_PATHS = {
		"api/users/register",
		"api/auth/login"
	};
	
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			executeFilterLogin(request, response, chain);
		} catch (Exception e) {
			logger.error("Unexpected Error in authentiacation filter", e);
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			sendErrorResponse(httpResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
			
		}
	}
	
	public void sendErrorResponse(HttpServletResponse response, int statuscode, String message) throws IOException {
		response.setStatus(statuscode);
		response.getWriter().write(message);
	}

	public void executeFilterLogin(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			
			String requestURI = httpRequest.getRequestURI();
			System.err.println(requestURI);
			logger.info("Reqeuset URI: {}", requestURI);
			
			if (Arrays.asList(UNAUTHENTICATED_PATHS).contains(requestURI)) {
				chain.doFilter(request, response);
				return;
			}
			 if (httpRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
		        setCORSHeaders(httpResponse);
		        return;
		     }
			 
			 // for login time token 
			 // changes made here
			 String uri = requestURI;
			 if (uri.equals("/api/auth/login")) {
			     chain.doFilter(request, response);
			     return;
			 }
			  
			String token = getAuthTokenFromCookies(httpRequest);
			System.out.println(token);
			if (token == null || !authService.validateToken(token)) {
				sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid Token");
				return;
			}
			
			String username = authService.extractUserName(token);
			Optional<User> userOptional = userRepository.findByUsername(username);
			if (userOptional.isEmpty()) {
				sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: User Not Found");
				return;
			}
			
			User authenticatdUser = userOptional.get();
			Role role = authenticatdUser.getRole();
			logger.info("Authenticated User: {}, Role: {}", authenticatdUser.getUsername(), role);
			
			 if (requestURI.startsWith("/admin/") && role != Role.ADMIN) {
			        sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Forbidden: Admin access required");
			        return;
			    }
			
			 if (requestURI.startsWith("/api/") && (role != Role.CUSTOMER && role!=Role.ADMIN)) {
			        sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Forbidden: Customer access required");
			        return;
			    }
			
			
			httpRequest.setAttribute("authenticatedUser", authenticatdUser);
			chain.doFilter(request, response);
	}
	
	private String getAuthTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> "authToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
	
	void setCORSHeaders(HttpServletResponse response) {
		response.setHeader("ACCESS-CONTROL-ALLOW-ORIGIN", ALLOWED_ORIGIN);
		response.setHeader("ACCESS-CONTROL-ALLOW-METHODS", "GET, POST, PUT, DELETE, OPTIONS");
		response.setHeader("ACCESS-CONTROL-ALLOW-HEADER", "Content-Type, Authorization");
		response.setHeader("ACCESS-CONTROL-ALLOW-CREDENTIALS", "true");
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
