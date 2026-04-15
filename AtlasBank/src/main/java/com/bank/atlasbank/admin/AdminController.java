package com.bank.atlasbank.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<Admin> admin = adminService.authenticate(username, password);
        if (admin.isPresent()) {
            String role = admin.get().getRole() == null ? "" : admin.get().getRole().trim().toUpperCase();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "role", role
            ));
        }
        return ResponseEntity.status(401).body(Map.of(
            "success", false,
            "message", "Credenciales inválidas"
        ));
    }
}
