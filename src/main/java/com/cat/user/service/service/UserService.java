package com.cat.user.service.service;

import com.cat.user.service.dto.request.CreateUserRequest;
import com.cat.user.service.dto.response.UserResponse;

public interface UserService {

    UserResponse registerUser(CreateUserRequest request);
}
