package com.example.food_drugs.responses;

import lombok.*;
import org.bson.types.ObjectId;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class MongoInventoryWrapper {
   private Double temperature;

   private Double humidity ;

   private Long inventoryId ;

   private Date createDate ;

   private String inventoryName ;

   private ObjectId _id;

}
