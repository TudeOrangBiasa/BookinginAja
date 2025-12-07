package com.example.opp.util;

public class GenerateHashes {
    public static void main(String[] args) {
        String admin = PasswordUtil.hash("admin123");
        String staff = PasswordUtil.hash("staff123");
        
        System.out.println("-- Run this in Supabase SQL Editor:");
        System.out.println("UPDATE users SET password = '" + admin + "' WHERE username = 'admin';");
        System.out.println("UPDATE users SET password = '" + staff + "' WHERE username = 'receptionist';");
    }
}
