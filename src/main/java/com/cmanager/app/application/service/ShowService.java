package com.cmanager.app.application.service;

import com.cmanager.app.application.domain.Episode;
import com.cmanager.app.application.domain.Show;
import com.cmanager.app.application.repository.EpisodeRepository;
import com.cmanager.app.application.repository.ShowRepository;
import com.cmanager.app.core.exception.AlreadyExistsException;
import com.cmanager.app.integration.client.RequestService;
import com.cmanager.app.integration.dto.EpisodeRequestDTO;
import com.cmanager.app.integration.dto.ShowsRequestDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final EpisodeRepository episodeRepository;
    private final RequestService requestService;

    public ShowService(ShowRepository showRepository,
                       EpisodeRepository episodeRepository,
                       RequestService requestService) {
        this.showRepository = showRepository;
        this.episodeRepository = episodeRepository;
        this.requestService = requestService;
    }

    @Transactional
    public Show sync(String showName) {
        final ShowsRequestDTO dto = requestService.getShow(showName);
        if (dto == null) {
            throw new EntityNotFoundException("Show not found on TVMaze: " + showName);
        }
        if (showRepository.existsByIdIntegration(dto.id())) {
            throw new AlreadyExistsException("Show", dto.name());
        }

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

        final List<EpisodeRequestDTO> episodeDTOs = (dto._embedded() != null && dto._embedded().episodes() != null)
                ? dto._embedded().episodes()
                : Collections.emptyList();

        final List<Episode> episodes = episodeDTOs.stream()
                .map(e -> toEpisode(e, saved))
                .toList();

        if (!episodes.isEmpty()) {
            episodeRepository.saveAll(episodes);
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Show> list(String name, Pageable pageable) {
        return showRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Show findById(String id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Show not found: " + id));
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
