package uk.co.asepstrath.bank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TransactionTest {

    @Test
    public void transactionGetWithdrawAccount() {
        Transaction test = new Transaction("0062f74c-1fd6-4e0d-9213-285a56772069", "067c6b71-1903-472d-99e4-2c2f13b4ffce", "2019-12-13T10:29:34.857Z", "00000000-0000-0000-0000-000000000000", 123.45f, "GBP");
        Assertions.assertEquals(test.getWithdrawAccount(), "0062f74c-1fd6-4e0d-9213-285a56772069");
    }

    @Test
    public void transactionGetDepositAccount() {
        Transaction test = new Transaction("0062f74c-1fd6-4e0d-9213-285a56772069", "067c6b71-1903-472d-99e4-2c2f13b4ffce", "2019-12-13T10:29:34.857Z", "00000000-0000-0000-0000-000000000000", 123.45f, "GBP");
        Assertions.assertEquals(test.getDepositAccount(), "067c6b71-1903-472d-99e4-2c2f13b4ffce");
    }

    @Test
    public void transactionGetTimestamp() {
        Transaction test = new Transaction("0062f74c-1fd6-4e0d-9213-285a56772069", "067c6b71-1903-472d-99e4-2c2f13b4ffce", "2019-12-13T10:29:34.857Z", "00000000-0000-0000-0000-000000000000", 123.45f, "GBP");
        Assertions.assertEquals(test.getTimestamp(), "2019-12-13T10:29:34.857Z");
    }

    @Test
    public void transactionGetId() {
        Transaction test = new Transaction("0062f74c-1fd6-4e0d-9213-285a56772069", "067c6b71-1903-472d-99e4-2c2f13b4ffce", "2019-12-13T10:29:34.857Z", "00000000-0000-0000-0000-000000000000", 123.45f, "GBP");
        Assertions.assertEquals(test.getId(), "00000000-0000-0000-0000-000000000000");
    }

    @Test
    public void transactionGetAmount() {
        Transaction test = new Transaction("0062f74c-1fd6-4e0d-9213-285a56772069", "067c6b71-1903-472d-99e4-2c2f13b4ffce", "2019-12-13T10:29:34.857Z", "00000000-0000-0000-0000-000000000000", 123.45f, "GBP");
        Assertions.assertEquals(test.getAmount(), 123.45f);
    }

    @Test
    public void transactionGetCurrency() {
        Transaction test = new Transaction("0062f74c-1fd6-4e0d-9213-285a56772069", "067c6b71-1903-472d-99e4-2c2f13b4ffce", "2019-12-13T10:29:34.857Z", "00000000-0000-0000-0000-000000000000", 123.45f, "GBP");
        Assertions.assertEquals(test.getCurrency(), "GBP");
    }

    @Test
    public void transactionToString() {
        Transaction test = new Transaction("0062f74c-1fd6-4e0d-9213-285a56772069", "067c6b71-1903-472d-99e4-2c2f13b4ffce", "2019-12-13T10:29:34.857Z", "00000000-0000-0000-0000-000000000000", 123.45f, "GBP");
        String testString = "0062f74c-1fd6-4e0d-9213-285a56772069 067c6b71-1903-472d-99e4-2c2f13b4ffce 2019-12-13T10:29:34.857Z 00000000-0000-0000-0000-000000000000 123.45 GBP";
        Assertions.assertEquals(test.toString(), testString);
    }
}
