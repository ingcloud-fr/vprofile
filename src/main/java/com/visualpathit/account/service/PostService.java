package com.visualpathit.account.service;

import com.visualpathit.account.model.Post;
import com.visualpathit.account.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing posts
 */
public interface PostService {

    /**
     * Find all posts ordered by creation date (newest first)
     */
    List<Post> findAllPosts();

    /**
     * Find all posts with pagination
     */
    Page<Post> findAllPosts(Pageable pageable);

    /**
     * Find posts by a specific author
     */
    List<Post> findByAuthor(User author);

    /**
     * Find posts by a specific author with pagination
     */
    Page<Post> findByAuthor(User author, Pageable pageable);

    /**
     * Create a new post
     */
    Post createPost(String content, String imageUrl, User author);

    /**
     * Save a post
     */
    Post save(Post post);

    /**
     * Count posts by author
     */
    long countByAuthor(User author);
}
