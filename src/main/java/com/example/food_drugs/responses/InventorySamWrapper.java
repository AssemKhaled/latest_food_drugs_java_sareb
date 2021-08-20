package com.example.food_drugs.responses;



public class InventorySamWrapper {
    private Long id;
    private String name;
    private String lastDataId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastDataId() {
        return lastDataId;
    }

    public void setLastDataId(String lastDataId) {
        this.lastDataId = lastDataId;
    }



    public InventorySamWrapper(Long id, String name, String lastDataId) {
        this.id = id;
        this.name = name;
        this.lastDataId = lastDataId;
    }
}
