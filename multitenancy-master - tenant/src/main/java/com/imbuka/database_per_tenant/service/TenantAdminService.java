package com.imbuka.database_per_tenant.service;

import com.imbuka.database_per_tenant.model.Tenant;
import com.imbuka.database_per_tenant.repository.TenantRepository;
import com.imbuka.database_per_tenant.util.EncryptionService;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
@Service
@EnableConfigurationProperties(LiquibaseProperties.class)
public class TenantAdminService {

    private static final String VALID_DB_SCHEMA_NAME_REGEXP = "[A-Za-z0-9_]*";
    private final EncryptionService encryptionService;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final LiquibaseProperties tenantLiquibaseProperties;
    private final ResourceLoader resourceLoader;
    private final TenantRepository tenantRepository;
    private final String urlPrefix;
    private final String secret;
    private final String salt;

    @Autowired
    public TenantAdminService(EncryptionService encryptionService,
                              DataSource dataSource,
                              JdbcTemplate jdbcTemplate,
                              @Qualifier("tenantLiquibaseProperties")
                              LiquibaseProperties tenantLiquibaseProperties,
                              ResourceLoader resourceLoader,
                              TenantRepository tenantRepository,
                              @Value("${multitenancy.tenant.datasource.url-prefix}") String urlPrefix,
                              @Value("${encryption.secret}") String secret,
                              @Value("${encryption.salt}") String salt
    ) {
        this.encryptionService = encryptionService;
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.tenantLiquibaseProperties = tenantLiquibaseProperties;
        this.resourceLoader = resourceLoader;
        this.tenantRepository = tenantRepository;
        this.urlPrefix = urlPrefix;
        this.secret = secret;
        this.salt = salt;
    }


    public void createTenant(String tenantId, String db,  String password) {

        //verify the db string to prevent SQL Injection
        if (!db.matches(VALID_DB_SCHEMA_NAME_REGEXP)) {
            throw new TenantCreationException("Invalid database name: " + db);
        }
        String url = urlPrefix + db;
        String encryptedPassword = encryptionService.encrypt(password, secret, salt);

        System.out.println(encryptedPassword);
        try {
            createDatabase(db, password);
        } catch (DataAccessException e) {
            throw new TenantCreationException("Error when creating db: " + db, e);
        }
        try (Connection connection = DriverManager.getConnection(url, db, password)) {
            DataSource tenantDataSource = new SingleConnectionDataSource(connection, false);
            runLiquibase(tenantDataSource);
        } catch (SQLException | LiquibaseException e) {
            throw new TenantCreationException("Error when populating db: ", e);
        }

        Tenant tenant = Tenant.builder()
                .tenantId(tenantId)
                .db(db)
//                .url(url)
//                .username(userName)
                .password(encryptedPassword)
                .build();
        System.out.println("inafika hapa");
        tenantRepository.save(tenant);
    }

    private void runLiquibase(DataSource dataSource) throws LiquibaseException {
        System.out.println("being called ============");
        SpringLiquibase liquibase = getSpringLiquibase(dataSource);
        liquibase.afterPropertiesSet();
    }

    private void createDatabase(String db, String password) {
        jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("CREATE DATABASE " + db));
//        jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("CREATE USER " + db + " WITH ENCRYPTED PASSWORD '" + password + "'"));
        jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("GRANT ALL PRIVILEGES ON DATABASE " + db + " TO " + "postgres"));
    }


    private SpringLiquibase getSpringLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setResourceLoader(resourceLoader);
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(tenantLiquibaseProperties.getChangeLog());
        liquibase.setContexts(tenantLiquibaseProperties.getContexts());
        liquibase.setDefaultSchema(tenantLiquibaseProperties.getDefaultSchema());
        liquibase.setLiquibaseSchema(tenantLiquibaseProperties.getLiquibaseSchema());
        liquibase.setLiquibaseTablespace(tenantLiquibaseProperties.getLiquibaseTablespace());
        liquibase.setDatabaseChangeLogTable(tenantLiquibaseProperties.getDatabaseChangeLogTable());
        liquibase.setDatabaseChangeLogLockTable(tenantLiquibaseProperties.getDatabaseChangeLogLockTable());
        liquibase.setDropFirst(tenantLiquibaseProperties.isDropFirst());
        liquibase.setShouldRun(tenantLiquibaseProperties.isEnabled());
        liquibase.setTag(tenantLiquibaseProperties.getTag());
        liquibase.setChangeLogParameters(tenantLiquibaseProperties.getParameters());
        liquibase.setRollbackFile(tenantLiquibaseProperties.getRollbackFile());
        liquibase.setTestRollbackOnUpdate(tenantLiquibaseProperties.isTestRollbackOnUpdate());
        return liquibase;
    }

}
