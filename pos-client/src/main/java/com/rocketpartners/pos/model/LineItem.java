package com.rocketpartners.pos.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class LineItem {

    private Product product;
    private int quantity;
}
