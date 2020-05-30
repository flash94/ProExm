package com.example.proexm.models;

public class UserModel {
    private int id;
    private String UserName;
    private String Email;
    private String Password;
    private String Role;
    private int RoleId;
    private String AddedTimeStamp;

    public String getAddedTimeStamp() {
        return AddedTimeStamp;
    }

    public void setAddedTimeStamp(String addedTimeStamp) {
        AddedTimeStamp = addedTimeStamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }

    public int getRoleId() {
        return RoleId;
    }

    public void setRoleId(int roleId) {
        RoleId = roleId;
    }
}
