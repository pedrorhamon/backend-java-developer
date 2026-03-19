package com.cmanager.app.core.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility class for common operations.
 */
public class Util {

    private static final int MAX_PAGE_SIZE = 1000;

    private Util() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a validated Pageable object with input validation.
     *
     * @param page      page number (0-indexed), must be >= 0
     * @param size      page size, must be between 1 and 1000
     * @param sortField field to sort by, must not be blank
     * @param sortOrder sort direction (ASC or DESC), case-insensitive
     * @return validated Pageable object
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public static Pageable getPageable(int page, int size, String sortField, String sortOrder) {
        validatePage(page);
        validateSize(size);
        validateSortField(sortField);
        validateSortOrder(sortOrder);

        final var sort = sortOrder.equalsIgnoreCase("ASC")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        return PageRequest.of(page, size, sort);
    }

    private static void validatePage(int page) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be >= 0, got: " + page);
        }
    }

    private static void validateSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be >= 1, got: " + size);
        }
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be <= " + MAX_PAGE_SIZE + ", got: " + size);
        }
    }

    private static void validateSortField(String sortField) {
        if (sortField == null || sortField.isBlank()) {
            throw new IllegalArgumentException("Sort field must not be blank");
        }
        // Basic validation: only allow alphanumeric, underscore, and dot (for nested properties)
        if (!sortField.matches("[a-zA-Z0-9_.]+")) {
            throw new IllegalArgumentException("Sort field contains invalid characters: " + sortField);
        }
    }

    private static void validateSortOrder(String sortOrder) {
        if (sortOrder == null || sortOrder.isBlank()) {
            throw new IllegalArgumentException("Sort order must not be blank");
        }
        if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
            throw new IllegalArgumentException("Sort order must be ASC or DESC, got: " + sortOrder);
        }
    }
}
