package com.bank.atlasbank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AtlasViewController {

    @GetMapping("/")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/panel/cliente")
    public String clientPanel() {
        return "client/dashboard";
    }

    @GetMapping("/panel/cliente/inicio")
    public String clientHome() {
        return "client/inicio";
    }

    @GetMapping("/panel/cliente/cuentas")
    public String clientAccounts() {
        return "client/cuentas";
    }

    @GetMapping("/panel/cliente/transferencias")
    public String clientTransfers() {
        return "client/transferencias";
    }

    @GetMapping("/panel/cliente/movimientos")
    public String clientMovements() {
        return "client/movivmientos";
    }

    @GetMapping("/panel/cliente/tarjetas")
    public String clientCards() {
        return "client/tarjetas";
    }

    @GetMapping("/panel/admin")
    public String adminPanel() {
        return "admin/panel_admin";
    }

    @GetMapping("/panel/admin/usuarios")
    public String adminUsers() {
        return "admin/usuarios";
    }

    @GetMapping("/panel/admin/estado-clientes")
    public String adminCustomerStatus() {
        return "admin/estado_clientes";
    }
}
