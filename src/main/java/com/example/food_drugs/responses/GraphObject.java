package com.example.food_drugs.responses;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class GraphObject {
    String name;
    double value;
}
