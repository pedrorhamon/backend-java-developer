package com.cmanager.app.service;

import com.cmanager.app.application.data.SeasonAverageDTO;
import com.cmanager.app.application.repository.EpisodeRepository;
import com.cmanager.app.application.service.EpisodeService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EpisodeService - Testes Unitários")
class EpisodeServiceTest {

    @Mock
    private EpisodeRepository episodeRepository;

    @InjectMocks
    private EpisodeService episodeService;

    @Test
    @DisplayName("getAverageByShow() lança EntityNotFoundException quando não há episódios")
    void getAverage_noEpisodes_throws() {
        final String showId = "show-sem-episodios";
        when(episodeRepository.existsByShowId(showId)).thenReturn(false);

        assertThatThrownBy(() -> episodeService.getAverageByShow(showId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(showId);
    }

    @Test
    @DisplayName("getAverageByShow() retorna 0 quando todos os ratings são nulos")
    void getAverage_allNullRatings_returnsZero() {
        final String showId = "show-sem-rating";
        when(episodeRepository.existsByShowId(showId)).thenReturn(true);
        // AVG retorna null quando todos os valores são NULL no SQL
        when(episodeRepository.findAverageRatingBySeasonAndShowId(showId))
                .thenReturn(List.of(new Object[]{1, null}, new Object[]{2, null}));

        final List<SeasonAverageDTO> result = episodeService.getAverageByShow(showId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).season()).isEqualTo(1);
        assertThat(result.get(0).averageRating()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.get(1).season()).isEqualTo(2);
        assertThat(result.get(1).averageRating()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getAverageByShow() calcula média corretamente por temporada")
    void getAverage_validRatings_returnsCorrectAverage() {
        final String showId = "show-com-rating";
        when(episodeRepository.existsByShowId(showId)).thenReturn(true);
        when(episodeRepository.findAverageRatingBySeasonAndShowId(showId))
                .thenReturn(List.of(
                        new Object[]{1, 8.5},
                        new Object[]{2, 7.333333333}
                ));

        final List<SeasonAverageDTO> result = episodeService.getAverageByShow(showId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).season()).isEqualTo(1);
        assertThat(result.get(0).averageRating())
                .isEqualByComparingTo(BigDecimal.valueOf(8.5).setScale(2, RoundingMode.HALF_UP));
        assertThat(result.get(1).season()).isEqualTo(2);
        assertThat(result.get(1).averageRating())
                .isEqualByComparingTo(BigDecimal.valueOf(7.33).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("getAverageByShow() ignora ratings nulos e calcula média dos demais na temporada")
    void getAverage_mixedNullAndValidRatings_calculatesPartialAverage() {
        final String showId = "show-mixed";
        when(episodeRepository.existsByShowId(showId)).thenReturn(true);
        // SQL AVG ignora NULLs automaticamente — aqui simula o resultado já calculado
        when(episodeRepository.findAverageRatingBySeasonAndShowId(showId))
                .thenReturn(List.of(new Object[]{1, 9.0})); // Ex.: 2 episódios com rating, 1 sem

        final List<SeasonAverageDTO> result = episodeService.getAverageByShow(showId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).averageRating())
                .isEqualByComparingTo(BigDecimal.valueOf(9.00).setScale(2, RoundingMode.HALF_UP));
    }
}
