package com.example.demo.admincontroller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.adminservice.AdminBuissnessService;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/admin/buissness")
public class AdminBuissnessController {
	
	AdminBuissnessService adminBuissnessService;

	public AdminBuissnessController(AdminBuissnessService adminBuissnessService) {
		this.adminBuissnessService = adminBuissnessService;
	}
	
	@GetMapping("/monthly")
	public ResponseEntity<?> getMontlyBuissness(@RequestParam int month, @RequestParam int year) {
		try {
			Map<String, Object> buissnessReport = adminBuissnessService.calculateMontlyBuissness(month, year);
			return ResponseEntity.status(HttpStatus.OK).body(buissnessReport);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
		}
	}
	
	@GetMapping("/daily")
	public ResponseEntity<?> getDailyBuissness(@RequestParam String date) {
		try {
			LocalDate localDate = LocalDate.parse(date);
			Map<String, Object> buissnessReport = adminBuissnessService.calculateDailyBuissness(localDate);
			return ResponseEntity.status(HttpStatus.OK).body(buissnessReport);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
		}
	}
	
	@GetMapping("/yearly")
	public ResponseEntity<?> getYearlyBuissness(@RequestParam int year) {
		try {
			Map<String, Object> buissnessReport = adminBuissnessService.calculateYearlyBuissness(year);
			return ResponseEntity.status(HttpStatus.OK).body(buissnessReport);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
		}
	}
	
	@GetMapping("/overall")
	public ResponseEntity<?> getOverallBuissness() {
		try {
			Map<String, Object> buissnessReport = adminBuissnessService.calculateOverallBuissness();
			return ResponseEntity.status(HttpStatus.OK).body(buissnessReport);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
		}
	}
	
}
