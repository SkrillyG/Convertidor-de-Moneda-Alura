public final class CurrencyCalculator {
    private CurrencyCalculator(){}
    public static boolean isValidAmount(double amount) {
        return Double.isFinite(amount) && amount > 0;
    }

    public static double convert(double amount, double rate) {
        return amount * rate;
    }
}
