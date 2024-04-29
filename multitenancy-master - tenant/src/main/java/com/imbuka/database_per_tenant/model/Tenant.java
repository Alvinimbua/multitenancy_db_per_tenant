package com.imbuka.database_per_tenant.model;

import jakarta.persistence.*;
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
//    @Column(name = "username")
//    private String username;


    @Column(name = "password")
    private String password;

}
