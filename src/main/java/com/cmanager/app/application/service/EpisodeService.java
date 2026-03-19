package com.cmanager.app.application.service;

import com.cmanager.app.application.data.SeasonAverageDTO;
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
     * Calcula a média de rating por temporada de um show.
     * - Ratings nulos são ignorados no cálculo
     * - Se todos os ratings forem nulos → retorna 0 para a temporada
     * - Se não houver episódios → lança EntityNotFoundException
     */
    @Transactional(readOnly = true)
    public List<SeasonAverageDTO> getAverageByShow(String showId) {
        if (!episodeRepository.existsByShowId(showId)) {
            throw new EntityNotFoundException("No episodes found for show: " + showId);
        }

        final List<Object[]> rows = episodeRepository.findAverageRatingBySeasonAndShowId(showId);

        return rows.stream()
                .map(row -> {
                    final Integer season = (Integer) row[0];
                    final Double avg = (Double) row[1];
                    final BigDecimal rating = (avg == null)
                            ? BigDecimal.ZERO
                            : BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
                    return new SeasonAverageDTO(season, rating);
                })
                .toList();
    }
}
