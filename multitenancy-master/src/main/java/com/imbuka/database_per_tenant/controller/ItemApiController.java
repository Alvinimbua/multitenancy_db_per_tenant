package com.imbuka.database_per_tenant.controller;

import com.imbuka.database_per_tenant.exception.NotFoundException;
import com.imbuka.database_per_tenant.model.ItemValue;
import com.imbuka.database_per_tenant.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ItemApiController extends ApiExceptionHandler {

    private final ItemService itemService;

    @GetMapping(value = "/items", produces = {ContentType.ITEMS_1_0})
    public ResponseEntity<List<ItemValue>> getItems() {
        List<ItemValue> itemValues = itemService.getItems();
        return new ResponseEntity<>(itemValues, HttpStatus.OK);
    }

    @GetMapping(value = "/items/{itemId}", produces = {ContentType.ITEM_1_0})
    public ResponseEntity<ItemValue> getItem(@PathVariable("itemId") long itemId) {
        try {
            ItemValue branch = itemService.getItem(itemId);
            return new ResponseEntity<>(branch, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @PostMapping(value = "/items",
            consumes = {ContentType.ITEM_1_0},
            produces = {ContentType.ITEM_1_0})
    public ResponseEntity<ItemValue> createItem( @RequestBody ItemValue itemValue) {
        ItemValue item = itemService.createItem(itemValue);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.LOCATION, "/items/" + item.getItemId());
        return new ResponseEntity<>(item, headers, HttpStatus.CREATED);
    }

    @PutMapping(value = "/items/{itemId}",
            consumes = {ContentType.ITEM_1_0},
            produces = {ContentType.ITEM_1_0})
    ResponseEntity<ItemValue> updateItem(@PathVariable long itemId,  @RequestBody ItemValue itemValue) {
        itemValue.setItemId(itemId);
        try {
            ItemValue item = itemService.updateItem(itemValue);
            return new ResponseEntity<>(item, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @DeleteMapping("/items/{itemId}")
    ResponseEntity<Void> deleteItem(@PathVariable long itemId) {
        try {
            itemService.deleteItembyId(itemId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    /**
     * this method asynchronously retrieves a list of ItemValue objects from the itemService
     * and wraps it in a CompletableFuture containing a ResponseEntity.
     * This allows for non-blocking execution of the method,
     * making it suitable for handling potentially long-running operations without blocking the main thread.
     *
     * @return CompletableFuture
     */
    @GetMapping(value = "/async/items", produces = {ContentType.ITEMS_1_0})
    //CompletableFuture -> represents a future result of an asynchronous operation.
    public CompletableFuture<ResponseEntity<List<ItemValue>>> asyncGetProducts() {
        List<ItemValue> itemValues = itemService.getItems();
        return CompletableFuture.completedFuture(new ResponseEntity<>(itemValues, HttpStatus.OK));
    }

    @GetMapping(value = "/async/items/{itemId}", produces = {ContentType.ITEM_1_0})
    public CompletableFuture<ResponseEntity<ItemValue>> asyncGetProduct(@PathVariable("itemId") long itemId) {
        return CompletableFuture.completedFuture(getItem(itemId));
    }

}
