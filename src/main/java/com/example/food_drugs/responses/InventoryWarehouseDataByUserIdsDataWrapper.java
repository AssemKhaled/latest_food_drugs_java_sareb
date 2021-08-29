package com.example.food_drugs.responses;

import com.example.food_drugs.entity.MonogoInventoryLastData;
import lombok.*;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class InventoryWarehouseDataByUserIdsDataWrapper {
    java.lang.Integer userId;
    java.lang.Integer inventoryId;
    java.lang.String wareHouseName;
    java.lang.String inventoryName;
    java.lang.String storingCategory;
    java.lang.String lastUpdate;
    java.lang.String lastDataId;
    MonogoInventoryLastData lastData;
    List<MonogoInventoryLastData> lastDataList;
    GraphDataWrapper graphData ;
    Integer lastDataListSize;
}
