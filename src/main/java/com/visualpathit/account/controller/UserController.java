package com.visualpathit.account.controller;

import com.visualpathit.account.model.User;
import com.visualpathit.account.service.PostService;
import com.visualpathit.account.service.ProducerService;
import com.visualpathit.account.service.SecurityService;
import com.visualpathit.account.service.UserService;
import com.visualpathit.account.utils.MemcachedUtils;
import com.visualpathit.account.validator.UserValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final int PAGE_SIZE = 20;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private ProducerService producerService;

    @Autowired
    private PostService postService;

    @GetMapping("/")
    public String home() {
        // Redirect authenticated users to welcome page
        // Non-authenticated users will be redirected to login by Spring Security
        return "redirect:/welcome";
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("userForm", new User());
        return "registration";
    }

    @PostMapping("/registration")
    public String registration(@ModelAttribute("userForm") @Valid User userForm, BindingResult bindingResult,
                              Model model, HttpServletRequest request) {
        logger.info("Registration attempt for username: {}", userForm.getUsername());

        userValidator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            logger.warn("Registration validation failed for username: {}", userForm.getUsername());
            return "registration";
        }

        userService.save(userForm);
        logger.info("User created successfully: {}", userForm.getUsername());

        boolean loginSuccessful = securityService.autologin(userForm.getUsername(), userForm.getPasswordConfirm(), request);
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
    public String welcome(Model model,
                         @RequestParam(value = "page", defaultValue = "0") int page) {
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

        // Get all posts with pagination for the public timeline
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page postsPage = postService.findAllPosts(pageable);

        model.addAttribute("posts", postsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postsPage.getTotalPages());
        model.addAttribute("totalPosts", postsPage.getTotalElements());
        model.addAttribute("hasNext", postsPage.hasNext());
        model.addAttribute("hasPrevious", postsPage.hasPrevious());

        logger.info("Timeline loaded with {} posts", postsPage.getContent().size());

        return "welcome";
    }

    @PostMapping("/welcome/post")
    public String createPostFromWelcome(@RequestParam("content") String content,
                                       @RequestParam(value = "imageUrl", required = false) String imageUrl) {
        logger.info("Creating new post from welcome page");

        // Get current user
        String username = securityService.findLoggedInUsername();
        if (username == null) {
            logger.error("No logged-in user found when creating post");
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(username);
        if (currentUser == null) {
            logger.error("User not found: {}", username);
            return "redirect:/login";
        }

        // Validate content
        if (content == null || content.trim().isEmpty()) {
            logger.warn("Attempted to create post with empty content");
            return "redirect:/welcome?error=empty";
        }

        if (content.length() > 500) {
            logger.warn("Attempted to create post with content exceeding 500 characters");
            return "redirect:/welcome?error=toolong";
        }

        // Create post
        postService.createPost(content, imageUrl, currentUser);

        logger.info("Post created successfully by user: {}", username);
        return "redirect:/welcome";
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

    @PostMapping("/profile/upload-photo")
    public String uploadPhoto(@RequestParam("photo") MultipartFile file) {
        // Get current logged-in user
        String username = securityService.findLoggedInUsername();
        if (username == null) {
            logger.error("No logged-in user found when uploading photo");
            return "redirect:/login";
        }

        User user = userService.findByUsername(username);
        if (user == null) {
            logger.error("User not found: {}", username);
            return "redirect:/login";
        }

        // Validation: Empty file check
        if (file.isEmpty()) {
            logger.warn("Attempted to upload empty file");
            return "redirect:/welcome?error=emptyFile";
        }

        // Validation: File type (images only)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            logger.warn("Attempted to upload non-image file: {}", contentType);
            return "redirect:/welcome?error=invalidType";
        }

        // Validation: File size (max 5 MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            logger.warn("Attempted to upload file too large: {} bytes", file.getSize());
            return "redirect:/welcome?error=fileTooLarge";
        }

        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // Create upload directory if it doesn't exist
            // Use webapp directory for file storage
            String webappPath = System.getProperty("catalina.home");
            if (webappPath == null) {
                webappPath = System.getProperty("user.dir");
            }
            Path uploadPath = Paths.get(webappPath, "webapps", "ROOT", "resources", "Images", "profiles");

            // Create directory if it doesn't exist
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath);
            }

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File uploaded successfully: {}", filePath);

            // Update user profile image URL
            String photoUrl = "/resources/Images/profiles/" + filename;
            user.setProfileImg(photoUrl);
            user.setProfileImgPath(filePath.toString());
            userService.save(user);

            logger.info("Profile photo updated successfully for user: {}", username);
            return "redirect:/welcome?success=photoUploaded";

        } catch (IOException e) {
            logger.error("Failed to upload photo for user: {}", username, e);
            return "redirect:/welcome?error=uploadFailed";
        }
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
