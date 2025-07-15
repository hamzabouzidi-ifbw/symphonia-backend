package com.example.tenant.Dto;

public class DidResponse {
    private String didNumber;

    public DidResponse(String didNumber) {
        this.didNumber = didNumber;
    }

    public String getDidNumber() {
        return didNumber;
    }

    public void setDidNumber(String didNumber) {
        this.didNumber = didNumber;
    }
}
