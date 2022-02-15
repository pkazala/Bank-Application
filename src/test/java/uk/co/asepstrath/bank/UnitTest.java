package uk.co.asepstrath.bank;

import io.jooby.MockRouter;
import io.jooby.StatusCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnitTest {
    /*
    Unit tests should be here
    Example can be found in example/UnitTest.java
     */
    @Test
    public void createAccount(){
        Account account = new Account();
        Assertions.assertTrue(account != null);
    }

    @Test
    //This test checks if the account has the starting amount 0 by default
    public void startingSimple(){
        Account account = new Account();
        Assertions.assertTrue(account.getBalance() == 0);
    }

    @Test
    //Depositing £50 in an account with £20 should result in an account containing £70
    public void addingFunds() {
        Account account = new Account();
        account.deposit(50);
        account.deposit(20);
        Assertions.assertTrue(account.getBalance() == 70);
    }

    @Test
    //Withdrawing £20 from an account with £40 should result in an account containing £20
    public void spendingSpree(){
        Account account = new Account();
        account.deposit(40);
        account.withdraw(20);
        Assertions.assertEquals(account.getBalance(),20);
    }

    @Test
    //Checking if the system reports an error if the user overdraws
    public void noOverdraft() {
        Account account = new Account(30.00f);
        ArithmeticException thrown = Assertions.assertThrows(ArithmeticException.class, () -> {
            account.withdraw(100);
        });
        Assertions.assertTrue(thrown.getMessage().contains("Amount to withdraw exceeds balance."));
    }

    @Test
    //Starting with an account with £20, deposit £10 five times then withdraw £20 three times
    // The account should end with £10
    public void superSaving() {
        Account account = new Account(20.0f);
        for(int i =0; i<5;i++){
            account.deposit(10);
        }
        for(int i =0; i<3;i++){
            account.withdraw(20);
        }
        Assertions.assertTrue(account.getBalance() == 10);
    }

    @Test
    //Checking if we keep track of pennies correctly
    public void takeCareOfPennies() {
        Account account = new Account(5.45f);
        account.deposit(17.56);
        Assertions.assertTrue(account.getBalance() == 23.01f);
    }
}
