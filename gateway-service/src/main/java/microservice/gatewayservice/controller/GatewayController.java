package microservice.gatewayservice.controller;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class GatewayController {
    private final DiscoveryClient discovery;
    private final RestClient client;

    public GatewayController(DiscoveryClient discovery) {
        this.discovery = discovery;
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.client = RestClient.builder().requestFactory(factory).build();
    }

    @PostMapping
    public ResponseEntity<byte[]> create(@RequestBody String body) {
        return forward(HttpMethod.POST, "/orders", body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> find(@PathVariable UUID id) {
        return forward(HttpMethod.GET, "/orders/" + id, null);
    }

    private ResponseEntity<byte[]> forward(HttpMethod method, String path, String body) {
        var instances = discovery.getInstances("order-service");
        if (instances.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Order service is unavailable");
        }
        var request = client.method(method).uri(instances.getFirst().getUri().resolve(path));
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).body(body);
        }
        try {
            return request.exchange((req, response) -> {
                HttpHeaders headers = new HttpHeaders();
                if (response.getHeaders().getContentType() != null) {
                    headers.setContentType(response.getHeaders().getContentType());
                }
                URI location = response.getHeaders().getLocation();
                if (location != null && location.getPath().startsWith("/orders/")) {
                    headers.setLocation(URI.create("/api" + location.getPath()));
                }
                return new ResponseEntity<>(response.getBody().readAllBytes(), headers, response.getStatusCode());
            });
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cannot reach order service");
        }
    }
}
