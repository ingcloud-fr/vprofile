<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Update Profile</title>

    <link href="${contextPath}/resources/css/bootstrap.min.css" rel="stylesheet">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css">
	<link rel="stylesheet" href="https://bootswatch.com/cosmo/bootstrap.min.css">
    <style>
        body {
            background-color: #f5f5f5;
            padding-top: 40px;
            padding-bottom: 40px;
        }
        .form-profile {
            max-width: 600px;
            margin: 0 auto;
            background-color: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .form-profile h2 {
            margin-bottom: 30px;
            color: #333;
        }
        .form-group label {
            font-weight: 600;
            color: #555;
        }
        .readonly-field {
            background-color: #e9ecef;
            cursor: not-allowed;
        }
        .btn-update {
            margin-right: 10px;
        }
    </style>
</head>

<body>

<div class="form-profile">
    <h2 class="text-center"><i class="fa fa-user-circle"></i> Update Your Profile</h2>

    <form:form method="POST" modelAttribute="user" class="form-horizontal">

        <!-- Username (non modifiable) -->
        <div class="form-group">
            <label for="username"><i class="fa fa-user"></i> Username</label>
            <form:input path="username" class="form-control readonly-field" readonly="true" placeholder="Your username"/>
            <small class="form-text text-muted">Username cannot be changed</small>
        </div>

        <!-- Email -->
        <div class="form-group">
            <label for="userEmail"><i class="fa fa-envelope"></i> Email Address</label>
            <form:input path="userEmail" type="email" class="form-control" placeholder="your.email@example.com"/>
            <small class="form-text text-muted">Your contact email</small>
        </div>

        <!-- Location -->
        <div class="form-group">
            <label for="permanentAddress"><i class="fa fa-map-marker"></i> Location</label>
            <form:input path="permanentAddress" class="form-control" placeholder="e.g., Paris, France"/>
            <small class="form-text text-muted">Where are you based?</small>
        </div>

        <!-- Bio -->
        <div class="form-group">
            <label for="skills"><i class="fa fa-pencil"></i> Biography</label>
            <form:textarea path="skills" class="form-control" rows="5" placeholder="Tell us about yourself, your skills, your interests..."/>
            <small class="form-text text-muted">A short description about you</small>
        </div>

        <!-- Buttons -->
        <div class="form-group text-center" style="margin-top: 30px;">
            <button type="submit" class="btn btn-success btn-lg btn-update">
                <i class="fa fa-check"></i> Update Profile
            </button>
            <a href="${contextPath}/welcome" class="btn btn-secondary btn-lg">
                <i class="fa fa-times"></i> Cancel
            </a>
        </div>

    </form:form>
</div>

<script src="${contextPath}/resources/js/bootstrap.min.js"></script>
</body>
</html>
