package com.cmanager.app.service;

import com.cmanager.app.application.data.SeasonAverageDTO;
import com.cmanager.app.application.data.SeasonRatingProjection;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EpisodeServiceTest {

    @Mock
    private EpisodeRepository episodeRepository;

    @InjectMocks
    private EpisodeService episodeService;

    @Test
    @DisplayName("Deve lancar EntityNotFoundException quando nao ha episodios para o show")
    void shouldThrowWhenNoEpisodes() {
        when(episodeRepository.findAverageRatingBySeasonAndShowId("show-1"))
                .thenReturn(List.of());

        assertThatThrownBy(() -> episodeService.getAverageByShow("show-1"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("show-1");
    }

    @Test
    @DisplayName("Deve retornar 0 quando todos os ratings de uma temporada sao nulos")
    void shouldReturnZeroWhenAllRatingsNull() {
        when(episodeRepository.findAverageRatingBySeasonAndShowId("show-2"))
                .thenReturn(List.of(new SeasonRatingProjection(1, null)));

        List<SeasonAverageDTO> result = episodeService.getAverageByShow("show-2");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).season()).isEqualTo(1);
        assertThat(result.get(0).averageRating()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Deve calcular media correta por temporada ignorando nulls")
    void shouldCalculateCorrectAveragePerSeason() {
        when(episodeRepository.findAverageRatingBySeasonAndShowId("show-3"))
                .thenReturn(List.of(
                        new SeasonRatingProjection(1, 8.5),
                        new SeasonRatingProjection(2, 7.333333333)
                ));

        List<SeasonAverageDTO> result = episodeService.getAverageByShow("show-3");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).season()).isEqualTo(1);
        assertThat(result.get(0).averageRating()).isEqualByComparingTo(new BigDecimal("8.50"));
        assertThat(result.get(1).season()).isEqualTo(2);
        assertThat(result.get(1).averageRating()).isEqualByComparingTo(new BigDecimal("7.33"));
    }

    @Test
    @DisplayName("Deve retornar zero para temporada com todos ratings nulos em mix com validos")
    void shouldReturnZeroForNullSeasonsWithinMix() {
        when(episodeRepository.findAverageRatingBySeasonAndShowId("show-4"))
                .thenReturn(List.of(
                        new SeasonRatingProjection(1, 9.0),
                        new SeasonRatingProjection(2, null),
                        new SeasonRatingProjection(3, 8.25)
                ));

        List<SeasonAverageDTO> result = episodeService.getAverageByShow("show-4");

        assertThat(result).hasSize(3);
        assertThat(result.get(0).averageRating()).isEqualByComparingTo(new BigDecimal("9.00"));
        assertThat(result.get(1).averageRating()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.get(2).averageRating()).isEqualByComparingTo(new BigDecimal("8.25"));
    }
}
