package com.example.food_drugs.helpers.Impl;

import com.example.examplequerydslspringdatajpamaven.entity.User;
import com.example.examplequerydslspringdatajpamaven.repository.DeviceRepository;
import com.example.examplequerydslspringdatajpamaven.service.UserServiceImpl;
import com.example.food_drugs.exception.ApiGetException;
import com.example.food_drugs.helpers.AssistantService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Assem
 */
@Service
public class AssistantServiceImpl implements AssistantService {
    private static final Log logger = LogFactory.getLog(AssistantServiceImpl.class);
    private final UserServiceImpl userServiceImpl;
    private final DeviceRepository deviceRepository;

    public AssistantServiceImpl(UserServiceImpl userServiceImpl, DeviceRepository deviceRepository) {
        this.userServiceImpl = userServiceImpl;
        this.deviceRepository = deviceRepository;
    }



    @Override
    public List<Long> getChildrenOfUser(Long userId) {

        List<Long> userIds = new ArrayList<>();
        User user = userServiceImpl.findById(userId);
        userServiceImpl.resetChildernArray();

        if (userId != 0) {
            if (user == null) {
                throw new ApiGetException("This User is not Found");
            }
            if (user.getDelete_date() != null) {
                throw new ApiGetException("This User Was Delete at : " + user.getDelete_date());
            }
            if (user.getAccountType().equals(4)) {
                userIds.add(userId);
            } else {
                List<User> childrenUsers = userServiceImpl.getActiveAndInactiveChildern(userId);
                if (childrenUsers.isEmpty()) {
                    userIds.add(userId);
                } else {
                    userIds.add(userId);
                    for (User object : childrenUsers) {
                        userIds.add(object.getId());
                    }
                }
            }
            return userIds;
        }
        throw new ApiGetException ("User Id is Invalid");
    }


}