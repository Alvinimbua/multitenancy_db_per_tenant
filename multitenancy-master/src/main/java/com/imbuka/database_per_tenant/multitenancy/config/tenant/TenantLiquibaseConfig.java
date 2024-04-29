package com.imbuka.database_per_tenant.multitenancy.config.tenant;

import com.imbuka.database_per_tenant.multitenancy.config.tenant.liquibase.DynamicDataSourceBasedMultiTenantSpringLiquibase;
import liquibase.integration.spring.MultiTenantSpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

@Lazy(false)
@Configuration
@ConditionalOnProperty(name = "multitenancy.tenant.liquibase.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(LiquibaseProperties.class)
public class TenantLiquibaseConfig {

    @Value("${multitenancy.tenant.liquibase.changeLog}")
    private String tenantLiquibaseChangelog;

    @Bean
    @DependsOn("liquibase")
    public MultiTenantSpringLiquibase multiTenantSpringLiquibase(
            @Qualifier("masterLiquibaseProperties")
            LiquibaseProperties liquibaseProperties) {
        MultiTenantSpringLiquibase liquibase = new MultiTenantSpringLiquibase();
        liquibase.setChangeLog(tenantLiquibaseChangelog);
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        liquibase.setShouldRun(liquibaseProperties.isEnabled());
        return liquibase;
    }

    @Bean
    @ConfigurationProperties("multitenancy.tenant.liquibase")
    public LiquibaseProperties tenantLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    public DynamicDataSourceBasedMultiTenantSpringLiquibase tenantSpringLiquibase() {
        return new DynamicDataSourceBasedMultiTenantSpringLiquibase();
    }
}
