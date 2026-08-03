package com.foxstyle.api.util;

import java.util.regex.Pattern;

public final class PasswordPolicy {
    public static final String REGEX = "^[A-Z](?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{7,99}$";
    public static final String MESSAGE = "Mật khẩu phải từ 8 ký tự, bắt đầu bằng chữ in hoa và có chữ, số, ký tự đặc biệt";
    private static final Pattern PATTERN = Pattern.compile(REGEX);
    private PasswordPolicy() {}
    public static boolean isValid(String password) {
        return password != null && PATTERN.matcher(password).matches();
    }
}
