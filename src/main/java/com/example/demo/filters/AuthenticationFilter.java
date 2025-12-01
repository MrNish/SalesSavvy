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


@Component
public class AuthenticationFilter implements Filter {

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
			"/api/users/register",
			"/api/auth/login"
	};

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			executeFilterLogin(request, response, chain);
		} catch (Exception e) {
			logger.error("Unexpected Error in authentication filter", e);
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
		logger.info("Request URI: {}", requestURI);

		// Handle CORS preflight
		if (httpRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
			setCORSHeaders(httpResponse);
			httpResponse.setStatus(HttpServletResponse.SC_OK);
			return;
		}

		// Handle unauthenticated paths
		if (Arrays.asList(UNAUTHENTICATED_PATHS).contains(requestURI)) {
			chain.doFilter(request, response);
			return;
		}

		String token = extractTokenFromRequest(httpRequest);

		if (token == null || !authService.validateToken(token)) {
			sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid Token");
			return;
		}


		logger.info("Token found: {}", token.substring(0, Math.min(10, token.length())) + "...");

		if (!authService.validateToken(token)) {
			logger.warn("Invalid token");
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

		if (requestURI.startsWith("/api/") && (role != Role.CUSTOMER && role != Role.ADMIN)) {
			sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Forbidden: Customer access required");
			return;
		}

		httpRequest.setAttribute("authenticatedUser", authenticatdUser);
		chain.doFilter(request, response);
	}

	private String extractTokenFromRequest(HttpServletRequest request) {
		// First try: Get from Authorization header
		String token = getAuthTokenFromHeader(request);

		// Second try: Get from cookies
		if (token == null) {
			token = getAuthTokenFromCookies(request);
		}

		return token;
	}

	private String getAuthTokenFromHeader(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			logger.info("Found token in Authorization header");
			return token;
		}
		return null;
	}

	private String getAuthTokenFromCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("authToken".equals(cookie.getName())) {
					String token = cookie.getValue();
					// Clean the token - remove any extra attributes that might have been included
					if (token != null && token.contains(" ")) {
						token = token.split(" ")[0]; // Take only the first part before any space
					}
					logger.info("Found token in cookies");
					return token;
				}
			}
		}
		return null;
	}

	void setCORSHeaders(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept, X-Requested-With, Origin");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Expose-Headers", "*");
	}
}