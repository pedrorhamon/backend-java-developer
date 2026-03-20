package com.cmanager.app.integration.client;

import com.cmanager.app.integration.dto.ShowsRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class RequestService {

    private final String tvMazeUrl;
    private final AbstractRequest<ShowsRequestDTO> abstractConnect;

    public RequestService(
            @Value("${app.tvmaze.url}") String tvMazeUrl,
            AbstractRequest<ShowsRequestDTO> abstractConnect) {
        this.tvMazeUrl = tvMazeUrl;
        this.abstractConnect = abstractConnect;
    }

    public ShowsRequestDTO getShow(String showName) {
        final var url = String.format(tvMazeUrl, showName);
        return abstractConnect.getShow(url, new ParameterizedTypeReference<>() {
        });
    }
}
