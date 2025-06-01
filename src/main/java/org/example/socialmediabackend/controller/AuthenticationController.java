package org.example.socialmediabackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmediabackend.dto.*;
import org.example.socialmediabackend.model.User;
import org.example.socialmediabackend.dto.LoginResponse;
import org.example.socialmediabackend.service.AuthenticationService;
import org.example.socialmediabackend.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileDto>> register(@Valid @RequestBody RegisterUserDto registerUserDto) {
        log.info("Register user API is called, request: {}", registerUserDto);
        User registeredUser = authenticationService.signup(registerUserDto);
        UserProfileDto userProfile = UserProfileDto.fromUser(registeredUser);
        log.info("User registered successfully, email: {}", registeredUser.getEmail());
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", userProfile));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> authenticate(@Valid @RequestBody LoginUserDto loginUserDto) {
        log.info("Login user API is called, request: {}", loginUserDto);
        User authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpirationTime());
        log.info("User login successful, email: {}", authenticatedUser.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyUser(@Valid @RequestBody VerifyUserDto verifyUserDto) {
        log.info("Verify user API is called, request: {}", verifyUserDto);
        authenticationService.verifyUser(verifyUserDto);
        log.info("Account verified successfully, email: {}", verifyUserDto.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Account verified successfully"));
    }

    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<Void>> resendVerificationCode(@RequestParam String email) {
        log.info("Resend verification code API is called, email: {}", email);
        authenticationService.resendVerificationCode(email);
        log.info("Verification code sent successfully, email: {}", email);
        return ResponseEntity.ok(ApiResponse.success("Verification code sent successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto requestDto) {
        log.info("Forgot password API is called, request: {}", requestDto);
        authenticationService.sendPasswordResetEmail(requestDto.getEmail());
        log.info("Password reset code sent successfully, email: {}", requestDto.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Password reset code sent to your email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        log.info("Reset password API is called, request: {}", resetPasswordDto);
        authenticationService.resetPassword(resetPasswordDto);
        log.info("Password reset successful, email: {}", resetPasswordDto.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Password reset successful"));
    }
}