package com.example.food_drugs.responses;

import com.example.food_drugs.entity.MonogoInventoryLastData;
import lombok.*;
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder

public class DeviceResponseDataWrapper {
    java.lang.Integer id ;
    java.lang.String deviceName;
    java.lang.Double lastTemp;
    java.lang.Double lastHum;
    java.lang.String lastUpdate;
    java.lang.String storingCategory;
    GraphDataWrapper graphData ;
}



