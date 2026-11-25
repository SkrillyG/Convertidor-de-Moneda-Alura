import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExchangeRateClient {
    public record RateData(Map<String, Double> rates, String lastUpdate) {
    }

    private static final Logger LOG = Logger.getLogger(ExchangeRateClient.class.getName());
    private static final String DEFAULT_BASE_URL = "https://v6.exchangerate-api.com/v6/";
    private static final String DEFAULT_KEY = "test";

    private final String apiKey;
    private final String baseUrl;
    private final Path ratesCache;
    private final Path codesCache;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    private Map<String, Double> memoryRates;
    private String memoryBase;
    private String memoryLastUpdate;
    private long memoryUntil;
    private Map<String, String> supportedCodes;

    public ExchangeRateClient(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL, defaultPath(".convertidor-moneda-cache.json"), defaultPath(".convertidor-moneda-codes.json"));
    }

    public ExchangeRateClient(String apiKey, String baseUrl) {
        this(apiKey, baseUrl, defaultPath(".convertidor-moneda-cache.json"), defaultPath(".convertidor-moneda-codes.json"));
    }

    public ExchangeRateClient(String apiKey, String baseUrl, Path ratesCache, Path codesCache) {
        this.apiKey = apiKey == null || apiKey.isBlank() ? DEFAULT_KEY : apiKey;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.ratesCache = ratesCache;
        this.codesCache = codesCache;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    private static Path defaultPath(String name) {
        return Path.of(System.getProperty("user.home"), name);
    }

    public RateData getRates(String base) {
        long now = Instant.now().getEpochSecond();
        if (memoryRates != null && base.equals(memoryBase) && now < memoryUntil) {
            return new RateData(memoryRates, memoryLastUpdate);
        }

        TargetCurrency cached = readCache(ratesCache, TargetCurrency.class);
        if (cached != null && "success".equals(cached.getResult()) && base.equals(cached.getBaseCode())
                && cached.getConversionRates() != null && cached.getTimeNextUpdateUnix() > now) {
            return remember(base, cached.getConversionRates(), cached.getTimeLastUpdateUtc(), cached.getTimeNextUpdateUnix());
        }

        TargetCurrency response = fetch("latest/" + base, TargetCurrency.class);
        if (response == null || !"success".equals(response.getResult()) || response.getConversionRates() == null) {
            return null;
        }
        writeCache(ratesCache, response);
        return remember(base, response.getConversionRates(), response.getTimeLastUpdateUtc(), response.getTimeNextUpdateUnix());
    }

    public Map<String, String> getSupportedCodes() {
        if (supportedCodes != null) {
            return supportedCodes;
        }
        SupportedCodesResponse cached = readCache(codesCache, SupportedCodesResponse.class);
        if (cached != null && "success".equals(cached.getResult()) && cached.getSupportedCodes() != null) {
            supportedCodes = cached.toMap();
            return supportedCodes;
        }
        SupportedCodesResponse response = fetch("codes", SupportedCodesResponse.class);
        if (response == null || !"success".equals(response.getResult()) || response.getSupportedCodes() == null) {
            return Collections.emptyMap();
        }
        supportedCodes = response.toMap();
        writeCache(codesCache, response);
        return supportedCodes;
    }

    private RateData remember(String base, Map<String, Double> rates, String lastUpdate, long until) {
        memoryBase = base;
        memoryRates = rates;
        memoryLastUpdate = lastUpdate;
        memoryUntil = until;
        return new RateData(rates, lastUpdate);
    }

    private <T> T fetch(String path, Class<T> type) {
        for (int attempt = 0; attempt <= 2; attempt++) {
            try {
                URI address = new URI(baseUrl + path);
                HttpRequest request = HttpRequest.newBuilder(address)
                        .header("Authorization", "Bearer " + apiKey)
                        .GET()
                        .timeout(Duration.ofSeconds(10))
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                if (status == 200) {
                    return gson.fromJson(response.body(), type);
                }
                if ((status == 429 || status >= 500) && attempt < 2) {
                    Thread.sleep(200L * (attempt + 1));
                    continue;
                }
                System.out.println("[✗] " + describe(status));
                return null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.log(Level.WARNING, "Consulta interrumpida", e);
                return null;
            } catch (IOException | URISyntaxException e) {
                LOG.log(Level.WARNING, "Error de red", e);
                System.out.println("[✗] No se pudo conectar con la API: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    private String describe(int status) {
        if (status == 400 || status == 401 || status == 403 || status == 404) {
            return "API key inválida o sin permisos (HTTP " + status + ")";
        }
        if (status == 429) {
            return "Cuota de la API agotada (HTTP 429)";
        }
        if (status >= 500) {
            return "Servicio de tasas no disponible (HTTP " + status + ")";
        }
        return "Error HTTP " + status;
    }

    private <T> T readCache(Path path, Class<T> type) {
        try {
            if (!Files.exists(path)) {
                return null;
            }
            return gson.fromJson(Files.readString(path), type);
        } catch (IOException | RuntimeException e) {
            LOG.log(Level.FINE, "Cache no legible", e);
            return null;
        }
    }

    private void writeCache(Path path, Object value) {
        try {
            Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
            Files.writeString(temporary, gson.toJson(value));
            try {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOG.log(Level.FINE, "No se pudo guardar la cache", e);
        }
    }
}
