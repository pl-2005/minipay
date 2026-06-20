package com.minipay.notify.service;

import java.net.URI;
import java.util.Locale;

import com.minipay.notify.repository.NotifyRecord;
import com.minipay.notify.repository.PaymentEvent;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class MerchantCallbackClient {
    private final RestClient merchantCallbackRestClient;

    public MerchantCallbackClient(RestClient merchantCallbackRestClient) {
        this.merchantCallbackRestClient = merchantCallbackRestClient;
    }

    public CallbackResult send(NotifyRecord notifyRecord, PaymentEvent paymentEvent) {
        URI callbackUri;
        try {
            callbackUri = URI.create(notifyRecord.callbackUrl());
        } catch (IllegalArgumentException ex) {
            return CallbackResult.failure("INVALID_CALLBACK_URL: " + ex.getMessage());
        }

        String scheme = callbackUri.getScheme();
        String host = callbackUri.getHost();
        if (scheme == null || host == null || !isHttp(scheme)) {
            return CallbackResult.failure("UNSUPPORTED_CALLBACK_URL: " + notifyRecord.callbackUrl());
        }

        if (isSimulatedCallbackHost(host)) {
            return CallbackResult.success("SIMULATED_CALLBACK_OK");
        }

        try {
            ResponseEntity<String> response = merchantCallbackRestClient.post()
                    .uri(callbackUri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(paymentEvent.payload())
                    .retrieve()
                    .toEntity(String.class);
            return CallbackResult.success("HTTP " + response.getStatusCode().value()
                    + " " + nullToEmpty(response.getBody()));
        } catch (RestClientResponseException ex) {
            return CallbackResult.failure("HTTP " + ex.getStatusCode().value()
                    + " " + nullToEmpty(ex.getResponseBodyAsString()));
        } catch (RestClientException ex) {
            return CallbackResult.failure(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private boolean isHttp(String scheme) {
        String normalized = scheme.toLowerCase(Locale.ROOT);
        return "http".equals(normalized) || "https".equals(normalized);
    }

    private boolean isSimulatedCallbackHost(String host) {
        String normalized = host.toLowerCase(Locale.ROOT);
        return normalized.equals("merchant.example.local") || normalized.endsWith(".example.local");
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
