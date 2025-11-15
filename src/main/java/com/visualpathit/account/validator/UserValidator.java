package com.visualpathit.account.validator;

import com.visualpathit.account.model.User;
import com.visualpathit.account.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {
    @Autowired
    private UserService userService;

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        User user = (User) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "NotEmpty");
        // Check for null before calling .length() to avoid NullPointerException
        if (user.getUsername() != null &&
            (user.getUsername().length() < 5 || user.getUsername().length() > 32)) {
            errors.rejectValue("username", "Size.userForm.username");
        }
        if (user.getUsername() != null && userService.findByUsername(user.getUsername()) != null) {
            errors.rejectValue("username", "Duplicate.userForm.username");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotEmpty");
        // Check for null before calling .length() to avoid NullPointerException
        if (user.getPassword() != null &&
            (user.getPassword().length() < 8 || user.getPassword().length() > 32)) {
            errors.rejectValue("password", "Size.userForm.password");
        }

        // Check for null before comparing passwords to avoid NullPointerException
        if (user.getPasswordConfirm() != null && user.getPassword() != null &&
            !user.getPasswordConfirm().equals(user.getPassword())) {
            errors.rejectValue("passwordConfirm", "Diff.userForm.passwordConfirm");
        }
    }
}
