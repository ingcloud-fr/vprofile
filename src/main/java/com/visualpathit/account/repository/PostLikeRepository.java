package com.visualpathit.account.repository;

import com.visualpathit.account.model.Post;
import com.visualpathit.account.model.PostLike;
import com.visualpathit.account.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing PostLike entities
 */
@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * Find a specific like by post and user
     */
    Optional<PostLike> findByPostAndUser(Post post, User user);

    /**
     * Check if a user has liked a post
     */
    boolean existsByPostAndUser(Post post, User user);

    /**
     * Count the number of likes for a post
     */
    long countByPost(Post post);

    /**
     * Delete a like by post and user
     */
    void deleteByPostAndUser(Post post, User user);

    /**
     * Delete all likes for a specific post
     */
    void deleteByPost(Post post);
}
