package org.example.socialmediabackend.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmediabackend.dto.LoginUserDto;
import org.example.socialmediabackend.dto.RegisterUserDto;
import org.example.socialmediabackend.dto.ResetPasswordDto;
import org.example.socialmediabackend.dto.VerifyUserDto;
import org.example.socialmediabackend.exception.ResourceNotFoundException;
import org.example.socialmediabackend.model.User;
import org.example.socialmediabackend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;


    public User signup(RegisterUserDto input) {
        log.info("Starting user signup for email: {}", input.getEmail());

        User user = new User(
                input.getUsername(),
                input.getEmail(),
                passwordEncoder.encode(input.getPassword()),
                input.getFirstName(),
                input.getLastName()
        );
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);

        sendVerificationEmail(user);
        User savedUser = userRepository.save(user);

        log.info("Successfully created user account for email: {}", input.getEmail());
        return savedUser;
    }

    public User authenticate(LoginUserDto input) {
        log.info("Authenticating user with email: {}", input.getEmail());

        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + input.getEmail()));

        if (!user.isEnabled()) {
            log.warn("Authentication failed - account not verified for email: {}", input.getEmail());
            throw new IllegalStateException("Account not verified. Please verify your account.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        log.info("Successfully authenticated user with email: {}", input.getEmail());
        return user;
    }

    public void verifyUser(VerifyUserDto input) {
        log.info("Verifying user account for email: {}", input.getEmail());

        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found with email: " + input.getEmail());
        }

        User user = optionalUser.get();
        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Verification code has expired for email: {}", input.getEmail());
            throw new IllegalStateException("Verification code has expired");
        }

        if (!user.getVerificationCode().equals(input.getVerificationCode())) {
            log.warn("Invalid verification code provided for email: {}", input.getEmail());
            throw new IllegalArgumentException("Invalid verification code");
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);

        log.info("Successfully verified user account for email: {}", input.getEmail());
    }

    public void resendVerificationCode(String email) {
        log.info("Resending verification code for email: {}", email);

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }

        User user = optionalUser.get();
        if (user.isEnabled()) {
            log.warn("Attempted to resend verification code for already verified account: {}", email);
            throw new IllegalStateException("Account is already verified");
        }

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
        sendVerificationEmail(user);
        userRepository.save(user);

        log.info("Successfully resent verification code for email: {}", email);
    }

    public void sendPasswordResetEmail(String email) {
        log.info("Sending password reset email for: {}", email);

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }

        User user = optionalUser.get();
        String resetCode = generateVerificationCode();
        user.setResetCode(resetCode);
        user.setResetCodeExpiresAt(LocalDateTime.now().plusHours(1));

        sendPasswordResetEmail(user);
        userRepository.save(user);

        log.info("Successfully sent password reset email for: {}", email);
    }

    public void resetPassword(ResetPasswordDto resetPasswordDto) {
        log.info("Resetting password for email: {}", resetPasswordDto.getEmail());

        Optional<User> optionalUser = userRepository.findByEmail(resetPasswordDto.getEmail());
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found with email: " + resetPasswordDto.getEmail());
        }

        User user = optionalUser.get();
        if (user.getResetCodeExpiresAt() == null ||
                user.getResetCodeExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Reset code has expired for email: {}", resetPasswordDto.getEmail());
            throw new IllegalStateException("Reset code has expired");
        }

        if (user.getResetCode() == null ||
                !user.getResetCode().trim().equals(resetPasswordDto.getResetCode().trim())) {
            log.warn("Invalid reset code provided for email: {}", resetPasswordDto.getEmail());
            throw new IllegalArgumentException("Invalid reset code");
        }

        user.setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        user.setResetCode(null);
        user.setResetCodeExpiresAt(null);
        userRepository.save(user);

        log.info("Successfully reset password for email: {}", resetPasswordDto.getEmail());
    }

    private void sendVerificationEmail(User user) {
        String subject = "Account Verification";
        String verificationCode = "VERIFICATION CODE " + user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
            log.info("Verification email sent successfully to: {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private void sendPasswordResetEmail(User user) {
        String subject = "Password Reset";
        String resetCode = "PASSWORD RESET CODE: " + user.getResetCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Password Reset Request</h2>"
                + "<p style=\"font-size: 16px;\">We received a request to reset your password. Please use the code below:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Reset Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + resetCode + "</p>"
                + "</div>"
                + "<p style=\"margin-top: 20px;\">If you did not request a password reset, please ignore this email.</p>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
            log.info("Password reset email sent successfully to: {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}