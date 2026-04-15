package com.bank.atlasbank.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @PostConstruct
    public void initDefaultAdmin() {
        Optional<Admin> existing = adminRepository.findByUsername("./chief");
        if (existing.isEmpty()) {
            Admin admin = new Admin("./chief", "P@ss-CHIEF-83f7d");
            adminRepository.save(admin);
        }
    }

    public Optional<Admin> authenticate(String username, String password) {
        Optional<Admin> admin = adminRepository.findByUsername(username);
        if (admin.isPresent() && admin.get().getPassword().equals(password)) {
            return admin;
        }
        return Optional.empty();
    }

    public boolean existsByUsername(String username) {
        return adminRepository.findByUsername(username).isPresent();
    }
}