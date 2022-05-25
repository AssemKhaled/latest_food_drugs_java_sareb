package com.example.food_drugs.helpers;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class Dictionary {

    public String Type(String type){
//        String storing;
        if (type.equals("SCD1")){
            return  "Drugs: Frozen";
        } else if (type.equals("SCD2")) {
            return "Drugs: Chilled";
        } else if (type.equals("SCD3")) {
            return "Drugs: Room Temperature";
        } else if (type.equals("SCC1")) {
            return "Cosmetics: Room Temperatue";
        } else if (type.equals("SCM1")) {
            return "Medical Equipment and Products: Frozen";
        } else if (type.equals("SCM2")) {
            return "Medical Equipment and Products: Chilled";
        } else if (type.equals("SCM3")) {
            return "Medical Equipment and Products: Cold Storage";
        } else if (type.equals("SCM4")) {
            return "Medical Equipment and Products: Room Temperatue";
        } else if (type.equals("SCM5")) {
            return "Medical Equipment and Products: No Heat Exposure";
        } else if (type.equals("SCF1")) {
            return "Food: Dry";
            
        } else if (type.equals("SCF2")) {
            return "Food: Chilled";
        } else if (type.equals("SCF3")) {
            return "Food: Chilled Vegetables and Fruits";
        } else if (type.equals("SCF4")) {
            return "Food: Frozen";
        } else if (type.equals("SCA1")) {
            return "Fodder(Animal Feed): Fodder";
        }
        else if (type.equals("SCP1")){
            return "Pesticides: Pesticides";
        }
        else {
         return "NOT FOUND";
        }
    }
}
