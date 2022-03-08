package uk.co.asepstrath.bank;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.*;

/*
This class has very similar functionality to ReverseTransaction, so it is going to be a subclass of it
 */
public class RepeatTransaction extends ReverseTransaction{

    
    /*
    Method to repeat a transaction between two accounts in our bank, given the Transaction object
    The correct amounts are then transferred in the same way as they were in the original transaction

    Will also repeat the sides of the transactions that ARE a part of this bank
    I'm not 100% sure it needs to check the banks, since nothing is mentioned about it in the API like it is for reversing a transaction
    but if it is repeating the transaction, it should probably update the values as well as adding a new transaction.
    But then again, there's nothing about notifying another bank or anything about it, or other banks at all, so I'm not sure

     */
    public static void repeatValues(Transaction toRepeat, Account[] accountsInBank, Statement stmt) throws SQLException {

        String sql;

        Account withdraw = accountsInBank[0];
        Account deposit = accountsInBank[1];

        /*
        Performs the opposite actions to what was in the original transaction, and updates their values in the database
        (or those that CAN be done by our bank)
         */

        if (withdraw != null) {
            System.out.println("OLD VALUES FOR 'withdraw': balance = " + withdraw.getBalance() + ", noOfTrans = " + withdraw.getNoOfTransactions());

            withdraw.withdraw(toRepeat.getAmount());
            withdraw.setNoOfTransactions(withdraw.getNoOfTransactions() + 1);

            /*
            If after withdrawing, the account would have less than 0 balance (i.e. they can't afford to repeat the transaction)
            then the transaction is cancelled and a repeated transaction is not added
             */
            if (withdraw.getBalance() < 0){
                return;
            }

            sql = "UPDATE Accounts SET balance = " + withdraw.getBalance() + "," + "noOfTransactions = " + withdraw.getNoOfTransactions() +
                    " WHERE id = '" + withdraw.getId() + "'";
            stmt.executeUpdate(sql);
        }

        if (deposit != null) {
            System.out.println("OLD VALUES FOR 'deposit': balance = " + deposit.getBalance() + ", noOfTrans = " + deposit.getNoOfTransactions());

            deposit.deposit(toRepeat.getAmount());
            deposit.setNoOfTransactions(deposit.getNoOfTransactions() + 1);

            sql = "UPDATE Accounts SET balance = " + deposit.getBalance() + "," + "noOfTransactions = " + deposit.getNoOfTransactions() +
                    " WHERE id = '" + deposit.getId() + "'";
            stmt.executeUpdate(sql);
        }

        /*
        Creates a new transaction of the repeated transaction
         */
        String currentTime = new Timestamp(System.currentTimeMillis()).toString();
        currentTime = currentTime.replace(" ","T");
        currentTime += "Z";

        sql = "INSERT INTO Transactions " + "VALUES ('" + toRepeat.getWithdrawAccount() + "', '"
                + toRepeat.getDepositAccount() + "', '" + currentTime + "', '"
                + toRepeat.getId() + "', '" + toRepeat.getAmount() + "', '" + toRepeat.getCurrency() + "')";
        stmt.executeUpdate(sql);

        System.out.println("Created new repeated transaction");

    }

    /*
    This method repeats a transaction given its ID
    Might be overcomplicated, may only need to add a new transaction to the database with the same information as the requested transaction
    Mine also updates the balances for the accounts in our bank with the new repeated transaction
    */
    public static void repeatTransaction(Logger log, DataSource ds, String transactionID) {

        try (Connection connection = ds.getConnection()) {

            /*
            Finds the transaction from the table where the id matches the transaction to be repeated
             */
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM Transactions WHERE id = '" + transactionID + "'";
            ResultSet rs = stmt.executeQuery(sql);

            boolean retrievedTransaction = false;

            /*
            Creates empty transaction so later code can run
             */
            Transaction toRepeat = new Transaction("", "", "", "", 0f, "");

            /*
            Goes through the information returned and creates a Transaction object storing this information
            retrievedTransaction will be set to true here if a transaction has been found
             */
            while (rs.next()) {

                retrievedTransaction = true;

                String wAccount = rs.getString("withdrawAccount");
                String dAccount = rs.getString("depositAccount");
                String tStamp = rs.getString("timestamp");
                String id = rs.getString("id");
                String amount = rs.getString("amount");
                String currency = rs.getString("currency");

                toRepeat = new Transaction(wAccount, dAccount, tStamp, id, Float.parseFloat(amount), currency);

                System.out.println("Transaction with ID '" + toRepeat.getId() + "':");
                System.out.println("Withdraw Account: " + toRepeat.getWithdrawAccount());
                System.out.println("Deposit Account: " + toRepeat.getDepositAccount());
                System.out.println("Amount: " + toRepeat.getAmount());
                System.out.println("Currency: " + toRepeat.getCurrency());
            }
            rs.close();

            /*
            If the transaction is found, checks to see if the two accounts involved are both from our bank
            to see which ones can have their values changed
             */
            if (retrievedTransaction) {

                System.out.println("Transaction found!");

                /*
                Find which of the 2 accounts are from our bank
                Stores their query results in this list since they are queried already
                 */
                Account[] accountsInBank = findFromOurBank(toRepeat, stmt);

                /*
                If either bank isn't from our bank, one must belong to another bank
                 */
                if (accountsInBank[0] == null || accountsInBank[1] == null) {
                    System.out.println("One of the accounts not in our bank.");
                    repeatValues(toRepeat, accountsInBank, stmt);

                }
                else{
                    /*
                    Repeats the transaction in our bank
                     */
                    System.out.println("Both in our bank!");
                    repeatValues(toRepeat, accountsInBank, stmt);
                }

            } else {
                log.info("Could not find given transaction.");
                return;
            }

        } catch (SQLException e) {
            log.error("Database Creation Error", e);
        }
    }

}
