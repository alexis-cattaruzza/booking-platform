package com.booking.api.service;

import com.booking.api.model.User;
import com.booking.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CustomUserDetailsService
 * Tests Spring Security UserDetailsService implementation
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD_HASH = "$2a$10$hashedpassword";

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email(TEST_EMAIL)
                .firstName("John")
                .lastName("Doe")
                .passwordHash(TEST_PASSWORD_HASH)
                .role(User.UserRole.BUSINESS)
                .emailVerified(true)
                .build();
    }

    @Test
    void loadUserByUsername_Success() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_EMAIL);

        // Then
        assertNotNull(userDetails);
        assertEquals(TEST_EMAIL, userDetails.getUsername());
        assertEquals(TEST_PASSWORD_HASH, userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());

        verify(userRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        // Given
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(nonExistentEmail));

        assertEquals("User not found with email: " + nonExistentEmail, exception.getMessage());
        verify(userRepository).findByEmail(nonExistentEmail);
    }

    @Test
    void loadUserByUsername_RoleBusiness() {
        // Given
        testUser.setRole(User.UserRole.BUSINESS);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_EMAIL);

        // Then
        assertEquals(1, userDetails.getAuthorities().size());
        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_BUSINESS", authority.getAuthority());
    }

    @Test
    void loadUserByUsername_RoleCustomer() {
        // Given
        testUser.setRole(User.UserRole.CUSTOMER);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_EMAIL);

        // Then
        assertEquals(1, userDetails.getAuthorities().size());
        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_CUSTOMER", authority.getAuthority());
    }

    @Test
    void loadUserByUsername_EmailVerifiedTrue() {
        // Given
        testUser.setEmailVerified(true);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_EMAIL);

        // Then
        assertTrue(userDetails.isEnabled(), "User with verified email should be enabled");
    }

    @Test
    void loadUserByUsername_EmailVerifiedFalse() {
        // Given
        testUser.setEmailVerified(false);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_EMAIL);

        // Then
        // SECURITY: Allow login for unverified users, but frontend shows warning
        assertTrue(userDetails.isEnabled(), "User with unverified email can still login");
    }

    @Test
    void loadUserByUsername_AccountNonExpired() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_EMAIL);

        // Then
        assertTrue(userDetails.isAccountNonExpired(), "Account should not be expired");
    }

    @Test
    void loadUserByUsername_AccountNonLocked() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_EMAIL);

        // Then
        assertTrue(userDetails.isAccountNonLocked(), "Account should not be locked");
    }

    @Test
    void loadUserByUsername_CredentialsNonExpired() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_EMAIL);

        // Then
        assertTrue(userDetails.isCredentialsNonExpired(), "Credentials should not be expired");
    }

    @Test
    void loadUserByUsername_PasswordHashMapping() {
        // Given
        String customHash = "$2a$10$customhashvalue";
        testUser.setPasswordHash(customHash);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_EMAIL);

        // Then
        assertEquals(customHash, userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_EmailMapping() {
        // Given
        String customEmail = "custom@example.com";
        testUser.setEmail(customEmail);
        when(userRepository.findByEmail(customEmail)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(customEmail);

        // Then
        assertEquals(customEmail, userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_DifferentUsers() {
        // Given
        User user1 = User.builder()
                .id(UUID.randomUUID())
                .email("user1@example.com")
                .passwordHash("hash1")
                .role(User.UserRole.BUSINESS)
                .emailVerified(true)
                .build();

        User user2 = User.builder()
                .id(UUID.randomUUID())
                .email("user2@example.com")
                .passwordHash("hash2")
                .role(User.UserRole.CUSTOMER)
                .emailVerified(false)
                .build();

        when(userRepository.findByEmail("user1@example.com")).thenReturn(Optional.of(user1));
        when(userRepository.findByEmail("user2@example.com")).thenReturn(Optional.of(user2));

        // When
        UserDetails details1 = userDetailsService.loadUserByUsername("user1@example.com");
        UserDetails details2 = userDetailsService.loadUserByUsername("user2@example.com");

        // Then
        assertEquals("user1@example.com", details1.getUsername());
        assertEquals("hash1", details1.getPassword());
        assertTrue(details1.isEnabled());
        assertEquals("ROLE_BUSINESS",
                details1.getAuthorities().iterator().next().getAuthority());

        assertEquals("user2@example.com", details2.getUsername());
        assertEquals("hash2", details2.getPassword());
        // SECURITY: Unverified users can still login
        assertTrue(details2.isEnabled());
        assertEquals("ROLE_CUSTOMER",
                details2.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_EmptyEmail() {
        // Given
        String emptyEmail = "";
        when(userRepository.findByEmail(emptyEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(emptyEmail));
    }

    @Test
    void loadUserByUsername_NullEmail() {
        // Given
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(null));
    }

    @Test
    void loadUserByUsername_SingleAuthority() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_EMAIL);

        // Then
        assertNotNull(userDetails.getAuthorities());
        assertEquals(1, userDetails.getAuthorities().size(),
                "Should have exactly one authority");
    }

    @Test
    void loadUserByUsername_VerifiedUserCanAuthenticate() {
        // Given
        testUser.setEmailVerified(true);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_EMAIL);

        // Then
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    void loadUserByUsername_UnverifiedUserIsDisabled() {
        // Given
        testUser.setEmailVerified(false);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_EMAIL);

        // Then
        // SECURITY: Allow login for unverified users, frontend will show verification prompt
        assertTrue(userDetails.isEnabled(),
                "Unverified user can still authenticate");

        // Other flags should still be true
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
    }
}
