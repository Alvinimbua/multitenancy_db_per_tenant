package com.imbuka.database_per_tenant.multitenancy.config.tenant.hibernate;

import com.imbuka.database_per_tenant.multitenancy.util.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Map;

/**
 * CurrentTenantIdentifierResolver --> encapsulates a strategy for resolving which tenant to use for a specific request,
 * MultiTenantConnectionProvider --> encapsulates a strategy for selecting an appropriate database connection for that tenant.
 */
// tells hibernate which is the currently configured tenant
@Slf4j
@Component("currentTenantIdentifierResolver")
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver, HibernatePropertiesCustomizer {

    private final String defaultTenant;

    @Autowired
    public CurrentTenantIdentifierResolverImpl(
            @Value("${multitenancy.master.schema:#{null}}") String defaultTenant) {
        this.defaultTenant = defaultTenant;
    }

    //it tells hibernate which is the currently configured tenant
    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        log.info(">>> tenantId in resolveCurrentTenantIdentifier ", tenantId);
        if (!Strings.isEmpty(tenantId)) {
            return tenantId;
        } else if (!Strings.isEmpty(this.defaultTenant)) {
            return this.defaultTenant;
        } else {
            throw new IllegalStateException("No Tenants Selected");
        }
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
