package sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class BackgroundClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundClient.class);

    private WebClient webClient;

    public BackgroundClient(WebClient.Builder webClient, ClientRegistrationRepository clientRegistrationRepository) {
        //ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 = getAnonHttpServlet(clientRegistrationRepository);
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 = getNoOpOauth2(clientRegistrationRepository);
        oauth2.setDefaultClientRegistrationId("mock");
        this.webClient = webClient.baseUrl("http://localhost:8080/").apply(oauth2.oauth2Configuration()).build();
    }

    private ServletOAuth2AuthorizedClientExchangeFilterFunction getNoOpOauth2(ClientRegistrationRepository clientRegistrationRepository) {
        return new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository,
                new OAuth2AuthorizedClientRepository() {
                    @Override
                    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String s, Authentication authentication, HttpServletRequest httpServletRequest) {
                        LOGGER.info("Load Client Called with args - {}. {}. {}", s, authentication, httpServletRequest);
                        return null;
                    }

                    @Override
                    public void saveAuthorizedClient(OAuth2AuthorizedClient oAuth2AuthorizedClient, Authentication authentication, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
                        LOGGER.info("Save client called with args - {}, {}", oAuth2AuthorizedClient, authentication);
                    }

                    @Override
                    public void removeAuthorizedClient(String s, Authentication authentication, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
                        LOGGER.info("Remove client called with args - {}, {}", s, authentication);
                    }
                });
    }

    private ServletOAuth2AuthorizedClientExchangeFilterFunction getAnonHttpServlet(ClientRegistrationRepository clientRegistrationRepository) {
        return new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository,
                new HttpSessionOAuth2AuthorizedClientRepository());
    }


    @Scheduled(fixedRate = 10000L)
    public void callApi() {
        String response = webClient.get().retrieve().bodyToMono(String.class).block();
        LOGGER.info("Got Response - {}", response);
    }
}
