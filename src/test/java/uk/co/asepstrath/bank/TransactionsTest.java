package uk.co.asepstrath.bank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TransactionsTest {

    @Test
    public void transactionsGetBalanceBefore() {
        Transactions test = new Transactions(22.65f, 26.43f, 17, 3);
        Assertions.assertEquals(test.getBalanceBefore(), 22.65f);
    }

    @Test
    public void transactionsGetBalanceAfter() {
        Transactions test = new Transactions(22.65f, 26.43f, 17, 3);
        Assertions.assertEquals(test.getBalanceAfter(), 26.43f);
    }

    @Test
    public void transactionsGetNoOfTransactions() {
        Transactions test = new Transactions(22.65f, 26.43f, 17, 3);
        Assertions.assertEquals(test.getNoOfTransactions(), 17);
    }

    @Test
    public void transactionsGetNoOfFailedTransactions() {
        Transactions test = new Transactions(22.65f, 26.43f, 17, 3);
        Assertions.assertEquals(test.getNoOfFailedTransactions(), 3);
    }
}
