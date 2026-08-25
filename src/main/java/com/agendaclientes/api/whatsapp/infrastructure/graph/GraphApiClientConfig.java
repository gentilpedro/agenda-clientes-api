package com.agendaclientes.api.whatsapp.infrastructure.graph;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
class GraphApiClientConfig {

    @Bean
    RestClient whatsappRestClient(@Value("${app.whatsapp.graph-api-version}") String versaoGraphApi) {
        return RestClient.builder()
                .baseUrl("https://graph.facebook.com/" + versaoGraphApi)
                .build();
    }
}
