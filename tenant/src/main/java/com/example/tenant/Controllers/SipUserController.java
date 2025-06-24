package com.example.tenant.Controllers;

import com.example.tenant.Dto.CreateSipUserRequest;
import com.example.tenant.Dto.SipUserCreationResponse;
import com.example.tenant.Services.SipUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/sip-users")

public class SipUserController {
    @Autowired
    private SipUserService sipUserService;

    @PostMapping
    public ResponseEntity<SipUserCreationResponse> createSipUser(
            @RequestBody CreateSipUserRequest request,
            @RequestHeader("Authorization") String token) {

        SipUserCreationResponse response = sipUserService.createSipUser(request, token);
        return ResponseEntity.ok(response);
    }





}