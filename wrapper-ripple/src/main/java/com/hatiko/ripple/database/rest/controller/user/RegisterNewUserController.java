package com.hatiko.ripple.database.rest.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hatiko.ripple.database.dto.UserDTO;
import com.hatiko.ripple.database.service.XrpDatabaseOperator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/db/register")
public class RegisterNewUserController {

	private final XrpDatabaseOperator userDataBaseOperator;

	@PostMapping
	public ResponseEntity<UserDTO> registerNewUser(@RequestBody UserDTO newUser) {

		log.info("Request to register a user");

		UserDTO response = userDataBaseOperator.registerNewUser(newUser);

		log.info("Response from userDataBaseOperator | registered user");
		
		return ResponseEntity.ok(response);
	}
}