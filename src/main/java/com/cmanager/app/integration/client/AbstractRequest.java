package com.cmanager.app.integration.client;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class AbstractRequest<T> {

    private final RestTemplate restTemplate;

    public AbstractRequest(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public T getShow(String url, ParameterizedTypeReference<T> typeReference) {
        try {
            return restTemplate.exchange(url, HttpMethod.GET, null, typeReference).getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new EntityNotFoundException("Show not found on TVMaze");
        }
    }
}
