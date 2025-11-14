<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.springframework.security.core.context.SecurityContextHolder" %>
<%@ page import="org.springframework.security.core.Authentication" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>Mon Profil - vProfile</title>
    <link rel="stylesheet" href="${contextPath}/resources/css/bootstrap.min.css">
    <link href="${contextPath}/resources/css/profile.css" rel="stylesheet">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css">
    <style>
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
                            <a class="navbar-brand" href="#">HKH Infotech</a>
                        </div>
                        <div class="navbar-collapse collapse">
                            <ul class="nav navbar-nav">
                                <li class="active"><a href="${contextPath}/welcome">Mon Profil</a></li>
                                <li><a href="${contextPath}/my-posts">Mes Messages</a></li>
                               <li>
                                    <a href="#"><i class="fa fa-bell-o fa-lg" aria-hidden="true"></i></a>
                                </li>
                                <li><a href="#"><i class="fa fa-envelope-o fa-lg" aria-hidden="true"></i></a></li>
                            </ul>
                            <ul class="nav navbar-nav navbar-right">
                                <li class="dropdown">
                                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                        <span class="user-avatar pull-left" style="margin-right: 8px; margin-top: -5px;">
                                            <img src="${contextPath}/resources/Images/user/user.png" class="img-responsive img-circle" title="${pageContext.request.userPrincipal.name}" alt="${pageContext.request.userPrincipal.name}" width="30px" height="30px">
                                        </span>
                                        <span class="user-name">${pageContext.request.userPrincipal.name}</span>
                                        <b class="caret"></b>
                                    </a>
                                    <ul class="dropdown-menu">
                                        <li>
                                            <div class="navbar-content">
                                                <div class="row">
                                                    <div class="col-md-5">
                                                        <img src="${contextPath}/resources/Images/user/user.png" alt="Alternate Text" class="img-responsive" width="120px" height="120px" />
                                                        <p class="text-center small">
                                                            <a href="${contextPath}/upload">Change Photo</a>
                                                        </p>
                                                    </div>
                                                    <div class="col-md-7">
                                                        <span>${pageContext.request.userPrincipal.name}</span> <br/>
                                                        <p class="text-muted small">
                                                            ${currentUser.userEmail}
                                                        </p>
                                                        <div class="divider"></div>
                                                        <a href="${contextPath}/user/${pageContext.request.userPrincipal.name}" class="btn btn-default btn-xs"><i class="fa fa-user-o" aria-hidden="true"></i> Update Profile </a>
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

<div style="padding-top:50px;">.</div>

<!-- Messages de feedback -->
<div class="container-fluid">
    <c:if test="${param.success == 'photoUploaded'}">
        <div class="alert alert-success alert-dismissible" role="alert">
            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
            <i class="fa fa-check-circle"></i> Photo de profil mise à jour avec succès !
        </div>
    </c:if>

    <c:if test="${param.error == 'emptyFile'}">
        <div class="alert alert-danger alert-dismissible" role="alert">
            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
            <i class="fa fa-exclamation-triangle"></i> Aucun fichier sélectionné
        </div>
    </c:if>

    <c:if test="${param.error == 'invalidType'}">
        <div class="alert alert-danger alert-dismissible" role="alert">
            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
            <i class="fa fa-exclamation-triangle"></i> Type de fichier non supporté (images uniquement)
        </div>
    </c:if>

    <c:if test="${param.error == 'fileTooLarge'}">
        <div class="alert alert-danger alert-dismissible" role="alert">
            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
            <i class="fa fa-exclamation-triangle"></i> Fichier trop volumineux (maximum 5 MB)
        </div>
    </c:if>

    <c:if test="${param.error == 'uploadFailed'}">
        <div class="alert alert-danger alert-dismissible" role="alert">
            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
            <i class="fa fa-exclamation-triangle"></i> Échec de l'upload. Veuillez réessayer.
        </div>
    </c:if>
</div>

<!-- Layout 2 colonnes : Profil à gauche, Timeline à droite -->
<div class="row">
    <!-- COLONNE GAUCHE : Profil utilisateur -->
    <div class="col-lg-3 col-md-3 hidden-sm hidden-xs">
        <div class="panel panel-default">
            <div class="panel-body">
                <div class="media">
                    <div align="center">
                        <c:choose>
                            <c:when test="${not empty currentUser.profileImg}">
                                <img class="thumbnail img-responsive" src="${contextPath}${currentUser.profileImg}" width="300px" height="300px" alt="Photo de profil">
                            </c:when>
                            <c:otherwise>
                                <img class="thumbnail img-responsive" src="${contextPath}/resources/Images/user/user.png" width="300px" height="300px" alt="Avatar par défaut">
                            </c:otherwise>
                        </c:choose>
                        <div style="margin-top: 15px;">
                            <a href="#" class="btn btn-primary btn-sm" data-toggle="modal" data-target="#uploadPhotoModal">
                                <i class="fa fa-camera"></i> Changer la photo
                            </a>
                        </div>
                    </div>
                    <div class="media-body">
                        <hr>
                        <h3><strong>Email</strong></h3>
                        <p>
                            <c:choose>
                                <c:when test="${not empty currentUser.userEmail}">
                                    <i class="fa fa-envelope"></i> ${currentUser.userEmail}
                                </c:when>
                                <c:otherwise>
                                    <em>Not specified</em>
                                </c:otherwise>
                            </c:choose>
                        </p>
                        <hr>
                        <h3><strong>Location</strong></h3>
                        <p>
                            <c:choose>
                                <c:when test="${not empty currentUser.permanentAddress}">
                                    <i class="fa fa-map-marker"></i> ${currentUser.permanentAddress}
                                </c:when>
                                <c:otherwise>
                                    <em>Not specified</em>
                                </c:otherwise>
                            </c:choose>
                        </p>
                        <hr>
                        <h3><strong>Bio</strong></h3>
                        <p style="white-space: pre-wrap;">
                            <c:choose>
                                <c:when test="${not empty currentUser.skills}">
                                    ${currentUser.skills}
                                </c:when>
                                <c:otherwise>
                                    <em>No bio information yet. Update your profile to add one!</em>
                                </c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- COLONNE DROITE : Timeline publique -->
    <div class="col-lg-9 col-md-9 col-sm-12 col-xs-12">
        <div class="panel panel-default">
            <div class="panel-body">
                <h2>Fil public</h2>
                <p class="text-muted">Tous les messages de la communauté</p>
            </div>
        </div>

        <!-- Formulaire de création de post -->
        <div class="post-create-box">
            <h4>Quoi de neuf ?</h4>
            <form action="${contextPath}/welcome/post" method="post">
                <textarea name="content" placeholder="Partagez vos idées..." required maxlength="500"></textarea>
                <input type="text" name="imageUrl" placeholder="URL de l'image (optionnel)">
                <button type="submit" class="btn btn-primary">
                    <i class="fa fa-paper-plane" aria-hidden="true"></i> Publier
                </button>
                <small class="text-muted pull-right" style="padding-top: 10px;">Max 500 caractères</small>
            </form>
        </div>

        <!-- Timeline publique : Affichage de TOUS les posts -->
        <c:choose>
            <c:when test="${not empty posts}">
                <c:forEach items="${posts}" var="post">
                    <div class="post-card">
                        <div class="post-header">
                            <c:choose>
                                <c:when test="${not empty post.author.profileImg}">
                                    <img src="${post.author.profileImg}" class="avatar" alt="${post.author.username}">
                                </c:when>
                                <c:otherwise>
                                    <img src="${contextPath}/resources/Images/user/user.png" class="avatar" alt="User">
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
                    <div style="text-align: center; margin-top: 30px;">
                        <ul class="pagination">
                            <c:if test="${hasPrevious}">
                                <li>
                                    <a href="${contextPath}/welcome?page=${currentPage - 1}" aria-label="Previous">
                                        <span aria-hidden="true">&laquo;</span>
                                    </a>
                                </li>
                            </c:if>

                            <c:forEach begin="0" end="${totalPages - 1}" var="i">
                                <li class="${i == currentPage ? 'active' : ''}">
                                    <a href="${contextPath}/welcome?page=${i}">${i + 1}</a>
                                </li>
                            </c:forEach>

                            <c:if test="${hasNext}">
                                <li>
                                    <a href="${contextPath}/welcome?page=${currentPage + 1}" aria-label="Next">
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

<!-- Modal d'upload de photo de profil -->
<div class="modal fade" id="uploadPhotoModal" tabindex="-1" role="dialog" aria-labelledby="uploadPhotoModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="uploadPhotoModalLabel">
                    <i class="fa fa-camera"></i> Changer la photo de profil
                </h4>
            </div>
            <form action="${contextPath}/profile/upload-photo" method="post" enctype="multipart/form-data" id="photoUploadForm">
                <div class="modal-body">
                    <div class="form-group">
                        <label for="photoInput">Sélectionnez une image :</label>
                        <input type="file" name="photo" id="photoInput" class="form-control" accept="image/*" required>
                        <p class="help-block">
                            <i class="fa fa-info-circle"></i> Formats acceptés : JPG, PNG, GIF. Taille maximum : 5 MB
                        </p>
                    </div>
                    <div id="imagePreview" style="display: none; text-align: center; margin-top: 15px;">
                        <img id="previewImg" src="" alt="Preview" style="max-width: 100%; max-height: 300px; border-radius: 4px;">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <i class="fa fa-times"></i> Annuler
                    </button>
                    <button type="submit" class="btn btn-primary" id="uploadBtn">
                        <i class="fa fa-upload"></i> Uploader
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript">
$(function () {
    $('[data-toggle="tooltip"]').tooltip()
})
$(function () {
    $('[data-toggle="popover"]').popover()
})

// Preview de l'image avant upload
document.getElementById('photoInput').addEventListener('change', function(e) {
    var file = e.target.files[0];
    if (file) {
        // Vérifier la taille
        if (file.size > 5 * 1024 * 1024) {
            alert('Le fichier est trop volumineux (maximum 5 MB)');
            this.value = '';
            document.getElementById('imagePreview').style.display = 'none';
            return;
        }

        // Vérifier le type
        if (!file.type.startsWith('image/')) {
            alert('Veuillez sélectionner une image');
            this.value = '';
            document.getElementById('imagePreview').style.display = 'none';
            return;
        }

        // Afficher l'aperçu
        var reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('previewImg').src = e.target.result;
            document.getElementById('imagePreview').style.display = 'block';
        };
        reader.readAsDataURL(file);
    }
});

// Fermer les alertes automatiquement après 5 secondes
setTimeout(function() {
    $('.alert').fadeOut('slow');
}, 5000);
</script>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<script src="${contextPath}/resources/js/bootstrap.min.js"></script>

</body>
</html>
