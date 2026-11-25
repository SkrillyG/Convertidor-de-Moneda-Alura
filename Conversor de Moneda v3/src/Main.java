import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final String KEY_PATTERN = "(?i)[0-9a-f]{24,32}";

    public static void main(String[] args) {
        String apiKey = System.getenv("EXCHANGE_RATE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("[✗] Falta EXCHANGE_RATE_API_KEY. Configúrala antes de ejecutar.");
            return;
        }
        if (!apiKey.matches(KEY_PATTERN)) {
            System.err.println("[!] La API key debe tener entre 24 y 32 caracteres hexadecimales.");
            return;
        }

        ConverterRates converter = new ConverterRates(apiKey);
        List<String> history = new ArrayList<>();

        try (Scanner reading = new Scanner(System.in)) {
            boolean salir = false;
            while (!salir) {
                showMenu();
                System.out.print("Opción: ");
                int option;
                try {
                    option = Integer.parseInt(reading.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("[!] Escribe un número del 1 al 9.");
                    continue;
                }

                if (option == 9) {
                    salir = true;
                    continue;
                }
                if (option == 8) {
                    showHistory(history);
                    continue;
                }

                String from;
                String to;
                if (option == 7) {
                    Map<String, String> codes = converter.supportedCodes();
                    if (codes.isEmpty()) {
                        System.out.println("[!] No se pudo cargar el catálogo de monedas.");
                        continue;
                    }
                    showCodes(codes);
                    from = askCode(reading, "Moneda origen", codes);
                    if (from == null) {
                        continue;
                    }
                    to = askCode(reading, "Moneda destino", codes);
                    if (to == null) {
                        continue;
                    }
                } else {
                    CurrencyPair pair = CurrencyPair.fromOption(option);
                    if (pair == null) {
                        System.out.println("[!] Opción incorrecta. Elige del 1 al 9.");
                        continue;
                    }
                    from = pair.getFrom();
                    to = pair.getTo();
                }

                String result = converter.convert(from, to, reading);
                if (result != null) {
                    history.add(result);
                }
            }
        }

        System.out.println("Gracias por utilizar el Converter de Monedas de AluraLatam!");
    }

    private static String askCode(Scanner reading, String label, Map<String, String> codes) {
        System.out.print(label + " (ej. USD): ");
        String code = reading.nextLine().trim().toUpperCase(Locale.ROOT);
        if (!codes.containsKey(code)) {
            System.out.println("[!] Código \"" + code + "\" no soportado.");
            return null;
        }
        return code;
    }

    private static void showHistory(List<String> history) {
        if (history.isEmpty()) {
            System.out.println("[!] Aún no hay conversiones en esta sesión.");
            return;
        }
        System.out.println("--- Historial de la sesión ---");
        for (int i = 0; i < history.size(); i++) {
            System.out.println((i + 1) + ". " + history.get(i));
        }
        System.out.println();
    }

    private static void showCodes(Map<String, String> codes) {
        System.out.println("--- Monedas disponibles ---");
        StringBuilder line = new StringBuilder();
        int column = 0;
        for (Map.Entry<String, String> entry : codes.entrySet()) {
            line.append(String.format("%-5s %-24s", entry.getKey(), entry.getValue()));
            if (++column == 3) {
                System.out.println(line);
                line.setLength(0);
                column = 0;
            }
        }
        if (line.length() > 0) {
            System.out.println(line);
        }
        System.out.println();
    }

    private static void showMenu() {
        System.out.println("""
            ╔══════════════════════════════════════════════════╗
            ║   CONVERTIDOR DE MONEDA — ALURA LATAM            ║
            ╚══════════════════════════════════════════════════╝
             1. USD  → CRC      2. CRC  → USD
             3. BRL  → CRC      4. CRC  → BRL
             5. ARS  → USD      6. USD  → ARS
             7. Otros pares (catálogo completo)
             8. Ver historial de la sesión
             9. Salir
            ====================================================
            """);
    }
}
