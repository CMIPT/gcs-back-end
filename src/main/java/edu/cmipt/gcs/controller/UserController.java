package edu.cmipt.gcs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.cmipt.gcs.pojo.user.UserDTO;
import edu.cmipt.gcs.pojo.user.UserPO;
import edu.cmipt.gcs.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequestMapping("/user")
@Tag(name = "User", description = "User Related APIs")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    @Operation(
        summary = "Create a new user",
        description = "Create a new user with the given information",
        tags = { "User", "Post Method" }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "User creation failed")
    })
    public ResponseEntity<Void> createUser(@RequestBody UserDTO user) {
        if (user == null ||
            user.getUsername() == null ||
            user.getEmail() == null ||
            user.getUserPassword() == null) {
            return ResponseEntity.badRequest().build();
        }
        // there may be some check before....
        boolean res = userService.save(new UserPO(user));
        if (res) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
