package com.ksr.mealhuteats.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsernameValidation {

    public static int validate(String asUsername){
        if (asUsername != null) {
            if (isValidEmail(asUsername)) {
                return 1;
            } else if (isValidPhone(asUsername)) {
                return 2;
            }
            return 0;
        }
        return 0;
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidPhone(String phone) {
        String phoneRegex = "^07[01245678][0-9]{7}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }
}
