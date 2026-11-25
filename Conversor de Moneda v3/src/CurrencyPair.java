public enum CurrencyPair {
    USD_CRC(1, "USD", "CRC"),
    CRC_USD(2, "CRC", "USD"),
    BRL_CRC(3, "BRL", "CRC"),
    CRC_BRL(4, "CRC", "BRL"),
    ARS_USD(5, "ARS", "USD"),
    USD_ARS(6, "USD", "ARS");

    private final int option;
    private final String from;
    private final String to;

    CurrencyPair(int option, String from, String to) {
        this.option = option;
        this.from = from;
        this.to = to;
    }

    public int getOption() {
        return option;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public static CurrencyPair fromOption(int option) {
        for (CurrencyPair pair : values()) {
            if (pair.option == option) {
                return pair;
            }
        }
        return null;
    }
}
