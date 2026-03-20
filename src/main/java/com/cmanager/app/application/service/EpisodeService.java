package com.cmanager.app.application.service;

import com.cmanager.app.application.data.SeasonAverageDTO;
import com.cmanager.app.application.data.SeasonRatingProjection;
import com.cmanager.app.application.repository.EpisodeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class EpisodeService {

    private static final Logger log = LoggerFactory.getLogger(EpisodeService.class);

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
        log.debug("[EpisodeService] Calculating season averages for showId={}", showId);

        final List<SeasonRatingProjection> projections =
                episodeRepository.findAverageRatingBySeasonAndShowId(showId);

        if (projections.isEmpty()) {
            log.warn("[EpisodeService] No episodes found for showId={}", showId);
            throw new EntityNotFoundException("No episodes found for show: " + showId);
        }

        final List<SeasonAverageDTO> result = projections.stream()
                .map(projection -> {
                    final BigDecimal rating = (projection.averageRating() == null)
                            ? BigDecimal.ZERO
                            : BigDecimal.valueOf(projection.averageRating())
                                    .setScale(2, RoundingMode.HALF_UP);
                    return new SeasonAverageDTO(projection.season(), rating);
                })
                .toList();

        log.debug("[EpisodeService] Season averages calculated — showId={}, seasons={}", showId, result.size());
        return result;
    }
}
