package com.example.food_drugs.Request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class GetAllInventoriesLastInfoRequest {
    Long userId ;
    String search ;
    int offset ;
}
