package com.example.opp.model;

import java.time.LocalDateTime;

public class Guest {

    public enum IdType { KTP, PASSPORT, SIM }

    private Long id;
    private String idNumber;
    private IdType idType;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Guest() {
        this.idType = IdType.KTP;
    }

    public Guest(String idNumber, String fullName, String phone) {
        this.idNumber = idNumber;
        this.fullName = fullName;
        this.phone = phone;
        this.idType = IdType.KTP;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public IdType getIdType() { return idType; }
    public void setIdType(IdType idType) { this.idType = idType; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
