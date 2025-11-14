package com.visualpathit.account.service;

import com.visualpathit.account.model.Post;
import com.visualpathit.account.model.PostLike;
import com.visualpathit.account.model.User;
import com.visualpathit.account.repository.PostLikeRepository;
import com.visualpathit.account.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing post likes
 */
@Service
public class PostLikeService {

    private static final Logger logger = LoggerFactory.getLogger(PostLikeService.class);

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostRepository postRepository;

    /**
     * Toggle like on a post (like if not liked, unlike if already liked)
     *
     * @param postId The ID of the post
     * @param user The user toggling the like
     * @return true if liked, false if unliked
     */
    @Transactional
    public boolean toggleLike(Long postId, User user) {
        logger.debug("Toggling like for post {} by user {}", postId, user.getUsername());

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    logger.error("Post not found: {}", postId);
                    return new RuntimeException("Post not found with id: " + postId);
                });

        // Check if user already liked this post
        if (postLikeRepository.existsByPostAndUser(post, user)) {
            // Unlike: remove the like
            logger.debug("User {} already liked post {}, removing like", user.getUsername(), postId);
            postLikeRepository.deleteByPostAndUser(post, user);
            logger.info("Post {} unliked by user {}", postId, user.getUsername());
            return false;
        } else {
            // Like: create new like
            logger.debug("User {} has not liked post {}, creating like", user.getUsername(), postId);
            PostLike like = new PostLike(post, user);
            postLikeRepository.save(like);
            logger.info("Post {} liked by user {}", postId, user.getUsername());
            return true;
        }
    }

    /**
     * Check if a user has liked a post
     *
     * @param postId The ID of the post
     * @param user The user to check
     * @return true if the user has liked the post
     */
    public boolean hasUserLiked(Long postId, User user) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            logger.warn("Cannot check like status: post {} not found", postId);
            return false;
        }
        return postLikeRepository.existsByPostAndUser(post, user);
    }

    /**
     * Get the number of likes for a post
     *
     * @param postId The ID of the post
     * @return The number of likes
     */
    public long getLikesCount(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            logger.warn("Cannot count likes: post {} not found", postId);
            return 0;
        }
        return postLikeRepository.countByPost(post);
    }

    /**
     * Get the number of likes for a post
     *
     * @param post The post entity
     * @return The number of likes
     */
    public long getLikesCount(Post post) {
        if (post == null) {
            return 0;
        }
        return postLikeRepository.countByPost(post);
    }

    /**
     * Check if a user has liked a post
     *
     * @param post The post entity
     * @param user The user to check
     * @return true if the user has liked the post
     */
    public boolean hasUserLiked(Post post, User user) {
        if (post == null || user == null) {
            return false;
        }
        return postLikeRepository.existsByPostAndUser(post, user);
    }
}
