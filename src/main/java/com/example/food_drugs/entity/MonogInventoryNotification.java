package com.example.food_drugs.entity;

import java.util.Date;

import javax.persistence.Id;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tc_inventory_notifications")
public class MonogInventoryNotification {

	@Id
	private ObjectId _id;
	private String type;
	private Long inventory_id;
	private Date create_date;
	private Object attributes;
	
	public MonogInventoryNotification() {
		super();

	}

	public MonogInventoryNotification(ObjectId _id, String type, Long inventory_id, Date create_date,
			Object attributes) {
		super();
		this._id = _id;
		this.type = type;
		this.inventory_id = inventory_id;
		this.create_date = create_date;
		this.attributes = attributes;
	}



	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public Object getAttributes() {
		return attributes;
	}

	public void setAttributes(Object attributes) {
		this.attributes = attributes;
	}
	
	

	
}
