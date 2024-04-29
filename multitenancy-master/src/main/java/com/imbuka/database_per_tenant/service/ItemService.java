package com.imbuka.database_per_tenant.service;

import com.imbuka.database_per_tenant.model.ItemValue;

import java.util.List;

public interface ItemService {

    List<ItemValue> getItems();

    ItemValue getItem(long itemId);

    ItemValue createItem(ItemValue itemValue);

    ItemValue updateItem(ItemValue itemValue);

    void deleteItembyId(long itemId);
}
