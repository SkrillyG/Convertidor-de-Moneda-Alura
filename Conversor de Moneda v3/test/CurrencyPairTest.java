import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CurrencyPairTest {

    @Test
    void fromOptionReturnsTheExpectedPair() {
        assertEquals(CurrencyPair.USD_CRC, CurrencyPair.fromOption(1));
        assertEquals(CurrencyPair.CRC_USD, CurrencyPair.fromOption(2));
        assertEquals(CurrencyPair.USD_ARS, CurrencyPair.fromOption(6));
    }

    @Test
    void fromOptionReturnsNullForInvalidOption() {
        assertNull(CurrencyPair.fromOption(0));
        assertNull(CurrencyPair.fromOption(7));
        assertNull(CurrencyPair.fromOption(99));
    }

    @Test
    void pairKeepsItsCurrencyCodes() {
        assertEquals("BRL", CurrencyPair.BRL_CRC.getFrom());
        assertEquals("CRC", CurrencyPair.BRL_CRC.getTo());
    }
}
