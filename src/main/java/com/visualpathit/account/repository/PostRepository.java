package com.visualpathit.account.repository;

import com.visualpathit.account.model.Post;
import com.visualpathit.account.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing Post entities
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Find all posts ordered by creation date (newest first)
     */
    List<Post> findAllByOrderByCreatedAtDesc();

    /**
     * Find all posts ordered by creation date with pagination
     */
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find posts by a specific author ordered by creation date (newest first)
     */
    List<Post> findByAuthorOrderByCreatedAtDesc(User author);

    /**
     * Find posts by a specific author with pagination
     */
    Page<Post> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);

    /**
     * Count posts by a specific author
     */
    long countByAuthor(User author);
}
