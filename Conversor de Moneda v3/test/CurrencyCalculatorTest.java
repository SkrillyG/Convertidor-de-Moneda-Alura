import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrencyCalculatorTest {

    @Test
    void convertMultipliesAmountByRate() {
        assertEquals(500.0, CurrencyCalculator.convert(100, 5.0), 0.0001);
        assertEquals(12.5, CurrencyCalculator.convert(5, 2.5), 0.0001);
    }

    @Test
    void isValidAmountRejectsInvalidValues() {
        assertFalse(CurrencyCalculator.isValidAmount(0));
        assertFalse(CurrencyCalculator.isValidAmount(-1));
        assertFalse(CurrencyCalculator.isValidAmount(Double.NaN));
        assertFalse(CurrencyCalculator.isValidAmount(Double.POSITIVE_INFINITY));
    }

    @Test
    void isValidAmountAcceptsPositiveValues() {
        assertTrue(CurrencyCalculator.isValidAmount(0.01));
        assertTrue(CurrencyCalculator.isValidAmount(100));
    }
}
