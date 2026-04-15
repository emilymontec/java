package com.bank.atlasbank;

import com.bank.atlasbank.admin.Admin;
import com.bank.atlasbank.admin.AdminRepository;
import com.bank.atlasbank.customer.Customer;
import com.bank.atlasbank.customer.CustomerRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Siembra datos iniciales en la BD si no existen.
 * Garantiza que siempre haya al menos un admin y un cliente funcionales.
 */
@Component
public class DataInitializer {

    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;

    public DataInitializer(AdminRepository adminRepository,
                           CustomerRepository customerRepository) {
        this.adminRepository = adminRepository;
        this.customerRepository = customerRepository;
    }

    @PostConstruct
    public void init() {
        seedAdmin();
        seedCustomer();
        printStatus();
    }

    private void seedAdmin() {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin("admin", "admin123");
            admin.setRole("ADMIN");
            adminRepository.save(admin);
            System.out.println("[AtlasBank] Admin sembrado → usuario: admin | password: admin123");
        }
    }

    private void seedCustomer() {
        // Un cliente es "funcional" solo si tiene AMBOS: customerId y password
        boolean hayClienteFuncional = customerRepository.findAll().stream()
                .anyMatch(c -> c.getCustomerId() != null && !c.getCustomerId().isBlank()
                            && c.getPassword() != null && !c.getPassword().isBlank());

        if (!hayClienteFuncional) {
            // Verificar que el email demo no exista ya
            if (customerRepository.findByEmail("demo@atlasbank.com").isEmpty()) {
                Customer cliente = new Customer();
                cliente.setCustomerId("cliente.demo");
                cliente.setFullName("Cliente Demo AtlasBank");
                cliente.setEmail("demo@atlasbank.com");
                cliente.setPhone("+52 000 000 0000");
                cliente.setPassword("demo1234");
                customerRepository.save(cliente);
                System.out.println("[AtlasBank] Cliente demo sembrado → customerId: cliente.demo | password: demo1234");
            }
        }
    }

    /** Muestra en consola las credenciales disponibles para acceder */
    private void printStatus() {
        System.out.println("\n=== AtlasBank - Credenciales disponibles ===");
        System.out.println("[ADMIN] Emergency → usuario: chief | password: P@ss-CHIEF-83f7d9");
        adminRepository.findAll().forEach(a ->
            System.out.println("[ADMIN] BD → usuario: " + a.getUsername() + " | rol: " + a.getRole())
        );
        customerRepository.findAll().stream()
            .filter(c -> c.getCustomerId() != null && c.getPassword() != null)
            .forEach(c ->
                System.out.println("[CLIENTE] customerId: " + c.getCustomerId() + " | nombre: " + c.getFullName())
            );
        System.out.println("============================================\n");
    }
}
