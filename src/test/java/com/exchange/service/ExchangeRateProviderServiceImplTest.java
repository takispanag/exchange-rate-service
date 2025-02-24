package com.exchange.service;

import com.exchange.dto.AllExchangeRatesDto;
import com.exchange.dto.SingleExchangeRateDto;
import com.exchange.exception.ExchangeRateException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExchangeRateProviderServiceImplTest {

    private WireMockServer wireMockServer;
    private ExchangeRateProviderServiceImpl exchangeRateService;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + wireMockServer.port())
                .build();
        exchangeRateService = new ExchangeRateProviderServiceImpl(restClient);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Nested
    @DisplayName("getExchangeRate method tests")
    class GetExchangeRateTests {

        @Test
        @DisplayName("Should return exchange rate when API call is successful")
        void shouldReturnExchangeRateWhenApiCallIsSuccessful() {
            String sourceCurrency = "USD";
            String targetCurrency = "EUR";
            String rateKey = sourceCurrency + targetCurrency;
            double expectedRate = 0.85;

            wireMockServer.stubFor(get(urlPathEqualTo("/live"))
                    .withQueryParam("source", equalTo(sourceCurrency))
                    .withQueryParam("currencies", equalTo(targetCurrency))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(String.format(
                                    "{\"success\":true,\"quotes\":{\"%s\":%.2f}}",
                                    rateKey, expectedRate))));

            SingleExchangeRateDto result = exchangeRateService.getExchangeRate(sourceCurrency, targetCurrency);

            assertThat(result).isNotNull();
            assertThat(result.getSourceCurrency()).isEqualTo(sourceCurrency);
            assertThat(result.getTargetCurrency()).isEqualTo(targetCurrency);
            assertThat(result.getExchangeRate()).isEqualByComparingTo(BigDecimal.valueOf(expectedRate));
            assertThat(result.getTimestamp()).isNotNull();

            wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/live"))
                    .withQueryParam("source", equalTo(sourceCurrency))
                    .withQueryParam("currencies", equalTo(targetCurrency)));
        }

        @Test
        @DisplayName("Should throw exception when API returns unsuccessful response")
        void shouldThrowExceptionWhenApiReturnsUnsuccessfulResponse() {
            String sourceCurrency = "USD";
            String targetCurrency = "INVALID";

            wireMockServer.stubFor(get(urlPathEqualTo("/live"))
                    .withQueryParam("source", equalTo(sourceCurrency))
                    .withQueryParam("currencies", equalTo(targetCurrency))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"success\":false,\"error\":{\"code\":202,\"info\":\"Invalid currency\"}}")));

            ExchangeRateException exception = assertThrows(ExchangeRateException.class,
                    () -> exchangeRateService.getExchangeRate(sourceCurrency, targetCurrency));

            assertThat(exception.getMessage()).contains("Failed to fetch exchange rate");
        }

        @Test
        @DisplayName("Should throw exception when API returns server error")
        void shouldThrowExceptionWhenApiReturnsServerError() {
            String sourceCurrency = "USD";
            String targetCurrency = "EUR";

            wireMockServer.stubFor(get(urlPathEqualTo("/live"))
                    .willReturn(aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")));

            assertThrows(Exception.class,
                    () -> exchangeRateService.getExchangeRate(sourceCurrency, targetCurrency));
        }
    }

    @Nested
    @DisplayName("getAllRates method tests")
    class GetAllRatesTests {

        @Test
        @DisplayName("Should return all exchange rates when API call is successful")
        void shouldReturnAllExchangeRatesWhenApiCallIsSuccessful() {
            String baseCurrency = "USD";

            wireMockServer.stubFor(get(urlPathEqualTo("/live"))
                    .withQueryParam("source", equalTo(baseCurrency))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(
                                    "{\"success\":true,\"quotes\":{\"USDEUR\":0.85,\"USDGBP\":0.73,\"USDJPY\":110.22}}")));

            AllExchangeRatesDto result = exchangeRateService.getAllRates(baseCurrency);

            assertThat(result).isNotNull();
            assertThat(result.getSourceCurrency()).isEqualTo(baseCurrency);
            assertThat(result.getRates()).hasSize(3);
            assertThat(result.getRates()).containsEntry("USDEUR", BigDecimal.valueOf(0.85));
            assertThat(result.getRates()).containsEntry("USDGBP", BigDecimal.valueOf(0.73));
            assertThat(result.getRates()).containsEntry("USDJPY", BigDecimal.valueOf(110.22));
            assertThat(result.getTimestamp()).isNotNull();

            wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/live"))
                    .withQueryParam("source", equalTo(baseCurrency)));
        }

        @Test
        @DisplayName("Should throw exception when getAllRates API returns unsuccessful response")
        void shouldThrowExceptionWhenGetAllRatesApiReturnsUnsuccessfulResponse() {
            String baseCurrency = "INVALID";

            wireMockServer.stubFor(get(urlPathEqualTo("/live"))
                    .withQueryParam("source", equalTo(baseCurrency))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"success\":false,\"error\":{\"code\":201,\"info\":\"Invalid base currency\"}}")));

            ExchangeRateException exception = assertThrows(ExchangeRateException.class,
                    () -> exchangeRateService.getAllRates(baseCurrency));

            assertThat(exception.getMessage()).contains("Failed to fetch exchange rates");
        }

        @Test
        @DisplayName("Should throw exception when getAllRates API connection fails")
        void shouldThrowExceptionWhenGetAllRatesApiConnectionFails() {
            String baseCurrency = "USD";

            wireMockServer.stubFor(get(urlPathEqualTo("/live"))
                    .willReturn(aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")));

            assertThrows(Exception.class,
                    () -> exchangeRateService.getAllRates(baseCurrency));
        }
    }
}