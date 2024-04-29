package com.imbuka.database_per_tenant.multitenancy.service;

import com.imbuka.database_per_tenant.multitenancy.entity.Tenant;
import org.springframework.data.repository.query.Param;

public interface TenantService {

    Tenant findByTenantId(@Param("tenantId") String tenantId);
}
