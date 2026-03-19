package com.cmanager.app.application.data;

/**
 * Internal record for season rating statistics query result.
 * Provides type-safe access to episode average ratings grouped by season.
 *
 * @param season the season number
 * @param averageRating the average rating for the season (nullable)
 */
public record SeasonRatingProjection(
        Integer season,
        Double averageRating
) {
}
