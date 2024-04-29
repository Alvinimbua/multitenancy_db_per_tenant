package com.imbuka.database_per_tenant.controller;

import com.imbuka.database_per_tenant.service.TenantAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class TenantAdminController {

    @Autowired
    private TenantAdminService tenantAdminService;

    @PostMapping("/tenants")
    public ResponseEntity<Void> createTenant(@RequestParam String tenantId,
                                             @RequestParam String db,
                                             @RequestParam String password) {
        tenantAdminService.createTenant(tenantId, db, password);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}