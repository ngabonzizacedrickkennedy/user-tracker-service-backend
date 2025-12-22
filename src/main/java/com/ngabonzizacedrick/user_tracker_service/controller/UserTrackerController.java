package com.ngabonzizacedrick.user_tracker_service.controller;

import com.ngabonzizacedrick.user_tracker_service.exception.UserNotFoundException;
import com.ngabonzizacedrick.user_tracker_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.cert.X509Certificate;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
public class UserTrackerController {

    private final UserService userService;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @PatchMapping("/track")
    public ResponseEntity<String> trackUser(HttpServletRequest request) {
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("jakarta.servlet.request.X509Certificate");
        
        if (certs == null || certs.length == 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Client certificate required");
        }

        String cn = extractCN(certs[0]);
        
        if (!isValidEmail(cn)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email format in certificate CN");
        }

        String clientIp = request.getRemoteAddr();
        int clientPort = request.getRemotePort();

        try {
            userService.updateUserActivity(cn, clientIp, clientPort);
            return ResponseEntity.ok("User activity tracked successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found");
        }
    }

    private String extractCN(X509Certificate cert) {
        String dn = cert.getSubjectX500Principal().getName();
        String[] parts = dn.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith("CN=")) {
                return trimmed.substring(3);
            }
        }
        return "";
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}