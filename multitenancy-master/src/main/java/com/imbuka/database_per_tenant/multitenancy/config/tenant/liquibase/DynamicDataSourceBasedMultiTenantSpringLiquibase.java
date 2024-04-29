package com.imbuka.database_per_tenant.multitenancy.config.tenant.liquibase;

import com.imbuka.database_per_tenant.multitenancy.entity.Tenant;
import com.imbuka.database_per_tenant.multitenancy.repository.TenantRepository;
import com.imbuka.database_per_tenant.util.EncryptionService;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.Data;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;

@Getter
@Setter
@Slf4j
public class DynamicDataSourceBasedMultiTenantSpringLiquibase implements InitializingBean, ResourceLoaderAware {

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private TenantRepository masterTenantRepository;


    @Qualifier("tenantLiquibaseProperties")
    private LiquibaseProperties liquibaseProperties;

    @Value("${multitenancy.tenant.datasource.url-prefix}")
    private String urlPrefix;

    @Value("${encryption.secret}")
    private String secret;

    @Value("${encryption.salt}")
    private String salt;

    private ResourceLoader resourceLoader;
    private String changelog;
    private String contexts;
    private boolean dropFirst = false;
    private boolean shouldRun = true;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("DynamicDataSources based multi tenancy enabled");
        this.runOnAllTenants(masterTenantRepository.findAll());

    }

    private void runOnAllTenants(Collection<Tenant> tenants) throws LiquibaseException {
        for (Tenant tenant : tenants) {
            String decryptedPassword = encryptionService.decrypt(tenant.getPassword(), secret, salt);
            log.info("Initializing liquibase for tenant " + tenant.getTenantId() + "and password " + decryptedPassword);
            try (Connection connection = DriverManager.getConnection(urlPrefix + tenant.getDb(), tenant.getDb(), decryptedPassword)) {
                DataSource tenantDataSource = new SingleConnectionDataSource(connection, false);
                SpringLiquibase liquibase = this.getSpringLiquibase(tenantDataSource);
                liquibase.afterPropertiesSet();

            } catch (SQLException | LiquibaseException e) {
                log.error("Failed to run liquibase for tenant " + tenant.getTenantId(), e);
            }
            log.info("Liquibase ran for tenant" + tenant.getTenantId());
        }
    }

    protected SpringLiquibase getSpringLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setResourceLoader(getResourceLoader());
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(liquibaseProperties.getChangeLog());
        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
        liquibase.setLiquibaseTablespace(liquibaseProperties.getLiquibaseTablespace());
        liquibase.setDatabaseChangeLogTable(liquibaseProperties.getDatabaseChangeLogTable());
        liquibase.setDatabaseChangeLogLockTable(liquibaseProperties.getDatabaseChangeLogLockTable());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        liquibase.setShouldRun(isShouldRun());
        liquibase.setTag(liquibaseProperties.getTag());
        liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
        liquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
        liquibase.setTestRollbackOnUpdate(liquibaseProperties.isTestRollbackOnUpdate());
        return liquibase;
    }
}
