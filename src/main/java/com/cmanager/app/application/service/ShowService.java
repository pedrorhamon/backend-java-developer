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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class ShowService {

    private static final Logger log = LoggerFactory.getLogger(ShowService.class);

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
        log.info("[ShowService] Starting sync for show: '{}'", showName);

        // Fetch from external API outside transaction to avoid holding DB connection
        final ShowsRequestDTO dto = requestService.getShow(showName);
        if (dto == null) {
            log.warn("[ShowService] Show '{}' not found on TVMaze", showName);
            throw new EntityNotFoundException("Show not found on TVMaze: " + showName);
        }
        log.debug("[ShowService] TVMaze response received — id={}, name='{}'", dto.id(), dto.name());

        // Check for duplicates before entering transaction
        if (showRepository.existsByIdIntegration(dto.id())) {
            log.warn("[ShowService] Show '{}' (id={}) already exists in the database", dto.name(), dto.id());
            throw new AlreadyExistsException("Show", dto.name());
        }

        // Extract episodes with null safety
        final List<EpisodeRequestDTO> episodeDTOs = extractEpisodes(dto);
        log.debug("[ShowService] {} episode(s) extracted from TVMaze response", episodeDTOs.size());

        // Persist within transaction
        final Show saved = persistShow(dto, episodeDTOs);
        log.info("[ShowService] Show '{}' synced successfully — showId={}, episodes={}", saved.getName(), saved.getId(), episodeDTOs.size());
        return saved;
    }

    @Transactional
    protected Show persistShow(ShowsRequestDTO dto, List<EpisodeRequestDTO> episodeDTOs) {
        log.debug("[ShowService] Persisting show '{}' within transaction", dto.name());

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
        log.debug("[ShowService] Show entity saved — showId={}", saved.getId());

        if (!episodeDTOs.isEmpty()) {
            final List<Episode> episodes = episodeDTOs.stream()
                    .map(e -> toEpisode(e, saved))
                    .toList();
            episodeRepository.saveAll(episodes);
            log.debug("[ShowService] {} episode(s) saved for showId={}", episodes.size(), saved.getId());
        } else {
            log.debug("[ShowService] No episodes to persist for showId={}", saved.getId());
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
        log.debug("[ShowService] Listing shows — filter='{}', page={}, size={}", name, pageable.getPageNumber(), pageable.getPageSize());
        final Page<Show> result = showRepository.findByNameContainingIgnoreCase(name, pageable);
        log.debug("[ShowService] Found {} show(s) (total={})", result.getNumberOfElements(), result.getTotalElements());
        return result;
    }

    @Transactional(readOnly = true)
    public Show findById(String id) {
        log.debug("[ShowService] Looking up show by id={}", id);
        return showRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[ShowService] Show not found — id={}", id);
                    return new EntityNotFoundException("Show not found: " + id);
                });
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
