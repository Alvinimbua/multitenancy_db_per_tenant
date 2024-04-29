package com.imbuka.database_per_tenant.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.imbuka.database_per_tenant.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemValue {

    @JsonProperty("itemId")
    private Long itemId;


    @JsonProperty("name")
    private String name;

    public static ItemValue fromEntity(Item item) {
        return ItemValue.builder()
                .itemId(item.getId())
                .name(item.getName())
                .build();
    }

    public static Item fromValue(ItemValue item) {
        return Item.builder()
                .id(item.getItemId())
                .name(item.getName())
                .build();
    }

}
