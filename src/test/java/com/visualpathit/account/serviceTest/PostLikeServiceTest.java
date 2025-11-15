package com.visualpathit.account.serviceTest;

import com.visualpathit.account.model.Post;
import com.visualpathit.account.model.PostLike;
import com.visualpathit.account.model.User;
import com.visualpathit.account.repository.PostLikeRepository;
import com.visualpathit.account.repository.PostRepository;
import com.visualpathit.account.service.PostLikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PostLikeService
 * Tests like/unlike functionality
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PostLikeService Unit Tests")
class PostLikeServiceTest {

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostLikeService postLikeService;

    private User testUser;
    private Post testPost;
    private PostLike testLike;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setUserEmail("test@example.com");

        // Setup test post
        testPost = new Post();
        testPost.setId(1L);
        testPost.setContent("Test post");
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setLikesCount(0);

        // Setup test like
        testLike = new PostLike(testPost, testUser);
    }

    // ========== TOGGLE LIKE TESTS ==========

    @Test
    @DisplayName("Should like post when user has not liked it")
    void testToggleLike_CreateLike() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.existsByPostAndUser(testPost, testUser)).thenReturn(false);
        when(postLikeRepository.save(any(PostLike.class))).thenReturn(testLike);
        when(postLikeRepository.countByPost(testPost)).thenReturn(1L);

        // When
        boolean result = postLikeService.toggleLike(1L, testUser);

        // Then
        assertTrue(result); // Returns true for "liked"
        verify(postLikeRepository).save(any(PostLike.class));
        verify(postLikeRepository).countByPost(testPost);
        verify(postRepository).save(testPost);
        assertEquals(1, testPost.getLikesCount());
    }

    @Test
    @DisplayName("Should unlike post when user has already liked it")
    void testToggleLike_RemoveLike() {
        // Given
        testPost.setLikesCount(1);
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.existsByPostAndUser(testPost, testUser)).thenReturn(true);
        when(postLikeRepository.countByPost(testPost)).thenReturn(0L);

        // When
        boolean result = postLikeService.toggleLike(1L, testUser);

        // Then
        assertFalse(result); // Returns false for "unliked"
        verify(postLikeRepository).deleteByPostAndUser(testPost, testUser);
        verify(postLikeRepository).countByPost(testPost);
        verify(postRepository).save(testPost);
        assertEquals(0, testPost.getLikesCount());
    }

    @Test
    @DisplayName("Should throw exception when post not found")
    void testToggleLike_PostNotFound() {
        // Given
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            postLikeService.toggleLike(999L, testUser);
        });

        verify(postLikeRepository, never()).save(any());
        verify(postLikeRepository, never()).deleteByPostAndUser(any(), any());
    }

    @Test
    @DisplayName("Should update likes count correctly when liking")
    void testToggleLike_UpdateLikesCountOnLike() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.existsByPostAndUser(testPost, testUser)).thenReturn(false);
        when(postLikeRepository.countByPost(testPost)).thenReturn(5L); // Post has 5 likes

        // When
        postLikeService.toggleLike(1L, testUser);

        // Then
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertEquals(5, postCaptor.getValue().getLikesCount());
    }

    @Test
    @DisplayName("Should update likes count correctly when unliking")
    void testToggleLike_UpdateLikesCountOnUnlike() {
        // Given
        testPost.setLikesCount(5);
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.existsByPostAndUser(testPost, testUser)).thenReturn(true);
        when(postLikeRepository.countByPost(testPost)).thenReturn(4L); // 4 likes after unlike

        // When
        postLikeService.toggleLike(1L, testUser);

        // Then
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertEquals(4, postCaptor.getValue().getLikesCount());
    }

    // ========== HAS USER LIKED TESTS ==========

    @Test
    @DisplayName("Should return true when user has liked post (by ID)")
    void testHasUserLiked_ById_True() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.existsByPostAndUser(testPost, testUser)).thenReturn(true);

        // When
        boolean result = postLikeService.hasUserLiked(1L, testUser);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when user has not liked post (by ID)")
    void testHasUserLiked_ById_False() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.existsByPostAndUser(testPost, testUser)).thenReturn(false);

        // When
        boolean result = postLikeService.hasUserLiked(1L, testUser);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when post not found (by ID)")
    void testHasUserLiked_ById_PostNotFound() {
        // Given
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        boolean result = postLikeService.hasUserLiked(999L, testUser);

        // Then
        assertFalse(result);
        verify(postLikeRepository, never()).existsByPostAndUser(any(), any());
    }

    @Test
    @DisplayName("Should return true when user has liked post (by entity)")
    void testHasUserLiked_ByEntity_True() {
        // Given
        when(postLikeRepository.existsByPostAndUser(testPost, testUser)).thenReturn(true);

        // When
        boolean result = postLikeService.hasUserLiked(testPost, testUser);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when user has not liked post (by entity)")
    void testHasUserLiked_ByEntity_False() {
        // Given
        when(postLikeRepository.existsByPostAndUser(testPost, testUser)).thenReturn(false);

        // When
        boolean result = postLikeService.hasUserLiked(testPost, testUser);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when post is null (by entity)")
    void testHasUserLiked_ByEntity_NullPost() {
        // When
        boolean result = postLikeService.hasUserLiked((Post) null, testUser);

        // Then
        assertFalse(result);
        verify(postLikeRepository, never()).existsByPostAndUser(any(), any());
    }

    @Test
    @DisplayName("Should return false when user is null (by entity)")
    void testHasUserLiked_ByEntity_NullUser() {
        // When
        boolean result = postLikeService.hasUserLiked(testPost, null);

        // Then
        assertFalse(result);
        verify(postLikeRepository, never()).existsByPostAndUser(any(), any());
    }

    // ========== GET LIKES COUNT TESTS ==========

    @Test
    @DisplayName("Should get likes count by post ID")
    void testGetLikesCount_ById() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.countByPost(testPost)).thenReturn(42L);

        // When
        long result = postLikeService.getLikesCount(1L);

        // Then
        assertEquals(42, result);
    }

    @Test
    @DisplayName("Should return zero when post not found by ID")
    void testGetLikesCount_ById_PostNotFound() {
        // Given
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        long result = postLikeService.getLikesCount(999L);

        // Then
        assertEquals(0, result);
        verify(postLikeRepository, never()).countByPost(any());
    }

    @Test
    @DisplayName("Should get likes count by post entity")
    void testGetLikesCount_ByEntity() {
        // Given
        when(postLikeRepository.countByPost(testPost)).thenReturn(15L);

        // When
        long result = postLikeService.getLikesCount(testPost);

        // Then
        assertEquals(15, result);
    }

    @Test
    @DisplayName("Should return zero when post entity is null")
    void testGetLikesCount_ByEntity_NullPost() {
        // When
        long result = postLikeService.getLikesCount((Post) null);

        // Then
        assertEquals(0, result);
        verify(postLikeRepository, never()).countByPost(any());
    }

    @Test
    @DisplayName("Should handle zero likes correctly")
    void testGetLikesCount_ZeroLikes() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.countByPost(testPost)).thenReturn(0L);

        // When
        long result = postLikeService.getLikesCount(1L);

        // Then
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should handle multiple likes from different users")
    void testToggleLike_MultipleLikes() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.existsByPostAndUser(testPost, testUser)).thenReturn(false);
        when(postLikeRepository.existsByPostAndUser(testPost, user2)).thenReturn(false);
        when(postLikeRepository.countByPost(testPost)).thenReturn(1L).thenReturn(2L);

        // When
        postLikeService.toggleLike(1L, testUser);
        postLikeService.toggleLike(1L, user2);

        // Then
        verify(postLikeRepository, times(2)).save(any(PostLike.class));
        verify(postRepository, times(2)).save(testPost);
    }
}
