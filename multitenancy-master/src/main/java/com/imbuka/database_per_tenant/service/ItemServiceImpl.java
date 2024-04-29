package com.imbuka.database_per_tenant.service;

import com.imbuka.database_per_tenant.entity.Item;
import com.imbuka.database_per_tenant.model.ItemValue;
import com.imbuka.database_per_tenant.multitenancy.util.TenantContext;
import com.imbuka.database_per_tenant.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ItemValue> getItems() {
        return itemRepository.findAll().stream()
                .map(ItemValue::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemValue getItem(long itemId) {
        return itemRepository.findById(itemId)
                .map(ItemValue::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Item " + itemId + " not found"));
    }

    @Override
    @Transactional
    public ItemValue createItem(ItemValue itemValue) {
        String tenantId = TenantContext.getTenantId();
        Item item = Item.builder()
                .name(itemValue.getName())
                .build();
        item = itemRepository.save(item);
        return ItemValue.fromEntity(item);
    }

    @Override
    @Transactional
    public ItemValue updateItem(ItemValue itemValue) {
        Item item = itemRepository.findById(itemValue.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Item" + itemValue.getItemId() + " not found"));
        item.setName(itemValue.getName());
        return ItemValue.fromEntity(item);
    }

    @Override
    @Transactional
    public void deleteItembyId(long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item " + itemId + " not found"));
        itemRepository.delete(item);


    }
}
