package com.visualpathit.account.controller;

import com.visualpathit.account.model.User;
import com.visualpathit.account.service.PostLikeService;
import com.visualpathit.account.service.SecurityService;
import com.visualpathit.account.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for handling post like/unlike actions
 */
@Controller
public class PostLikeController {

    private static final Logger logger = LoggerFactory.getLogger(PostLikeController.class);

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    /**
     * Toggle like on a post (like if not liked, unlike if already liked)
     */
    @PostMapping("/post/{postId}/like")
    public String toggleLike(
            @PathVariable Long postId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        logger.info("Like toggle request for post: {}", postId);

        try {
            // Get current user
            String username = securityService.findLoggedInUsername();
            if (username == null) {
                logger.error("No logged-in user found when toggling like");
                return "redirect:/login";
            }

            User user = userService.findByUsername(username);
            if (user == null) {
                logger.error("User not found: {}", username);
                return "redirect:/login";
            }

            // Toggle the like
            boolean liked = postLikeService.toggleLike(postId, user);

            // Add success message
            if (liked) {
                redirectAttributes.addFlashAttribute("likeSuccess", "Post liké !");
                logger.info("User {} liked post {}", username, postId);
            } else {
                redirectAttributes.addFlashAttribute("likeSuccess", "Like retiré !");
                logger.info("User {} unliked post {}", username, postId);
            }

        } catch (Exception e) {
            logger.error("Error toggling like for post {}", postId, e);
            redirectAttributes.addFlashAttribute("likeError", "Erreur lors du like");
        }

        // Redirect to the referring page (or welcome if no referer)
        String referer = request.getHeader("Referer");
        String redirectUrl = (referer != null && !referer.isEmpty()) ? referer : "/welcome";

        logger.debug("Redirecting to: {}", redirectUrl);
        return "redirect:" + redirectUrl;
    }
}
