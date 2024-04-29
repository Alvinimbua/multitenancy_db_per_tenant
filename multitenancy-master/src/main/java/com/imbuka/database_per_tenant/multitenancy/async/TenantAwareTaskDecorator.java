package com.imbuka.database_per_tenant.multitenancy.async;

import com.imbuka.database_per_tenant.multitenancy.util.TenantContext;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

/**
 * The ThreadLocal mechanism only works for synchronous flows.
 * Below is an implementation to pass along the Current Tenant to the asynchronous execution context.
 * asynchronous execution is encapsulated via the TaskExecutor abstraction
 * TaskDecorator interface provides a mechanism to attach additional information to an asynchronous execution.
 */
public class TenantAwareTaskDecorator implements TaskDecorator {
    @Override
    @NonNull
    public Runnable decorate(Runnable runnable) {
        String tenantId = TenantContext.getTenantId();
        return () -> {
            try {
                TenantContext.setTenantId(tenantId);
                runnable.run();
            } finally {
                TenantContext.setTenantId(null);
            }
        };
    }
}
