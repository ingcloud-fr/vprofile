package com.visualpathit.account.controllerTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.visualpathit.account.controller.UserController;
import com.visualpathit.account.model.User;
import com.visualpathit.account.service.PostLikeService;
import com.visualpathit.account.service.PostService;
import com.visualpathit.account.service.SecurityService;
import com.visualpathit.account.service.UserService;
import com.visualpathit.account.setup.StandaloneMvcTestViewResolver;

public class UserControllerTest {

    @Mock
    private UserService controllerSer;

    @Mock
    private SecurityService securityService;

    @Mock
    private PostService postService;

    @Mock
    private PostLikeService postLikeService;

    @InjectMocks
    private UserController controller;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // Mock securityService.findLoggedInUsername() to return null (unauthenticated user)
        when(securityService.findLoggedInUsername()).thenReturn(null);

        // Mock postService.findAllPosts() to return empty page
        Page<com.visualpathit.account.model.Post> emptyPage =
            new PageImpl<>(Collections.emptyList());
        when(postService.findAllPosts(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(new StandaloneMvcTestViewResolver()).build();
    }

    @Test
    public void registrationTestforHappyFlow() throws Exception {
        mockMvc.perform(get("/registration"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(forwardedUrl("registration"));
    }

    @Test
    public void registrationTestforNullValueHappyFlow() throws Exception {
        mockMvc.perform(get("/registration"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(forwardedUrl("registration"));
    }

    @Test
    public void loginTestHappyFlow() throws Exception {
        // GET "/" redirects to /welcome (not login page)
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));
    }

    @Test
    public void welcomeTestHappyFlow() throws Exception {
        mockMvc.perform(get("/welcome"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(forwardedUrl("welcome"));
    }

    @Test
    public void welcomeAfterDirectLoginTestHappyFlow() throws Exception {
        // GET "/" redirects to /welcome (not login page)
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));
    }

    @Test
    public void indexTestHappyFlow() throws Exception {
        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index_home"))
                .andExpect(forwardedUrl("index_home"));
    }
}

