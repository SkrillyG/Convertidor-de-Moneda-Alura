import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Scanner;

public class ConverterRates {
    private static final String API_BASE_URL = "https://v6.exchangerate-api.com/v6/";
    private final String apiKey;

    public ConverterRates(String apiKey) {
        this.apiKey = apiKey;
    }

    public void changesCoin(String currencyOfOri, String targetCurrency) {
        try {
            URI address = buildURI(currencyOfOri);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(address)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Gson gson = new Gson();
                targetCurrency answer = gson.fromJson(response.body(), targetCurrency.class);

                if (answer != null && answer.getConversionRates() != null) {
                    Map<String, Double> conversionRates = answer.getConversionRates();

                    if (conversionRates.containsKey(targetCurrency)) {
                        double rate = conversionRates.get(targetCurrency);
                        Scanner reading = new Scanner(System.in);
                        System.out.print("Por favor, introduce la amount a convertir (" + currencyOfOri + " a " + targetCurrency + "): ");
                        double amount = reading.nextDouble();
                        double amountConverted = amount * rate;
                        System.out.println(amount + " " + currencyOfOri + " equivale a " + amountConverted + " " + targetCurrency);
                    } else {
                        System.out.println("No se encontró la rate de conversión para " + targetCurrency);
                    }
                } else {
                    System.out.println("Esta API no contiene los datos buscados.");
                }
            } else {
                System.out.println("¡Error! al realizar la solicitud. RAZON!!: " + response.statusCode());
            }
        } catch (IOException | InterruptedException | URISyntaxException e) {
            System.err.println("Error al realizar la solicitud: " + e.getMessage());
        }
    }

    private URI buildURI(String currencyOfOri) throws URISyntaxException {
        String address = API_BASE_URL + apiKey + "/latest/" + currencyOfOri;
        return new URI(address);
    }
}