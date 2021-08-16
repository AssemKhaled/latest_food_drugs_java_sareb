package com.example.food_drugs.entity;

import java.util.Date;

import javax.persistence.Id;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tc_inventory_last_data")
public class MonogoInventoryLastData {
	
	@Id
	private ObjectId _id;
	private Double temperature;
	private Double humidity;
	private Long inventory_id;
	private Date create_date;
	
	public ObjectId get_id() {
		return _id;
	}
	public void set_id(ObjectId _id) {
		this._id = _id;
	}
	public Double getTemperature() {
		return temperature;
	}
	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}
	public Double getHumidity() {
		return humidity;
	}
	public void setHumidity(Double humidity) {
		this.humidity = humidity;
	}
	public Long getInventory_id() {
		return inventory_id;
	}
	public void setInventory_id(Long inventory_id) {
		this.inventory_id = inventory_id;
	}
	public Date getCreate_date() {
		return create_date;
	}
	public void setCreate_date(Date create_date) {
		this.create_date = create_date;
	}
	
	
	
	
	public MonogoInventoryLastData() {

	}
	public MonogoInventoryLastData(ObjectId _id, Double temperature, Double humidity, Long inventory_id, Date create_date) {
		super();
		this._id = _id;
		this.temperature = temperature;
		this.humidity = humidity;
		this.inventory_id = inventory_id;
		this.create_date = create_date;
	}
	
	
}
