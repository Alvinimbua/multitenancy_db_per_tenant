package com.imbuka.database_per_tenant.multitenancy.interceptor;

import com.imbuka.database_per_tenant.multitenancy.util.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

/**
 * An interceptor that will capture the Tenant id either from http Header X-TENANT_ID
 * OR from the subdomain part of the request server name
 */

@Component
public class TenantInterceptor implements WebRequestInterceptor {

    private final String defaultTenant;

    public TenantInterceptor(
            @Value("${multitenancy.tenant.default-tenant:#{null}}") String defaultTenant) {
        this.defaultTenant = defaultTenant;
    }

    @Override
    public void preHandle(WebRequest request) throws Exception {
        String tenantId = null;
        if (request.getHeader(TenantConstants.X_TENANT_ID) != null) {
            tenantId = request.getHeader(TenantConstants.X_TENANT_ID);
        } else if (this.defaultTenant != null) {
            tenantId = this.defaultTenant;
        } else {
            tenantId = ((ServletWebRequest) request).getRequest().getServerName().split("\\.")[0];
        }
        TenantContext.setTenantId(tenantId);
    }
    @Override
    public void postHandle (@NonNull WebRequest request, ModelMap model) throws Exception {
        TenantContext.clear();
    }

    @Override
    public void afterCompletion(@NonNull WebRequest request, Exception ex) throws Exception {

    }
}
