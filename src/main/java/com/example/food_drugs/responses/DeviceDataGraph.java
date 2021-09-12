package com.example.food_drugs.responses;


import java.sql.Date;

public interface DeviceDataGraph {
    int getName() ;
    int getId() ;
    Date getLastupdate();
    double getLastTemp();
    double getLastHum();
    String getStoringCategory();
}
