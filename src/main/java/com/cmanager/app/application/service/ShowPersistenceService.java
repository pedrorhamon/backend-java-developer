package com.cmanager.app.application.service;

import com.cmanager.app.application.domain.Episode;
import com.cmanager.app.application.domain.Show;
import com.cmanager.app.application.repository.EpisodeRepository;
import com.cmanager.app.application.repository.ShowRepository;
import com.cmanager.app.integration.dto.EpisodeRequestDTO;
import com.cmanager.app.integration.dto.ShowsRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShowPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(ShowPersistenceService.class);

    private final ShowRepository showRepository;
    private final EpisodeRepository episodeRepository;

    public ShowPersistenceService(ShowRepository showRepository,
                                  EpisodeRepository episodeRepository) {
        this.showRepository = showRepository;
        this.episodeRepository = episodeRepository;
    }

    @Transactional
    public Show persist(ShowsRequestDTO dto, List<EpisodeRequestDTO> episodeDTOs) {
        log.debug("[ShowPersistenceService] Persisting show '{}' within transaction", dto.name());

        final Show show = new Show();
        show.setIdIntegration(dto.id());
        show.setName(dto.name());
        show.setType(dto.type());
        show.setLanguage(dto.language());
        show.setStatus(dto.status());
        show.setRuntime(dto.runtime());
        show.setAverageRuntime(dto.averageRuntime());
        show.setOfficialSite(dto.officialSite());
        show.setRating(dto.rating() != null ? dto.rating().average() : null);
        show.setSummary(dto.summary());

        final Show saved = showRepository.save(show);
        log.debug("[ShowPersistenceService] Show entity saved — showId={}", saved.getId());

        if (!episodeDTOs.isEmpty()) {
            final List<Episode> episodes = episodeDTOs.stream()
                    .map(e -> toEpisode(e, saved))
                    .toList();
            episodeRepository.saveAll(episodes);
            log.debug("[ShowPersistenceService] {} episode(s) saved for showId={}", episodes.size(), saved.getId());
        } else {
            log.debug("[ShowPersistenceService] No episodes to persist for showId={}", saved.getId());
        }

        return saved;
    }

    private Episode toEpisode(EpisodeRequestDTO dto, Show show) {
        final Episode episode = new Episode();
        episode.setIdIntegration(dto.id());
        episode.setShow(show);
        episode.setName(dto.name());
        episode.setSeason(dto.season());
        episode.setNumber(dto.number());
        episode.setType(dto.type());
        episode.setAirdate(dto.airdate());
        episode.setAirtime(dto.airtime());
        episode.setAirstamp(dto.airstamp());
        episode.setRuntime(dto.runtime());
        episode.setRating(dto.rating() != null ? dto.rating().average() : null);
        episode.setSummary(dto.summary());
        return episode;
    }
}
