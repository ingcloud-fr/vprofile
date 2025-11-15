<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.springframework.security.core.context.SecurityContextHolder" %>
<%@ page import="org.springframework.security.core.Authentication" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>Mes Messages - Facelink</title>
    <link rel="stylesheet" href="${contextPath}/resources/css/bootstrap.min.css">
    <link href="${contextPath}/resources/css/profile.css" rel="stylesheet">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css">
    <style>
        .timeline-container {
            padding-top: 20px;
        }
        .post-card {
            background: #fff;
            border: 1px solid #ddd;
            border-radius: 4px;
            padding: 20px;
            margin-bottom: 20px;
        }
        .post-header {
            display: flex;
            align-items: center;
            margin-bottom: 15px;
        }
        .post-header .avatar {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            margin-right: 10px;
        }
        .post-info strong {
            display: block;
            color: #333;
        }
        .post-info small {
            color: #999;
        }
        .post-content {
            margin-bottom: 15px;
            word-wrap: break-word;
        }
        .post-content p {
            margin-bottom: 10px;
        }
        .post-image {
            max-width: 100%;
            height: auto;
            border-radius: 4px;
            margin-top: 10px;
        }
        .post-actions {
            border-top: 1px solid #eee;
            padding-top: 10px;
        }
        .btn-like {
            background: #fff;
            border: 1px solid #ddd;
            padding: 6px 16px;
            border-radius: 20px;
            cursor: pointer;
            transition: all 0.3s;
            font-size: 14px;
            color: #666;
        }
        .btn-like:hover {
            background: #f8f9fa;
            border-color: #e74c3c;
            color: #e74c3c;
        }
        .btn-like.liked {
            background: #ffe6e6;
            border-color: #e74c3c;
            color: #e74c3c;
        }
        .btn-like.liked:hover {
            background: #ffcccc;
        }
        .btn-like i {
            margin-right: 4px;
        }
        .pagination-wrapper {
            text-align: center;
            margin-top: 30px;
        }
        .no-posts {
            text-align: center;
            padding: 40px;
            color: #999;
        }
        .stats-box {
            background: #f9f9f9;
            border: 1px solid #ddd;
            border-radius: 4px;
            padding: 15px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>

<div class="mainbody container-fluid">
    <div class="row">
        <div class="navbar-wrapper">
            <div class="container-fluid">
                <nav class="navbar navbar-custom navbar-static-top" role="navigation">
                    <div class="container-fluid">
                        <div class="navbar-header">
                            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                                <span class="sr-only">Toggle navigation</span>
                                <span class="icon-bar"></span>
                                <span class="icon-bar"></span>
                                <span class="icon-bar"></span>
                            </button>
                            <a class="navbar-brand" href="#" style="margin-right: -8px; margin-top: -5px;">
                                <img alt="Brand" src="${contextPath}/resources/Images/user/logo.png" width="30px" height="30px">
                            </a>
                            <a class="navbar-brand" href="#">Facelink</a>
                        </div>
                        <div class="navbar-collapse collapse">
                            <ul class="nav navbar-nav">
                                <li><a href="${contextPath}/welcome">Mon Profil</a></li>
                                <li class="active"><a href="${contextPath}/my-posts">Mes Messages</a></li>
                               <li>
                                    <a href="#"><i class="fa fa-bell-o fa-lg" aria-hidden="true"></i></a>
                                </li>
                                <li><a href="#"><i class="fa fa-envelope-o fa-lg" aria-hidden="true"></i></a></li>
                            </ul>
                            <ul class="nav navbar-nav navbar-right">
                                <li class="dropdown">
                                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                        <span class="user-avatar pull-left" style="margin-right: 8px; margin-top: -5px;">
                                            <c:choose>
                                                <c:when test="${not empty currentUser.profileImg}">
                                                    <img src="${contextPath}${currentUser.profileImg}"
                                                         class="img-responsive img-circle"
                                                         alt="${currentUser.username}"
                                                         width="30px" height="30px"
                                                         onerror="this.src='${contextPath}/resources/Images/default-avatar.png'">
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="${contextPath}/resources/Images/default-avatar.png"
                                                         class="img-responsive img-circle"
                                                         alt="User"
                                                         width="30px" height="30px">
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
                                        <span class="user-name">${pageContext.request.userPrincipal.name}</span>
                                        <b class="caret"></b>
                                    </a>
                                    <ul class="dropdown-menu">
                                        <li>
                                            <div class="navbar-content">
                                                <div class="row">
                                                    <div class="col-md-5">
                                                        <c:choose>
                                                            <c:when test="${not empty currentUser.profileImg}">
                                                                <img src="${contextPath}${currentUser.profileImg}"
                                                                     alt="Profile"
                                                                     class="img-responsive"
                                                                     width="120px" height="120px"
                                                                     onerror="this.src='${contextPath}/resources/Images/default-avatar.png'" />
                                                            </c:when>
                                                            <c:otherwise>
                                                                <img src="${contextPath}/resources/Images/default-avatar.png"
                                                                     alt="Profile"
                                                                     class="img-responsive"
                                                                     width="120px" height="120px" />
                                                            </c:otherwise>
                                                        </c:choose>
                                                        <p class="text-center small">
                                                            <a href="${contextPath}/upload">Change Photo</a>
                                                        </p>
                                                    </div>
                                                    <div class="col-md-7">
                                                        <span>${pageContext.request.userPrincipal.name}</span>
                                                        <p class="text-muted small">${currentUser.userEmail}</p>
                                                        <div class="divider"></div>
                                                        <a href="${contextPath}/user/${pageContext.request.userPrincipal.name}" class="btn btn-default btn-xs">
                                                            <i class="fa fa-user-o" aria-hidden="true"></i> Update Profile
                                                        </a>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="navbar-footer">
                                                <div class="navbar-footer-content">
                                                    <div class="row"></div>
                                                </div>
                                            </div>
                                            <form id="logoutForm" method="POST" action="${contextPath}/logout">
                                                <button type="submit" class="btn btn-default btn-sm pull-right">
                                                    <i class="fa fa-power-off" aria-hidden="true"></i> Sign Out
                                                </button>
                                            </form>
                                        </li>
                                    </ul>
                                </li>
                            </ul>
                        </div>
                    </div>
                </nav>
            </div>
        </div>
    </div>
</div>

<div class="container timeline-container">
    <div class="row">
        <div class="col-lg-8 col-lg-offset-2 col-md-10 col-md-offset-1">
            <h2>Mes Messages</h2>
            <p class="text-muted">Vos publications personnelles</p>

            <!-- Stats Box -->
            <div class="stats-box">
                <strong>Total de vos publications:</strong> ${totalPosts}
                <span class="pull-right">
                    <a href="${contextPath}/welcome" class="btn btn-primary btn-sm">
                        <i class="fa fa-globe" aria-hidden="true"></i> Retour au fil public
                    </a>
                </span>
            </div>

            <!-- Posts Feed -->
            <c:choose>
                <c:when test="${not empty posts}">
                    <c:forEach items="${posts}" var="post">
                        <div class="post-card">
                            <div class="post-header">
                                <c:choose>
                                    <c:when test="${not empty post.author.profileImg}">
                                        <img src="${contextPath}${post.author.profileImg}"
                                             class="avatar"
                                             alt="${post.author.username}"
                                             onerror="this.src='${contextPath}/resources/Images/default-avatar.png'">
                                    </c:when>
                                    <c:otherwise>
                                        <img src="${contextPath}/resources/Images/default-avatar.png"
                                             class="avatar"
                                             alt="User">
                                    </c:otherwise>
                                </c:choose>
                                <div class="post-info">
                                    <strong>${post.author.username}</strong>
                                    <small><i class="fa fa-clock-o" aria-hidden="true"></i> ${post.timeAgo}</small>
                                </div>
                            </div>
                            <div class="post-content">
                                <p style="white-space: pre-wrap;">${post.content}</p>
                                <c:if test="${not empty post.imageUrl}">
                                    <img src="${post.imageUrl}" class="post-image" alt="Post image">
                                </c:if>
                            </div>
                            <div class="post-actions">
                                <form action="${contextPath}/post/${post.id}/like" method="post" style="display:inline;">
                                    <c:choose>
                                        <c:when test="${post.likedByCurrentUser}">
                                            <button type="submit" class="btn-like liked">
                                                <i class="fa fa-heart" aria-hidden="true"></i> ${post.likesCount}
                                                <c:choose>
                                                    <c:when test="${post.likesCount <= 1}">like</c:when>
                                                    <c:otherwise>likes</c:otherwise>
                                                </c:choose>
                                            </button>
                                        </c:when>
                                        <c:otherwise>
                                            <button type="submit" class="btn-like">
                                                <i class="fa fa-heart-o" aria-hidden="true"></i> ${post.likesCount}
                                                <c:choose>
                                                    <c:when test="${post.likesCount <= 1}">like</c:when>
                                                    <c:otherwise>likes</c:otherwise>
                                                </c:choose>
                                            </button>
                                        </c:otherwise>
                                    </c:choose>
                                </form>
                            </div>
                        </div>
                    </c:forEach>

                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <div class="pagination-wrapper">
                            <ul class="pagination">
                                <c:if test="${hasPrevious}">
                                    <li>
                                        <a href="${contextPath}/my-posts?page=${currentPage - 1}" aria-label="Previous">
                                            <span aria-hidden="true">&laquo;</span>
                                        </a>
                                    </li>
                                </c:if>

                                <c:forEach begin="0" end="${totalPages - 1}" var="i">
                                    <li class="${i == currentPage ? 'active' : ''}">
                                        <a href="${contextPath}/my-posts?page=${i}">${i + 1}</a>
                                    </li>
                                </c:forEach>

                                <c:if test="${hasNext}">
                                    <li>
                                        <a href="${contextPath}/my-posts?page=${currentPage + 1}" aria-label="Next">
                                            <span aria-hidden="true">&raquo;</span>
                                        </a>
                                    </li>
                                </c:if>
                            </ul>
                        </div>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <div class="no-posts">
                        <i class="fa fa-edit fa-5x" aria-hidden="true"></i>
                        <p>Vous n'avez pas encore publié de messages.</p>
                        <p>
                            <a href="${contextPath}/welcome" class="btn btn-success">
                                <i class="fa fa-plus" aria-hidden="true"></i> Créer votre premier post
                            </a>
                        </p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<script src="${contextPath}/resources/js/bootstrap.min.js"></script>
</body>
</html>
