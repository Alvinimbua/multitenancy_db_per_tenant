package com.imbuka.database_per_tenant.repository;

import com.imbuka.database_per_tenant.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
