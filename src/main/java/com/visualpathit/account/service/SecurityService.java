package com.visualpathit.account.service;

import jakarta.servlet.http.HttpServletRequest;

/** method for finding already added user !*/
public interface SecurityService {
	/** {@inheritDoc}} !*/
    String findLoggedInUsername();

    boolean autologin(String username, String password, HttpServletRequest request);
}
