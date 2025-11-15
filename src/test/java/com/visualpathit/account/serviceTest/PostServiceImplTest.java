package com.visualpathit.account.serviceTest;

import com.visualpathit.account.model.Post;
import com.visualpathit.account.model.User;
import com.visualpathit.account.repository.PostRepository;
import com.visualpathit.account.service.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PostServiceImpl
 * Tests post management business logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PostService Unit Tests")
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostServiceImpl postService;

    private User testAuthor;
    private Post testPost;

    @BeforeEach
    void setUp() {
        // Setup test author
        testAuthor = new User();
        testAuthor.setId(1L);
        testAuthor.setUsername("testauthor");
        testAuthor.setEmail("author@example.com");

        // Setup test post
        testPost = new Post();
        testPost.setId(1L);
        testPost.setContent("Test post content");
        testPost.setAuthor(testAuthor);
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setLikesCount(0);
    }

    @Test
    @DisplayName("Should find all posts ordered by creation date descending")
    void testFindAllPosts_Success() {
        // Given
        Post post1 = new Post();
        post1.setId(1L);
        post1.setCreatedAt(LocalDateTime.now().minusDays(1));

        Post post2 = new Post();
        post2.setId(2L);
        post2.setCreatedAt(LocalDateTime.now());

        List<Post> posts = Arrays.asList(post2, post1); // Most recent first
        when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(posts);

        // When
        List<Post> result = postService.findAllPosts();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(post2.getId(), result.get(0).getId()); // Most recent first
        verify(postRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Should find all posts with pagination")
    void testFindAllPostsWithPagination_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Post> posts = Arrays.asList(testPost);
        Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

        when(postRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(postPage);

        // When
        Page<Post> result = postService.findAllPosts(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(postRepository).findAllByOrderByCreatedAtDesc(pageable);
    }

    @Test
    @DisplayName("Should find posts by author")
    void testFindByAuthor_Success() {
        // Given
        List<Post> posts = Arrays.asList(testPost);
        when(postRepository.findByAuthorOrderByCreatedAtDesc(testAuthor)).thenReturn(posts);

        // When
        List<Post> result = postService.findByAuthor(testAuthor);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAuthor.getId(), result.get(0).getAuthor().getId());
        verify(postRepository).findByAuthorOrderByCreatedAtDesc(testAuthor);
    }

    @Test
    @DisplayName("Should find posts by author with pagination")
    void testFindByAuthorWithPagination_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Post> posts = Arrays.asList(testPost);
        Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

        when(postRepository.findByAuthorOrderByCreatedAtDesc(testAuthor, pageable)).thenReturn(postPage);

        // When
        Page<Post> result = postService.findByAuthor(testAuthor, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testAuthor.getId(), result.getContent().get(0).getAuthor().getId());
        verify(postRepository).findByAuthorOrderByCreatedAtDesc(testAuthor, pageable);
    }

    @Test
    @DisplayName("Should create post with content only")
    void testCreatePost_ContentOnly() {
        // Given
        String content = "This is a test post";
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // When
        Post result = postService.createPost(content, null, testAuthor);

        // Then
        assertNotNull(result);
        verify(postRepository).save(any(Post.class));

        // Verify post was created with correct properties
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();

        assertEquals(content, savedPost.getContent());
        assertNull(savedPost.getImageUrl());
        assertEquals(testAuthor, savedPost.getAuthor());
        assertEquals(0, savedPost.getLikesCount());
        assertNotNull(savedPost.getCreatedAt());
    }

    @Test
    @DisplayName("Should create post with content and image URL")
    void testCreatePost_WithImage() {
        // Given
        String content = "Post with image";
        String imageUrl = "https://example.com/image.jpg";
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // When
        Post result = postService.createPost(content, imageUrl, testAuthor);

        // Then
        assertNotNull(result);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();

        assertEquals(content, savedPost.getContent());
        assertEquals(imageUrl, savedPost.getImageUrl());
    }

    @Test
    @DisplayName("Should trim and set null for empty image URL")
    void testCreatePost_EmptyImageUrl() {
        // Given
        String content = "Post with empty image URL";
        String imageUrl = "   "; // Whitespace only
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // When
        postService.createPost(content, imageUrl, testAuthor);

        // Then
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();

        assertNull(savedPost.getImageUrl()); // Should be null, not whitespace
    }

    @Test
    @DisplayName("Should trim whitespace from image URL")
    void testCreatePost_TrimImageUrl() {
        // Given
        String content = "Post with trimmed image";
        String imageUrl = "  https://example.com/image.jpg  ";
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // When
        postService.createPost(content, imageUrl, testAuthor);

        // Then
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();

        assertEquals("https://example.com/image.jpg", savedPost.getImageUrl()); // Trimmed
    }

    @Test
    @DisplayName("Should initialize likes count to zero on post creation")
    void testCreatePost_InitialLikesCount() {
        // Given
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // When
        postService.createPost("Test content", null, testAuthor);

        // Then
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();

        assertEquals(0, savedPost.getLikesCount());
    }

    @Test
    @DisplayName("Should set creation timestamp on post creation")
    void testCreatePost_CreatedAtTimestamp() {
        // Given
        LocalDateTime beforeCreation = LocalDateTime.now();
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // When
        postService.createPost("Test content", null, testAuthor);

        // Then
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();

        assertNotNull(savedPost.getCreatedAt());
        assertTrue(savedPost.getCreatedAt().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(savedPost.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Should save existing post")
    void testSave_Success() {
        // Given
        testPost.setLikesCount(5);
        when(postRepository.save(testPost)).thenReturn(testPost);

        // When
        Post result = postService.save(testPost);

        // Then
        assertNotNull(result);
        assertEquals(5, result.getLikesCount());
        verify(postRepository).save(testPost);
    }

    @Test
    @DisplayName("Should count posts by author")
    void testCountByAuthor_Success() {
        // Given
        long expectedCount = 42L;
        when(postRepository.countByAuthor(testAuthor)).thenReturn(expectedCount);

        // When
        long result = postService.countByAuthor(testAuthor);

        // Then
        assertEquals(expectedCount, result);
        verify(postRepository).countByAuthor(testAuthor);
    }

    @Test
    @DisplayName("Should return zero when author has no posts")
    void testCountByAuthor_NoPostsFound() {
        // Given
        when(postRepository.countByAuthor(testAuthor)).thenReturn(0L);

        // When
        long result = postService.countByAuthor(testAuthor);

        // Then
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should return empty list when no posts exist")
    void testFindAllPosts_EmptyList() {
        // Given
        when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Arrays.asList());

        // When
        List<Post> result = postService.findAllPosts();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when author has no posts")
    void testFindByAuthor_EmptyList() {
        // Given
        when(postRepository.findByAuthorOrderByCreatedAtDesc(testAuthor)).thenReturn(Arrays.asList());

        // When
        List<Post> result = postService.findByAuthor(testAuthor);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
