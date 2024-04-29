package com.imbuka.database_per_tenant.multitenancy.service;

import com.imbuka.database_per_tenant.multitenancy.repository.TenantRepository;
import com.imbuka.database_per_tenant.multitenancy.entity.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    @Override
    public Tenant findByTenantId(String tenantId) {
        return tenantRepository.findByTenantId(tenantId)
                .orElseThrow(()-> new RuntimeException("No such element: " + tenantId));
    }
}
