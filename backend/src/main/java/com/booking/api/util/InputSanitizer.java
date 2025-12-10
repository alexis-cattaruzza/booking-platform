package com.booking.api.util;

import lombok.extern.slf4j.Slf4j;

/**
 * SECURITY: Utility class for sanitizing user input to prevent XSS attacks
 */
@Slf4j
public class InputSanitizer {

    /**
     * SECURITY: Sanitize HTML to prevent XSS attacks
     * Removes potentially dangerous HTML tags and attributes
     *
     * @param input Raw user input that may contain HTML
     * @return Sanitized string safe for display
     */
    public static String sanitizeHtml(String input) {
        if (input == null) {
            return null;
        }

        // Remove script tags and their content
        String sanitized = input.replaceAll("(?i)<script[^>]*>.*?</script>", "");

        // Remove event handlers (onclick, onload, etc.)
        sanitized = sanitized.replaceAll("(?i)\\s*on\\w+\\s*=\\s*[\"'][^\"']*[\"']", "");
        sanitized = sanitized.replaceAll("(?i)\\s*on\\w+\\s*=\\s*\\S+", "");

        // Remove javascript: protocol
        sanitized = sanitized.replaceAll("(?i)javascript:", "");

        // Remove data: protocol (can be used for XSS)
        sanitized = sanitized.replaceAll("(?i)data:", "");

        // Remove potentially dangerous tags
        sanitized = sanitized.replaceAll("(?i)<(iframe|object|embed|applet|meta|link)[^>]*>", "");

        // Remove style attributes (can contain expressions)
        sanitized = sanitized.replaceAll("(?i)\\s*style\\s*=\\s*[\"'][^\"']*[\"']", "");

        return sanitized;
    }

    /**
     * SECURITY: Sanitize text for SQL-like patterns (defense in depth)
     * Note: Should be used WITH prepared statements, not instead of them
     *
     * @param input Raw user input
     * @return Sanitized string
     */
    public static String sanitizeSql(String input) {
        if (input == null) {
            return null;
        }

        // Remove SQL comment patterns
        String sanitized = input.replaceAll("--", "");
        sanitized = sanitized.replaceAll("/\\*.*?\\*/", "");

        // Remove semicolons (statement terminators)
        sanitized = sanitized.replaceAll(";", "");

        return sanitized;
    }

    /**
     * SECURITY: Remove control characters that could cause issues
     *
     * @param input Raw user input
     * @return Sanitized string without control characters
     */
    public static String removeControlCharacters(String input) {
        if (input == null) {
            return null;
        }

        // Remove control characters except newline, carriage return, and tab
        return input.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
    }

    /**
     * SECURITY: Sanitize filename to prevent path traversal
     *
     * @param filename User-provided filename
     * @return Safe filename
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) {
            return null;
        }

        // Remove path separators
        String sanitized = filename.replaceAll("[/\\\\]", "");

        // Remove path traversal patterns
        sanitized = sanitized.replaceAll("\\.\\.", "");

        // Remove null bytes
        sanitized = sanitized.replaceAll("\\x00", "");

        // Keep only safe characters
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9._-]", "_");

        return sanitized;
    }

    /**
     * SECURITY: Comprehensive sanitization for user-generated content
     * Applies multiple sanitization techniques
     *
     * @param input Raw user input
     * @return Sanitized string safe for storage and display
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }

        String sanitized = input;
        sanitized = removeControlCharacters(sanitized);
        sanitized = sanitizeHtml(sanitized);

        // Trim whitespace
        sanitized = sanitized.trim();

        return sanitized;
    }
}
