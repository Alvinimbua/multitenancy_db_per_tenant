package com.imbuka.database_per_tenant.repository;

import com.imbuka.database_per_tenant.model.Tenant;
import org.springframework.data.repository.CrudRepository;

public interface TenantRepository extends CrudRepository<Tenant, String> {
}
