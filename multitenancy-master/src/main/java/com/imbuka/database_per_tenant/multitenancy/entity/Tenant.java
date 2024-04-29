package com.imbuka.database_per_tenant.multitenancy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tenant {

    @Id
    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "db")
    private String db;

//    @Column(name = "url")
//    private String url;
//
    @Column(name = "password")
    private String password;

//    @Column(name = "username")
//    private String username;
}
