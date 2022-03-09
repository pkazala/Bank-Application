package uk.co.asepstrath.bank;

import io.jooby.Jooby;
import io.jooby.OpenAPIModule;
import io.jooby.handlebars.HandlebarsModule;
import io.jooby.helper.UniRestExtension;
import io.jooby.hikari.HikariModule;
import io.jooby.json.JacksonModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RepeatTest extends Jooby {

    private DataSource ds;
    private Logger log;
    private Dataface df;

    private float findAccountBalanceSQL(String accID) {

        float balance = 0;

        try (Connection connection = ds.getConnection()) {

            /*
            Finds the account from the table where the id matches the given account
             */
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM Accounts WHERE id = '" + accID + "'";
            ResultSet rs = stmt.executeQuery(sql);

            /*
            Finds the balance in the database of the given account
             */
            while (rs.next()) {
                balance = rs.getFloat("balance");
            }
            rs.close();

        } catch (SQLException e) {
            log.error("Database Creation Error", e);
        }
        return balance;
    }

    @BeforeAll
    public void setUp() {

        install(new UniRestExtension());
        install(new HandlebarsModule());
        install(new HikariModule("mem"));
        install(new JacksonModule());
        install(new OpenAPIModule());

        ds = require(DataSource.class);
        log = getLog();
        df = new Dataface(ds, log);

        String success = df.initialiseData();

    }

    /*
    Test to check what happens if an invalid ID is passed into the method
    It should exit returning the string "Could not find given transaction."
     */
    @Test
    public void invalidID(){
        String success = RepeatTransaction.repeatTransaction(log, ds, "bad-id");
        Assertions.assertEquals("Could not find given transaction.", success);
    }

    /*
    Test to check what happens if a valid ID for a transaction between two accounts within our bank is passed into the method
    The accounts involved in the transaction should both be found to be in our bank using the findFromOurBank method
    The values of these accounts in the SQL database after processing them should have the amount added on or subtracted
    After repeating the transaction, it should exit returning the string "Transaction repeated successfully."
     */
    @Test
    public void validIDBothInBank(){

        try (Connection connection = ds.getConnection()) {

            /*
            Finds information about this transaction to check if both accounts are in our bank as they should be,
            and to find information about those accounts to see their balances
            Since both accounts are in our bank, they should both not be null
             */
            Transaction found = RepeatTransaction.findTransactionSQL(ds, log,"001aa34a-7362-4138-85ea-e735c4580398");

            /*
            Repeats transaction with ID 001aa34a-7362-4138-85ea-e735c4580398#
            This is known to be a valid transaction between two accounts in our bank
            The message should be "Transaction repeated successfully." if everything was correct
             */
            String success = RepeatTransaction.repeatTransaction(log, ds, "001aa34a-7362-4138-85ea-e735c4580398");//001aa34a-7362-4138-85ea-e735c4580398");
            Assertions.assertEquals("Transaction repeated successfully.", success);


            Account[] accounts = RepeatTransaction.findFromOurBank(found, connection.createStatement());
            Assertions.assertNotEquals(null, accounts[0]);
            Assertions.assertNotEquals(null, accounts[1]);

            float acc1Balance = 0;
            float acc2Balance = 0;
            /*
            Neither account should be null, but checks just to make sure
             */
            if (accounts[0] != null){
                acc1Balance = findAccountBalanceSQL(accounts[0].getId());
            }
            if (accounts[1] != null){
                acc2Balance = findAccountBalanceSQL(accounts[1].getId());
            }

            System.out.println("ACC1 NOW: " + acc1Balance);
            System.out.println("ACC2 NOW: " + acc2Balance);

            /*
            Checks if the values in the accounts match the value before the transaction took place
            172671.06 was the amount before money was withdrawn, so that amount is subtracted again
            208267.20 was the amount before money was deposited, so that amount is added again
             */
            Assertions.assertEquals(172671.06f - found.getAmount(), acc1Balance);
            Assertions.assertEquals(208267.20f + found.getAmount(), acc2Balance);
        }
        catch (SQLException e) {
            log.error("Database Creation Error", e);
        }
    }

    /*
    Test to check what happens if a valid ID for a transaction where only one account is in our bank.
    In this case that is the one the money was taken from and needs to be taken out a second time
    The withdraw account involved in the transaction should be found to be in our bank using the findFromOurBank method, but the other should not
    The values of this account in the SQL database after processing it should have the amount subtracted again
    After repeating the transaction, it should exit returning the string "Transaction repeated successfully. 202 - Accepted."
     */
    @Test
    public void validIDWithdrawFromBank(){

        try (Connection connection = ds.getConnection()) {

            /*
            Finds information about this transaction to check if only the withdraw account is in our bank as it should be,
            and to find information about the account to see its balance
            Since only the first account is in our bank, that one should not be null
             */
            Transaction found = RepeatTransaction.findTransactionSQL(ds, log, "00167166-14e2-4aae-9f9c-ed6e1b58d745");

            /*
            Repeats transaction with ID 00167166-14e2-4aae-9f9c-ed6e1b58d745
            This is known to be a valid transaction with only the one sending money being in our bank
            The message should be "Transaction repeated successfully." if everything was correct
             */
            String success = RepeatTransaction.repeatTransaction(log, ds, "00167166-14e2-4aae-9f9c-ed6e1b58d745");
            Assertions.assertEquals("Transaction repeated successfully.", success);

            Account[] accounts = RepeatTransaction.findFromOurBank(found, connection.createStatement());
            Assertions.assertNotEquals(null, accounts[0]);
            Assertions.assertEquals(null, accounts[1]);

            float accBalance = 0;
            /*
            This account shouldn't be null, but checks just to make sure
             */
            if (accounts[0] != null){
                accBalance = findAccountBalanceSQL(accounts[0].getId());
            }

            System.out.println("ACC NOW: " + accBalance);

            /*
            Checks if the values in the account has the amount subtracted again
            939119.20f was the amount before money was withdrawn, so the amount should be subtracted
            */
            Assertions.assertEquals( 939119.20f - found.getAmount(), accBalance);
        }
        catch (SQLException e) {
            log.error("Database Creation Error", e);
        }
    }

    /*
    Test to check what happens if a valid ID for a transaction where only one account is in our bank.
    In this case that is the one the money put into and needs to be put into a second time
    The deposit account involved in the transaction should be found to be in our bank using the findFromOurBank method, but the other should not
    The values of this account in the SQL database after processing it should have the amount added back on
    After reversing the transaction, it should exit returning the string "Transaction repeated successfully."
     */
    @Test
    public void validIDDepositFromBank(){

        try (Connection connection = ds.getConnection()) {

            /*
            Finds information about this transaction to check if only the deposit account is in our bank as it should be,
            and to find information about the account to see its balance
            Since only the second account is in our bank, that one should not be null
             */
            Transaction found = RepeatTransaction.findTransactionSQL(ds, log, "0004cd6f-415f-46e7-986e-45e7eb9e8705");

            /*
            Repeats transaction with ID 0004cd6f-415f-46e7-986e-45e7eb9e8705
            This is known to be a valid transaction with only the one receiving money being in our bank
            The message should be "Transaction repeated successfully." if everything was correct
             */
            String success = RepeatTransaction.repeatTransaction(log, ds, "0004cd6f-415f-46e7-986e-45e7eb9e8705");
            Assertions.assertEquals("Transaction repeated successfully.", success);


            Account[] accounts = RepeatTransaction.findFromOurBank(found, connection.createStatement());
            Assertions.assertEquals(null, accounts[0]);
            Assertions.assertNotEquals(null, accounts[1]);

            float accBalance = 0;
            /*
            This account shouldn't be null, but checks just to make sure
             */
            if (accounts[1] != null){
                accBalance = findAccountBalanceSQL(accounts[1].getId());
            }

            System.out.println("ACC NOW: " + accBalance);

            /*
            Checks if the values in the account matches the value before the transaction took place
            26920.88f was the amount before money was added, so the value should have the money added again
            */
            Assertions.assertEquals( 26920.88f + found.getAmount(), accBalance);
        }
        catch (SQLException e) {
            log.error("Database Creation Error", e);
        }
    }

    /*
    Test to check what happens if when trying to repeat a transaction the account can't afford to do it a second time
    It should exit returning the string "Could not afford to repeat transaction."
     */
    @Test
    public void cantAfford(){

        try (Connection connection = ds.getConnection()) {
            /*
            Finds information about this transaction to find the accounts involved
            This is so I can edit the value of the account's balance to test what happens if they can't afford it
             */
            Transaction found = RepeatTransaction.findTransactionSQL(ds, log, "001aa34a-7362-4138-85ea-e735c4580398");
            Account[] accounts = RepeatTransaction.findFromOurBank(found, connection.createStatement());

            Account depositAcc = accounts[0];

            System.out.println("OLD BALANCE: " + findAccountBalanceSQL(accounts[0].getId()));

            /*
            Updates the balance to less than the amount that is being deposited
             */
            Statement stmt = connection.createStatement();
            String sql = "UPDATE Accounts SET balance = " + (0.5 * found.getAmount())+ " WHERE id = '" + depositAcc.getId() + "'";
            stmt.executeUpdate(sql);

            System.out.println("NEW BALANCE: " + findAccountBalanceSQL(accounts[0].getId()));

            /*
            Tries to make the repeated transaction, but it should fail
            It should exit returning the string "Could not afford to repeat transaction."
             */
            String success = RepeatTransaction.repeatTransaction(log, ds, "001aa34a-7362-4138-85ea-e735c4580398");
            Assertions.assertEquals("Could not afford to repeat transaction.", success);
        }
        catch (SQLException e) {
            log.error("Database Creation Error", e);
        }

    }

}