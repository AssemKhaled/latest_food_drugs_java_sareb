package com.example.food_drugs.responses;

import lombok.*;
import org.bson.types.ObjectId;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class PositionResponse {
    ObjectId _id;
    String deviceName ;
    String serverTime;
    double temperature;
    double humidity;
    double speed;
}
