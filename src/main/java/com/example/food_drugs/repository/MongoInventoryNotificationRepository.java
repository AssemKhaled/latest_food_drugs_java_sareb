package com.example.food_drugs.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.food_drugs.entity.MonogInventoryNotification;

public interface MongoInventoryNotificationRepository extends MongoRepository<MonogInventoryNotification, String>{

}
