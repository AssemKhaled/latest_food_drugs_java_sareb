package com.example.food_drugs.responses;

import lombok.*;
import java.util.*;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class GraphDataWrapper {
    String series;
    List<GraphObject> graphObjectList;
}
