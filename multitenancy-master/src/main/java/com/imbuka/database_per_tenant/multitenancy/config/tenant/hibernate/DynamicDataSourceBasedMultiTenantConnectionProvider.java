package com.imbuka.database_per_tenant.multitenancy.config.tenant.hibernate;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.imbuka.database_per_tenant.multitenancy.entity.Tenant;
import com.imbuka.database_per_tenant.multitenancy.repository.TenantRepository;
import com.imbuka.database_per_tenant.util.EncryptionService;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

//hibernate needs to obtain connections in a tenant specific manner
@RequiredArgsConstructor
@Slf4j
@Component("dynamicDataSourceBasedMultiTenantConnectionProvider")
public class DynamicDataSourceBasedMultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {
    private static final String TENANT_POOL_NAME_SUFFIX = "DataSource";

    private final EncryptionService encryptionService;

    @Qualifier("masterDataSource")
    private final DataSource masterDataSource;

    @Qualifier("masterDataSourceProperties")
    private final DataSourceProperties dataSourceProperties;

    private final TenantRepository masterTenantRepository;

    @Value("${multitenancy.tenant.datasource.url-prefix}")
    private String urlPrefix;

    @Value("${multitenancy.datasource-cache.maximumSize:100}")
    private Long maximumSize;

    @Value("${multitenancy.datasource-cache.expireAfterAccess:10}")
    private Integer expireAfterAccess;

    @Value("${encryption.secret}")
    private String secret;

    @Value("${encryption.salt}")
    private String salt;

    private LoadingCache<String, DataSource> tenantDataSources;

    /**
     * the cache is intended to store data source objects for different tenants
     * allowing for efficient retrieval and management of tenant specific datasource
     */
    //we load the tenant and their dataSource built based on the corresponding connection details in the cache
    @PostConstruct // -> used to execute a method after dependency injection is done to perform any initialization
    //executed after the bean is fully initialized
    private void createCache() {
        tenantDataSources = CacheBuilder.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterAccess(expireAfterAccess, TimeUnit.MINUTES)
                //set to perform actions when entry is removed from the cache
                .removalListener((RemovalListener<String, DataSource>) removal -> {
                    HikariDataSource ds = (HikariDataSource) removal.getValue();
                    ds.close();
                    log.info("Closed datasource: {}", ds.getPoolName());
                })
                //for loading data into the cache when a requested key is not present
                //if tenant is found , it creates and configures a new data source for the data source
                .build(new CacheLoader<String, DataSource>() {
                    public DataSource load(String key) {
                        Tenant tenant = masterTenantRepository.findByTenantId(key)
                                .orElseThrow(() -> new RuntimeException("No such tenant: " + key));
                        return createAndConfigureDataSource(tenant);
                    }
                });
    }
    //return the master datasource by default
    @Override
    protected DataSource selectAnyDataSource() {
        return masterDataSource;
    }

    //return the corresponding data source based on the data passed in
    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        try {
            return tenantDataSources.get(tenantIdentifier);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private DataSource createAndConfigureDataSource(Tenant tenant) {
        String decryptPassword = encryptionService.decrypt(tenant.getPassword(),secret,salt);

        HikariDataSource ds = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();

        ds.setUsername(tenant.getDb());
        ds.setPassword(decryptPassword);
        ds.setJdbcUrl(urlPrefix + tenant.getDb());
        ds.setPoolName(tenant.getTenantId() + TENANT_POOL_NAME_SUFFIX);

        log.info("Configured datasource: {}", ds.getPoolName());
        log.info("ds url " + ds.getJdbcUrl() + ", user " + ds.getUsername());
        return  ds;
    }
}
