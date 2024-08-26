package edu.cmipt.gcs.controller;

import edu.cmipt.gcs.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@Tag(name = "User", description = "User Related APIs")
public class UserController {
    @Autowired private UserService userService;
}
