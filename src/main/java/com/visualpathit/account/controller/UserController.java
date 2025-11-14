package com.visualpathit.account.controller;

import com.visualpathit.account.model.User;
import com.visualpathit.account.service.ProducerService;
import com.visualpathit.account.service.SecurityService;
import com.visualpathit.account.service.UserService;
import com.visualpathit.account.utils.MemcachedUtils;
import com.visualpathit.account.validator.UserValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private ProducerService producerService;

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("userForm", new User());
        return "registration";
    }

    @PostMapping("/registration")
    public String registration(@ModelAttribute("userForm") @Valid User userForm, BindingResult bindingResult, Model model) {
        logger.info("Registration attempt for username: {}", userForm.getUsername());

        userValidator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            logger.warn("Registration validation failed for username: {}", userForm.getUsername());
            return "registration";
        }

        userService.save(userForm);
        logger.info("User created successfully: {}", userForm.getUsername());

        boolean loginSuccessful = securityService.autologin(userForm.getUsername(), userForm.getPasswordConfirm());
        if (!loginSuccessful) {
            logger.error("Auto-login failed after registration for user: {}, redirecting to login page", userForm.getUsername());
            model.addAttribute("message", "Account created successfully! Please login.");
            return "redirect:/login?registered";
        }

        logger.info("Registration and auto-login successful for user: {}, redirecting to welcome page", userForm.getUsername());
        return "redirect:/welcome";
    }

    @GetMapping("/login")
    public String login(Model model,
                        @RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "registered", required = false) String registered) {
        if (error != null) {
            logger.warn("Login page accessed with error parameter");
            model.addAttribute("error", "Your username and password is invalid.");
        }
        if (logout != null) {
            logger.info("Login page accessed after logout");
            model.addAttribute("message", "You have been logged out successfully.");
        }
        if (registered != null) {
            logger.info("Login page accessed after successful registration");
            model.addAttribute("message", "Account created successfully! Please login with your credentials.");
        }
        return "login";
    }

    @GetMapping("/welcome")
    public String welcome(Model model) {
        // Get currently logged-in user
        String username = securityService.findLoggedInUsername();
        logger.info("Welcome page accessed by user: {}", username != null ? username : "anonymous");

        if (username != null) {
            User currentUser = userService.findByUsername(username);
            if (currentUser != null) {
                model.addAttribute("currentUser", currentUser);
                logger.info("User profile loaded successfully for: {}", username);
            } else {
                logger.error("User not found in database: {}", username);
            }
        } else {
            logger.warn("Welcome page accessed without authentication");
        }
        return "welcome";
    }

    @GetMapping("/index")
    public String indexHome(Model model) {
        return "index_home";
    }

    @GetMapping("/users")
    public String getAllUsers(Model model) {
        List<User> users = userService.getList();
        model.addAttribute("users", users);
        return "userList";
    }

    @GetMapping("/users/{id}")
    public String getOneUser(@PathVariable("id") String id, Model model) {
        String result;
        try {
            User userData = MemcachedUtils.memcachedGetData(id);
            if (userData != null) {
                result = "Data is From Cache";
                model.addAttribute("user", userData);
            } else {
                User user = userService.findById(Long.parseLong(id));
                result = MemcachedUtils.memcachedSetData(user, id);
                if (result == null) {
                    result = "Memcached Connection Failure !!";
                }
                model.addAttribute("user", user);
            }
            model.addAttribute("Result", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "user";
    }

    @GetMapping("/user/{username}")
    public String userUpdate(@PathVariable("username") String username, Model model) {
        User user = userService.findByUsername(username);
        model.addAttribute("user", user);
        return "userUpdate";
    }

    @PostMapping("/user/{username}")
    public String userUpdateProfile(@PathVariable("username") String username, @ModelAttribute("user") User userForm) {
        User user = userService.findByUsername(username);
        updateUserDetails(user, userForm);
        userService.update(user);
        return "redirect:/welcome";
    }

//    @GetMapping("/user/rabbit")
//    public String rabbitmqSetUp() {
//        for (int i = 0; i < 20; i++) {
//            producerService.produceMessage(generateString());
//        }
//        return "rabbitmq";
//    }

    private void updateUserDetails(User user, User userForm) {
        // Only update modifiable fields (username is immutable)
        user.setUserEmail(userForm.getUserEmail());
        user.setPermanentAddress(userForm.getPermanentAddress());
        user.setSkills(userForm.getSkills());
    }

    private static String generateString() {
        return "uuid = " + UUID.randomUUID().toString();
    }
}
