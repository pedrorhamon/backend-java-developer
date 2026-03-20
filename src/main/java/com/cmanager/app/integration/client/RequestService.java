package com.cmanager.app.integration.client;

import com.cmanager.app.integration.dto.ShowsRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
        final var encoded = URLEncoder.encode(showName, StandardCharsets.UTF_8);
        final var url = String.format(tvMazeUrl, encoded);
        return abstractConnect.getShow(url, new ParameterizedTypeReference<>() {
        });
    }
}
