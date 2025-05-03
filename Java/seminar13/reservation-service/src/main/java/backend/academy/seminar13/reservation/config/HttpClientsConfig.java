package backend.academy.seminar13.reservation.config;

import backend.academy.seminar13.reservation.client.HotelService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientsConfig {

    @Bean
    public HotelService hotelService(RestClient.Builder clientBuilder) {
        var adapter = RestClientAdapter.create(clientBuilder.baseUrl("http://localhost:8080").build());
        var factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(HotelService.class);
    }
}
