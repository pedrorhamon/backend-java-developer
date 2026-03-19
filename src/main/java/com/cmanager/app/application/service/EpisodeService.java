package com.cmanager.app.application.service;

import com.cmanager.app.application.data.SeasonAverageDTO;
import com.cmanager.app.application.data.SeasonRatingProjection;
import com.cmanager.app.application.repository.EpisodeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class EpisodeService {

    private final EpisodeRepository episodeRepository;

    public EpisodeService(EpisodeRepository episodeRepository) {
        this.episodeRepository = episodeRepository;
    }

    /**
     * Calculates average episode ratings per season for a show.
     * - Null ratings are ignored in the calculation
     * - If all ratings are null → returns 0 for the season
     * - If no episodes exist → throws EntityNotFoundException
     *
     * @param showId the unique identifier of the show
     * @return list of season average DTOs sorted by season number
     * @throws EntityNotFoundException if no episodes found for the show
     */
    @Transactional(readOnly = true)
    public List<SeasonAverageDTO> getAverageByShow(String showId) {
        final List<SeasonRatingProjection> projections =
                episodeRepository.findAverageRatingBySeasonAndShowId(showId);

        if (projections.isEmpty()) {
            throw new EntityNotFoundException("No episodes found for show: " + showId);
        }

        return projections.stream()
                .map(projection -> {
                    final BigDecimal rating = (projection.averageRating() == null)
                            ? BigDecimal.ZERO
                            : BigDecimal.valueOf(projection.averageRating())
                                    .setScale(2, RoundingMode.HALF_UP);
                    return new SeasonAverageDTO(projection.season(), rating);
                })
                .toList();
    }
}
