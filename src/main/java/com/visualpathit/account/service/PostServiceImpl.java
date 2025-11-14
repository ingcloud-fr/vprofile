package com.visualpathit.account.service;

import com.visualpathit.account.model.Post;
import com.visualpathit.account.model.User;
import com.visualpathit.account.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of PostService
 */
@Service
@Transactional
public class PostServiceImpl implements PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    @Autowired
    private PostRepository postRepository;

    @Override
    public List<Post> findAllPosts() {
        logger.info("Finding all posts");
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Page<Post> findAllPosts(Pageable pageable) {
        logger.info("Finding all posts with pagination: {}", pageable);
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public List<Post> findByAuthor(User author) {
        logger.info("Finding posts by author: {}", author.getUsername());
        return postRepository.findByAuthorOrderByCreatedAtDesc(author);
    }

    @Override
    public Page<Post> findByAuthor(User author, Pageable pageable) {
        logger.info("Finding posts by author: {} with pagination: {}", author.getUsername(), pageable);
        return postRepository.findByAuthorOrderByCreatedAtDesc(author, pageable);
    }

    @Override
    public Post createPost(String content, String imageUrl, User author) {
        logger.info("Creating new post by user: {}", author.getUsername());

        Post post = new Post();
        post.setContent(content);
        post.setImageUrl(imageUrl != null && !imageUrl.trim().isEmpty() ? imageUrl.trim() : null);
        post.setAuthor(author);
        post.setCreatedAt(LocalDateTime.now());
        post.setLikesCount(0);

        Post savedPost = postRepository.save(post);
        logger.info("Post created successfully with ID: {}", savedPost.getId());

        return savedPost;
    }

    @Override
    public Post save(Post post) {
        logger.info("Saving post");
        return postRepository.save(post);
    }

    @Override
    public long countByAuthor(User author) {
        return postRepository.countByAuthor(author);
    }
}
