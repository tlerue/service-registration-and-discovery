package passport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.FeignConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@SpringBootApplication
@EnableEurekaClient
public class Application extends FeignConfiguration {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .web(false)
                .run(args);
    }

    @Bean
    BookmarkClient bookmarkClient() {
        return loadBalance(BookmarkClient.class, "http://bookmark-service");
    }
}

@Component
class DiscoveryClientExample implements CommandLineRunner {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Override
    public void run(String... strings) throws Exception {
        List<ServiceInstance> photoServices = discoveryClient.getInstances("photo-service");
        photoServices.forEach(System.out::println);

        List<ServiceInstance> bookmarkServices = discoveryClient.getInstances("bookmark-service");
        bookmarkServices.forEach(System.out::println);
    }
}

@Component
class RestTemplateExample implements CommandLineRunner {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void run(String... strings) throws Exception {
        // use the "smart" Eureka-aware RestTemplate
        ResponseEntity<List<Bookmark>> exchange =
                this.restTemplate.exchange(
                        "http://bookmark-service/{userId}/bookmarks",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Bookmark>>() {
                        },
                        (Object) "mstine");

        exchange.getBody().forEach(System.out::println);
    }

}

@Component
class FeignExample implements CommandLineRunner {

    @Autowired
    private BookmarkClient bookmarkClient;

    @Override
    public void run(String... strings) throws Exception {
        this.bookmarkClient.getBookmarks("jlong").forEach(System.out::println);
    }
}

interface BookmarkClient {

    @RequestMapping(method = RequestMethod.GET, value = "/{userId}/bookmarks")
    List<Bookmark> getBookmarks(@PathVariable("userId") String userId);
}

class Bookmark {
    private Long id;
    private String href, label, description, userId;

    @Override
    public String toString() {
        return "Bookmark{" +
                "id=" + id +
                ", href='" + href + '\'' +
                ", label='" + label + '\'' +
                ", description='" + description + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    public Bookmark() {
    }

    public Long getId() {
        return id;
    }

    public String getHref() {
        return href;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getUserId() {
        return userId;
    }
}

