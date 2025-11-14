package com.visualpathit.account.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a post on the timeline/wall
 */
@Entity
@Table(name = "posts")
public class Post implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "likes_count")
    private int likesCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PostLike> likes = new HashSet<>();

    // Transient field to track if current user has liked this post
    @Transient
    private boolean likedByCurrentUser = false;

    // Constructors
    public Post() {
        this.createdAt = LocalDateTime.now();
    }

    public Post(String content, User author) {
        this.content = content;
        this.author = author;
        this.createdAt = LocalDateTime.now();
        this.likesCount = 0;
    }

    public Post(String content, String imageUrl, User author) {
        this.content = content;
        this.imageUrl = imageUrl;
        this.author = author;
        this.createdAt = LocalDateTime.now();
        this.likesCount = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getLikesCount() {
        // Use the size of the likes collection if available, otherwise return the stored count
        return (likes != null && !likes.isEmpty()) ? likes.size() : likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public Set<PostLike> getLikes() {
        return likes;
    }

    public void setLikes(Set<PostLike> likes) {
        this.likes = likes;
    }

    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }

    /**
     * Helper method to check if a specific user has liked this post
     */
    public boolean isLikedBy(User user) {
        if (likes == null || user == null) {
            return false;
        }
        return likes.stream()
                .anyMatch(like -> like.getUser().getId().equals(user.getId()));
    }

    /**
     * Add a like to this post
     */
    public void addLike(PostLike like) {
        likes.add(like);
        like.setPost(this);
    }

    /**
     * Remove a like from this post
     */
    public void removeLike(PostLike like) {
        likes.remove(like);
        like.setPost(null);
    }

    /**
     * Get a human-readable "time ago" string (e.g., "Il y a 5 minutes")
     */
    public String getTimeAgo() {
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(createdAt, now);
        long hours = ChronoUnit.HOURS.between(createdAt, now);
        long days = ChronoUnit.DAYS.between(createdAt, now);

        if (minutes < 1) {
            return "À l'instant";
        } else if (minutes < 60) {
            return "Il y a " + minutes + (minutes == 1 ? " minute" : " minutes");
        } else if (hours < 24) {
            return "Il y a " + hours + (hours == 1 ? " heure" : " heures");
        } else if (days < 30) {
            return "Il y a " + days + (days == 1 ? " jour" : " jours");
        } else {
            return "Il y a plus d'un mois";
        }
    }
}
