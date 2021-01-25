package com.example.food_drugs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Component;
import com.example.food_drugs.entity.UserSFDA;


@Component
public interface UserRepositorySFDA extends  JpaRepository<UserSFDA, Long>, QueryDslPredicateExecutor<UserSFDA> {

}
