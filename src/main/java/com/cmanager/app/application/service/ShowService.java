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

    /**
     * Synchronizes a TV show from TVMaze API and persists it with its episodes.
     * The external API call is made outside of the transaction to avoid
     * holding a database transaction during network I/O.
     *
     * @param showName the name of the show to synchronize
     * @return the persisted Show entity with episodes
     * @throws EntityNotFoundException if the show is not found on TVMaze
     * @throws AlreadyExistsException if the show already exists in the database
     */
    public Show sync(String showName) {
        // Fetch from external API outside transaction to avoid holding DB connection
        final ShowsRequestDTO dto = requestService.getShow(showName);
        if (dto == null) {
            throw new EntityNotFoundException("Show not found on TVMaze: " + showName);
        }

        // Check for duplicates before entering transaction
        if (showRepository.existsByIdIntegration(dto.id())) {
            throw new AlreadyExistsException("Show", dto.name());
        }

        // Extract episodes with null safety
        final List<EpisodeRequestDTO> episodeDTOs = extractEpisodes(dto);

        // Persist within transaction
        return persistShow(dto, episodeDTOs);
    }

    @Transactional
    protected Show persistShow(ShowsRequestDTO dto, List<EpisodeRequestDTO> episodeDTOs) {
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

        if (!episodeDTOs.isEmpty()) {
            final List<Episode> episodes = episodeDTOs.stream()
                    .map(e -> toEpisode(e, saved))
                    .toList();
            episodeRepository.saveAll(episodes);
        }

        return saved;
    }

    /**
     * Safely extracts episodes from the external API response with null checks.
     */
    private List<EpisodeRequestDTO> extractEpisodes(ShowsRequestDTO dto) {
        if (dto._embedded() == null || dto._embedded().episodes() == null) {
            return Collections.emptyList();
        }
        return dto._embedded().episodes();
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
