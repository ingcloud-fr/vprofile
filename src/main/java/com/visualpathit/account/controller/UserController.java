package com.visualpathit.account.controller;

import com.visualpathit.account.model.Post;
import com.visualpathit.account.model.User;
import com.visualpathit.account.service.PostLikeService;
import com.visualpathit.account.service.PostService;
import com.visualpathit.account.service.ProducerService;
import com.visualpathit.account.service.SecurityService;
import com.visualpathit.account.service.UserService;
import com.visualpathit.account.utils.MemcachedUtils;
import com.visualpathit.account.validator.UserValidator;
import jakarta.servlet.ServletContext;
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

import javax.imageio.ImageIO;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import jakarta.servlet.http.HttpSession;
import net.coobird.thumbnailator.Thumbnails;

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

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private ServletContext servletContext;

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

        User currentUser = null;
        if (username != null) {
            currentUser = userService.findByUsername(username);
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
        Page<Post> postsPage = postService.findAllPosts(pageable);

        // Enrich each post with like information for the current user
        if (currentUser != null) {
            final User finalCurrentUser = currentUser;
            postsPage.getContent().forEach(post -> {
                boolean isLiked = postLikeService.hasUserLiked(post, finalCurrentUser);
                post.setLikedByCurrentUser(isLiked);
                logger.debug("Post {} liked by {}: {}", post.getId(), finalCurrentUser.getUsername(), isLiked);
            });
        }

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
        logger.debug("========== UPDATE USER PROFILE DEBUG START ==========");
        logger.debug("Updating profile for username: {}", username);

        User user = userService.findByUsername(username);
        logger.debug("User found in database: {}", user != null);

        if (user != null) {
            logger.debug("Current user email: {}", user.getUserEmail());
            logger.debug("New user email: {}", userForm.getUserEmail());
            logger.debug("Current profile image: {}", user.getProfileImg());

            updateUserDetails(user, userForm);
            logger.debug("User details updated");

            userService.update(user);
            logger.debug("User saved to database");
        } else {
            logger.error("User not found: {}", username);
        }

        logger.debug("========== UPDATE USER PROFILE DEBUG END ==========");
        return "redirect:/welcome";
    }

    @PostMapping("/profile/upload-photo")
    public String uploadPhoto(@RequestParam("photo") MultipartFile file, HttpSession session) {
        logger.debug("========== UPLOAD PHOTO DEBUG START ==========");

        // Get current logged-in user
        String username = securityService.findLoggedInUsername();
        logger.debug("Logged in username: {}", username);

        if (username == null) {
            logger.error("No logged-in user found when uploading photo");
            logger.debug("========== UPLOAD PHOTO DEBUG END (NO USER) ==========");
            return "redirect:/login";
        }

        User user = userService.findByUsername(username);
        logger.debug("User found in database: {}", user != null);

        if (user == null) {
            logger.error("User not found: {}", username);
            logger.debug("========== UPLOAD PHOTO DEBUG END (USER NOT FOUND) ==========");
            return "redirect:/login";
        }

        // Validation: Empty file check
        logger.debug("File original name: {}", file.getOriginalFilename());
        logger.debug("File size: {} bytes", file.getSize());
        logger.debug("File empty: {}", file.isEmpty());

        if (file.isEmpty()) {
            logger.warn("Attempted to upload empty file");
            logger.debug("========== UPLOAD PHOTO DEBUG END (EMPTY FILE) ==========");
            return "redirect:/welcome?error=emptyFile";
        }

        // Validation: File type (images only)
        String contentType = file.getContentType();
        logger.debug("File content type: {}", contentType);

        if (contentType == null || !contentType.startsWith("image/")) {
            logger.warn("Attempted to upload non-image file: {}", contentType);
            logger.debug("========== UPLOAD PHOTO DEBUG END (INVALID TYPE) ==========");
            return "redirect:/welcome?error=invalidType";
        }

        // Validation: File size (max 5 MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            logger.warn("Attempted to upload file too large: {} bytes", file.getSize());
            logger.debug("========== UPLOAD PHOTO DEBUG END (FILE TOO LARGE) ==========");
            return "redirect:/welcome?error=fileTooLarge";
        }

        logger.debug("All validations passed, proceeding with image processing");

        try {
            // Read the original image
            logger.debug("Reading original image from multipart file");
            BufferedImage originalImage = ImageIO.read(file.getInputStream());

            if (originalImage == null) {
                logger.error("Failed to read image file - ImageIO.read returned null");
                logger.debug("========== UPLOAD PHOTO DEBUG END (READ FAILED) ==========");
                return "redirect:/welcome?error=uploadFailed";
            }

            // Calculate dimensions for centered square crop
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            int size = Math.min(width, height);
            logger.debug("Original image dimensions: {}x{}, crop size: {}", width, height, size);

            int x = (width - size) / 2;
            int y = (height - size) / 2;
            logger.debug("Crop position: x={}, y={}", x, y);

            // Crop to square and resize to 300x300
            logger.debug("Starting crop and resize operation");

            // Generate unique filename (always save as JPG)
            String filename = UUID.randomUUID().toString() + ".jpg";
            logger.debug("Generated filename: {}", filename);

            // Use dedicated external volume for uploads (persistent across container restarts)
            // This path is mounted as a Docker volume in docker-compose.yml
            String uploadDirectory = "/var/lib/vprofile/uploads/profiles";
            Path uploadPath = Paths.get(uploadDirectory);
            logger.debug("Upload directory path: {}", uploadPath);
            logger.debug("Upload directory exists: {}", Files.exists(uploadPath));

            // Create directory if it doesn't exist
            if (!Files.exists(uploadPath)) {
                logger.debug("Creating upload directory");
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath);
            }

            // Check directory permissions
            logger.debug("Directory readable: {}", Files.isReadable(uploadPath));
            logger.debug("Directory writable: {}", Files.isWritable(uploadPath));
            logger.debug("Directory executable: {}", Files.isExecutable(uploadPath));

            // Save the cropped and resized image directly to file
            Path filePath = uploadPath.resolve(filename);
            logger.debug("Full file path: {}", filePath);
            logger.debug("Saving image to disk using Thumbnails.toFile()");

            // Use Thumbnails to write directly to file (more reliable than ImageIO.write)
            Thumbnails.of(originalImage)
                .sourceRegion(x, y, size, size)  // Centered square crop
                .size(300, 300)                   // Resize to 300x300
                .outputFormat("jpg")              // Force JPG format
                .outputQuality(0.9)               // High quality (0.0 to 1.0)
                .toFile(filePath.toFile());

            // Set file permissions to 644 (rw-r--r--) so Tomcat can read it
            // This is necessary because Thumbnails.toFile() creates files with restrictive permissions
            Files.setPosixFilePermissions(filePath,
                java.nio.file.attribute.PosixFilePermissions.fromString("rw-r--r--"));
            logger.debug("File permissions set to rw-r--r-- (644)");

            logger.info("Image saved successfully to: {}", filePath);

            // Immediate verification after write
            boolean fileExistsImmediately = Files.exists(filePath);
            logger.debug("File exists IMMEDIATELY after write: {}", fileExistsImmediately);

            if (fileExistsImmediately) {
                long fileSize = Files.size(filePath);
                logger.info("File IMMEDIATELY verified to exist, size: {} bytes", fileSize);

                // Check file permissions
                logger.debug("File readable: {}", Files.isReadable(filePath));
                logger.debug("File writable: {}", Files.isWritable(filePath));

                // List directory contents to confirm
                logger.debug("Directory contents:");
                try (var stream = Files.list(uploadPath)) {
                    stream.forEach(path -> logger.debug("  - {}", path.getFileName()));
                }
            } else {
                logger.error("File was NOT saved correctly at: {}", filePath);

                // Debug: List directory contents
                logger.debug("Directory contents after failed write:");
                try (var stream = Files.list(uploadPath)) {
                    stream.forEach(path -> logger.debug("  - {}", path.getFileName()));
                } catch (IOException listEx) {
                    logger.error("Cannot list directory: {}", listEx.getMessage());
                }
            }

            // IMPORTANT: Store relative URL starting with /
            // This URL will be served by Spring's ResourceHandler configuration
            String photoUrl = "/uploads/profiles/" + filename;
            logger.debug("Photo URL for database: {}", photoUrl);
            logger.debug("Current user profileImg before update: {}", user.getProfileImg());

            user.setProfileImg(photoUrl);
            user.setProfileImgPath(filePath.toString());
            logger.debug("User object updated with new photo info");

            // Use update() instead of save() to avoid re-encoding password
            logger.debug("Calling userService.update() to save to database");
            userService.update(user);
            logger.info("Photo URL saved to database: {}", photoUrl);
            logger.info("File path saved to database: {}", filePath);

            // Verify file again after database update
            boolean fileExistsAfterDB = Files.exists(filePath);
            logger.debug("File exists AFTER database update: {}", fileExistsAfterDB);

            if (fileExistsAfterDB) {
                long fileSize = Files.size(filePath);
                logger.info("File verified to exist at: {}, size: {} bytes", filePath, fileSize);
            } else {
                logger.error("File was NOT saved correctly at: {}", filePath);
            }

            // IMPORTANT: Update session to refresh navbar photo
            session.setAttribute("currentUser", user);
            logger.debug("Session updated with new profile photo");

            logger.info("Profile photo updated successfully for user: {}", username);
            logger.debug("========== UPLOAD PHOTO DEBUG END (SUCCESS) ==========");
            return "redirect:/welcome?success=photoUploaded";

        } catch (IOException e) {
            logger.error("Failed to upload photo for user: {}", username, e);
            logger.debug("Exception details: {}", e.getMessage());
            logger.debug("========== UPLOAD PHOTO DEBUG END (EXCEPTION) ==========");
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
