package com.example.food_drugs.responses;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class InventorySamWrapper {
    private int id;
    private String name;
    private String lastDataId;
}
