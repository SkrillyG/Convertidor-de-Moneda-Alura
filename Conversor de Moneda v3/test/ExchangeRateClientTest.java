import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExchangeRateClientTest {
    private static final String KEY = "0123456789abcdef0123456789abcdef";

    private HttpServer server;
    private String baseUrl;
    private Path ratesCache;
    private Path codesCache;
    private AtomicInteger rateCalls;
    private AtomicInteger attempts;

    @BeforeEach
    void setUp() throws Exception {
        Path directory = Files.createTempDirectory("exchange-client-test");
        ratesCache = directory.resolve("rates.json");
        codesCache = directory.resolve("codes.json");
        rateCalls = new AtomicInteger();
        attempts = new AtomicInteger();

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            String path = exchange.getRequestURI().getPath();
            String body;
            int status = 200;
            if (!("Bearer " + KEY).equals(auth)) {
                status = 401;
                body = "{\"result\":\"error\",\"error-type\":\"invalid-key\"}";
            } else if (path.endsWith("/codes")) {
                body = "{\"result\":\"success\",\"supported_codes\":[[\"USD\",\"US Dollar\"],[\"CRC\",\"Costa Rican Colon\"]]}";
            } else {
                rateCalls.incrementAndGet();
                if (attempts.getAndIncrement() == 0) {
                    status = 503;
                    body = "{}";
                } else {
                    body = "{\"result\":\"success\",\"time_last_update_utc\":\"Tue, 24 Nov 2026 00:00:00 +0000\","
                            + "\"time_next_update_unix\":9999999999,\"base_code\":\"USD\","
                            + "\"conversion_rates\":{\"USD\":1.0,\"CRC\":500.0}}";
                }
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort() + "/v6/";
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void getsRatesWithBearerAuthentication() {
        ExchangeRateClient client = new ExchangeRateClient(KEY, baseUrl, ratesCache, codesCache);
        ExchangeRateClient.RateData data = client.getRates("USD");

        assertNotNull(data);
        assertEquals(500.0, data.rates().get("CRC"), 0.0001);
        assertEquals("Tue, 24 Nov 2026 00:00:00 +0000", data.lastUpdate());
        assertTrue(Files.exists(ratesCache));
    }

    @Test
    void rejectsInvalidKey() {
        ExchangeRateClient client = new ExchangeRateClient("clave-invalida", baseUrl, ratesCache, codesCache);
        assertNull(client.getRates("USD"));
    }

    @Test
    void readsSupportedCodes() {
        ExchangeRateClient client = new ExchangeRateClient(KEY, baseUrl, ratesCache, codesCache);
        assertEquals("US Dollar", client.getSupportedCodes().get("USD"));
        assertEquals("Costa Rican Colon", client.getSupportedCodes().get("CRC"));
        assertTrue(Files.exists(codesCache));
    }

    @Test
    void reusesMemoryCacheWithoutNewRequests() {
        ExchangeRateClient client = new ExchangeRateClient(KEY, baseUrl, ratesCache, codesCache);
        client.getRates("USD");
        int calls = rateCalls.get();
        client.getRates("USD");

        assertEquals(calls, rateCalls.get());
    }
}
