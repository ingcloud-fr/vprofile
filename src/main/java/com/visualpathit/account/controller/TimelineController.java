package com.visualpathit.account.controller;

import com.visualpathit.account.model.Post;
import com.visualpathit.account.model.User;
import com.visualpathit.account.service.PostService;
import com.visualpathit.account.service.SecurityService;
import com.visualpathit.account.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller for managing the timeline/wall functionality
 */
@Controller
public class TimelineController {

    private static final Logger logger = LoggerFactory.getLogger(TimelineController.class);
    private static final int PAGE_SIZE = 20;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    /**
     * Redirect /timeline to /welcome (public timeline is now on welcome page)
     */
    @GetMapping("/timeline")
    public String timeline(@RequestParam(value = "page", defaultValue = "0") int page) {
        logger.info("Redirecting /timeline to /welcome");
        return page > 0 ? "redirect:/welcome?page=" + page : "redirect:/welcome";
    }

    /**
     * Create a new post - redirect to welcome/post
     */
    @PostMapping("/timeline/post")
    public String createPost(@RequestParam("content") String content,
                            @RequestParam(value = "imageUrl", required = false) String imageUrl) {
        logger.info("Redirecting POST /timeline/post to /welcome/post");

        // Get current user
        String username = securityService.findLoggedInUsername();
        if (username == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(username);
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Validate and create post
        if (content == null || content.trim().isEmpty()) {
            return "redirect:/welcome?error=empty";
        }

        if (content.length() > 500) {
            return "redirect:/welcome?error=toolong";
        }

        postService.createPost(content, imageUrl, currentUser);
        return "redirect:/welcome";
    }

    /**
     * Display posts by the current user only
     */
    @GetMapping("/my-posts")
    public String myPosts(Model model,
                         @RequestParam(value = "page", defaultValue = "0") int page) {
        logger.info("Accessing my-posts, page: {}", page);

        // Get current user
        String username = securityService.findLoggedInUsername();
        if (username == null) {
            logger.error("No logged-in user found when accessing my-posts");
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(username);
        if (currentUser == null) {
            logger.error("User not found: {}", username);
            return "redirect:/login";
        }

        model.addAttribute("currentUser", currentUser);

        // Get posts by current user with pagination
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Post> postsPage = postService.findByAuthor(currentUser, pageable);

        model.addAttribute("posts", postsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postsPage.getTotalPages());
        model.addAttribute("totalPosts", postsPage.getTotalElements());
        model.addAttribute("hasNext", postsPage.hasNext());
        model.addAttribute("hasPrevious", postsPage.hasPrevious());

        logger.info("My-posts loaded with {} posts for user: {}", postsPage.getContent().size(), username);

        return "my-posts";
    }
}
