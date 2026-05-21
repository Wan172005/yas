package com.yas.order.viewmodel.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCheckoutListVm {

    private Long id;
    private String name;
    private Double price;
    private Long taxClassId;
}
