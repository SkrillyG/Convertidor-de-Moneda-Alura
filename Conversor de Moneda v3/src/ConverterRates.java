import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class ConverterRates {
    private static final NumberFormat NUMBER = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CR"));

    private final ExchangeRateClient client;

    public ConverterRates(String apiKey) {
        this(new ExchangeRateClient(apiKey));
    }

    public ConverterRates(ExchangeRateClient client) {
        this.client = client;
    }

    public String convert(String fromCurrency, String toCurrency, Scanner reading) {
        ExchangeRateClient.RateData data = client.getRates(fromCurrency);
        if (data == null) {
            return null;
        }
        Double rate = data.rates().get(toCurrency);
        if (rate == null) {
            System.out.println("[!] No hay tasa de conversión para " + toCurrency);
            return null;
        }

        System.out.print("Cantidad a convertir: ");
        double amount;
        try {
            amount = Double.parseDouble(reading.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[!] Cantidad inválida. Escribe un número, por ejemplo: 100");
            return null;
        }
        if (!CurrencyCalculator.isValidAmount(amount)) {
            System.out.println("[!] La cantidad debe ser mayor que 0.");
            return null;
        }

        double converted = CurrencyCalculator.convert(amount, rate);
        String line = NUMBER.format(amount) + " " + fromCurrency + " equivale a " + NUMBER.format(converted) + " " + toCurrency;
        System.out.println();
        System.out.println("  " + line);
        System.out.println("  Tasa: 1 " + fromCurrency + " = " + NUMBER.format(rate) + " " + toCurrency);
        System.out.println("  Datos actualizados: " + (data.lastUpdate() == null ? "desconocido" : data.lastUpdate()));
        System.out.println();
        return line;
    }

    public Map<String, String> supportedCodes() {
        return client.getSupportedCodes();
    }
}
