package com.cmanager.app.application.service;

import com.cmanager.app.application.domain.Show;
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
    private final ShowPersistenceService showPersistenceService;
    private final RequestService requestService;

    public ShowService(ShowRepository showRepository,
                       ShowPersistenceService showPersistenceService,
                       RequestService requestService) {
        this.showRepository = showRepository;
        this.showPersistenceService = showPersistenceService;
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
        log.debug("[ShowService] TVMaze response received — id={}, name='{}'", dto.id(), dto.name());

        // Check for duplicates before entering transaction
        if (showRepository.existsByIdIntegration(dto.id())) {
            log.warn("[ShowService] Show '{}' (id={}) already exists in the database", dto.name(), dto.id());
            throw new AlreadyExistsException("Show", dto.name());
        }

        // Extract episodes with null safety
        final List<EpisodeRequestDTO> episodeDTOs = extractEpisodes(dto);
        log.debug("[ShowService] {} episode(s) extracted from TVMaze response", episodeDTOs.size());

        // Persist within transaction — delegated to separate bean so @Transactional proxy is honoured
        final Show saved = showPersistenceService.persist(dto, episodeDTOs);
        log.info("[ShowService] Show '{}' synced successfully — showId={}, episodes={}", saved.getName(), saved.getId(), episodeDTOs.size());
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

}
