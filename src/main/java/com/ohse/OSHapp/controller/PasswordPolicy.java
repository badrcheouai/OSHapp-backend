package com.ohse.OSHapp.controller;

public class PasswordPolicy {
    public static boolean isValid(String pw) {
        if (pw == null) return false;
        if (pw.length() < 8) return false;
        if (!pw.matches(".*[A-Z].*")) return false;
        if (!pw.matches(".*[0-9].*")) return false;
        if (!pw.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) return false;
        return true;
    }
} 