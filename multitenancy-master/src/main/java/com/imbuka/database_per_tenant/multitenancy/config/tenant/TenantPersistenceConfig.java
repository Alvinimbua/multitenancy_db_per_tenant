package com.imbuka.database_per_tenant.multitenancy.config.tenant;

import com.imbuka.database_per_tenant.multitenancy.entity.Tenant;
import com.imbuka.database_per_tenant.multitenancy.repository.TenantRepository;
import com.imbuka.database_per_tenant.multitenancy.util.TenantContext;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.SpringBeanContainer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * configures the masterEntityManagerFactory as well as masterTransactionManager
 * with master DataSource as its DataSource
 *
 * it ensures that the application can dynamically switch between different tenants data by configuring
 * the entitymanagerfactory with the appropriate multitenant support
 */
@Slf4j
@Configuration
@EnableJpaRepositories(
        basePackages = {"${multitenancy.tenant.repository.packages}" },
        entityManagerFactoryRef = "tenantEntityManagerFactory",
        transactionManagerRef = "tenantTransactionManager"
)
@EnableConfigurationProperties(JpaProperties.class)
@RequiredArgsConstructor
public class TenantPersistenceConfig {

    private final ConfigurableListableBeanFactory beanFactory;
    private final JpaProperties jpaProperties;

    @Value("${multitenancy.tenant.entityManager.packages}")
    private String entityPackages;

    private final TenantRepository tenantRepository;

    @Primary
    @Bean("tenantEntityManagerFactory") //for creating Entity ManagerFactory that can create EntityManager instance for interacting with database
    public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory(
            //to provide db connections for diff tenants
            @Qualifier("dynamicDataSourceBasedMultiTenantConnectionProvider") MultiTenantConnectionProvider connectionProvider,
            //to resolve  current tenant identifier, to determine which tenant data should be accessed
            @Qualifier("currentTenantIdentifierResolver") CurrentTenantIdentifierResolver tenantResolver) {
        LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
        emfBean.setPersistenceUnitName("tenant-persistence-unit");
        emfBean.setPackagesToScan(entityPackages);

        //to indicate that hibernate is used as the JPA Provider
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        emfBean.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>(this.jpaProperties.getProperties());
        //allow hibernate to access Spring-managed beans
        properties.put(AvailableSettings.BEAN_CONTAINER, new SpringBeanContainer(this.beanFactory));

        String tenant = TenantContext.getTenantId();

        if (null == tenant) {
            properties.remove(AvailableSettings.DEFAULT_SCHEMA);
        } else {
            Tenant dbTenant = tenantRepository.findByTenantId(tenant).get();
            dbTenant.setTenantId(tenant);
        }

        properties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
        properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantResolver);
        emfBean.setJpaPropertyMap(properties);
        log.info("tenantEntityManagerFactory set up successfully!");
        return emfBean;
    }

    /**
     * for managing transaction in spring application that uses JPA for data access
     * @param emf
     * @return
     */
    @Primary
    @Bean("tenantTransactionManager")
    public JpaTransactionManager tenantTransactionManager(
            @Qualifier("tenantEntityManagerFactory") EntityManagerFactory emf) {
        JpaTransactionManager tenantTransactionManager = new JpaTransactionManager();
        tenantTransactionManager.setEntityManagerFactory(emf);
        return tenantTransactionManager;
    }
}
