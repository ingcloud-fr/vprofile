<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.springframework.security.core.context.SecurityContextHolder" %>
<%@ page import="org.springframework.security.core.Authentication" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>Timeline - Facelink</title>
    <link rel="stylesheet" href="${contextPath}/resources/css/bootstrap.min.css">
    <link href="${contextPath}/resources/css/profile.css" rel="stylesheet">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css">
    <style>
        .timeline-container {
            padding-top: 20px;
        }
        .post-create-box {
            background: #fff;
            border: 1px solid #ddd;
            border-radius: 4px;
            padding: 20px;
            margin-bottom: 20px;
        }
        .post-create-box textarea {
            width: 100%;
            border: 1px solid #ccc;
            border-radius: 4px;
            padding: 10px;
            margin-bottom: 10px;
            resize: vertical;
            min-height: 80px;
        }
        .post-create-box input[type="text"] {
            width: 100%;
            border: 1px solid #ccc;
            border-radius: 4px;
            padding: 8px;
            margin-bottom: 10px;
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
        .likes-count {
            color: #666;
            font-size: 14px;
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
                                <li class="active"><a href="${contextPath}/timeline">Timeline</a></li>
                                <li><a href="${contextPath}/my-posts">Mes Messages</a></li>
                                <li><a href="${contextPath}/welcome">Mon Profil</a></li>
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
            <h2>Fil public</h2>
            <p class="text-muted">Tous les messages de la communauté</p>

            <!-- Post Creation Form -->
            <div class="post-create-box">
                <h4>Quoi de neuf ?</h4>
                <form action="${contextPath}/timeline/post" method="post">
                    <textarea name="content" placeholder="Partagez vos idées..." required maxlength="500"></textarea>
                    <input type="text" name="imageUrl" placeholder="URL de l'image (optionnel)">
                    <button type="submit" class="btn btn-primary">
                        <i class="fa fa-paper-plane" aria-hidden="true"></i> Publier
                    </button>
                    <small class="text-muted pull-right" style="padding-top: 10px;">Max 500 caractères</small>
                </form>
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
                                <span class="likes-count">
                                    <i class="fa fa-heart" aria-hidden="true" style="color: #e74c3c;"></i> ${post.likesCount} likes
                                </span>
                            </div>
                        </div>
                    </c:forEach>

                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <div class="pagination-wrapper">
                            <ul class="pagination">
                                <c:if test="${hasPrevious}">
                                    <li>
                                        <a href="${contextPath}/timeline?page=${currentPage - 1}" aria-label="Previous">
                                            <span aria-hidden="true">&laquo;</span>
                                        </a>
                                    </li>
                                </c:if>

                                <c:forEach begin="0" end="${totalPages - 1}" var="i">
                                    <li class="${i == currentPage ? 'active' : ''}">
                                        <a href="${contextPath}/timeline?page=${i}">${i + 1}</a>
                                    </li>
                                </c:forEach>

                                <c:if test="${hasNext}">
                                    <li>
                                        <a href="${contextPath}/timeline?page=${currentPage + 1}" aria-label="Next">
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
                        <i class="fa fa-comments-o fa-5x" aria-hidden="true"></i>
                        <p>Aucun message pour le moment. Soyez le premier à publier !</p>
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
