package com.rbaciam.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import com.rbaciam.dto.PaginatedResponse;
import com.rbaciam.dto.UserCreateDTO;
import com.rbaciam.dto.UserDTO;
import com.rbaciam.dto.UserUpdateDTO;

public interface UserService {

	Map<String, Object> createUser(UserCreateDTO userCreateDTO, Long userId);

	Map<String, Object> verifyUserToken(String authToken);

	Map<String, Object> assignPassword(String authToken, Long userId, String password);

	Map<String, Object> updateUser(Long id, UserUpdateDTO userUpdateDTO, Long userId);

	void deleteUser(Long id, Long requesterId);

	Optional<UserDTO> getUserById(Long targetUserId, Long requestingUserId);

	PaginatedResponse<UserDTO> getAllUsers(int page, int size, Long userId, String search);

	PaginatedResponse<UserDTO> filterUsers(Long companyId, String status, Long roleId, LocalDateTime createdFrom,
			LocalDateTime createdTo, int page, int size, Long userId);

	UserDTO changeUserRole(Long userId, Long roleId, Long requesterId);

	PaginatedResponse<UserDTO> getUsersByCompanyId(Long companyId, int page, int size, Long userId, String search);

}