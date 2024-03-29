package uk.co.asepstrath.bank;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.*;

public class ReverseTransaction {

    public static Transaction findTransactionSQL(DataSource ds, Logger log, String transID){

        Transaction foundTransaction = null;

        try (Connection connection = ds.getConnection()) {

            /*
            Finds the transaction from the table where the id matches the transaction given
             */
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM Transactions WHERE id = '" + transID + "'";
            ResultSet rs = stmt.executeQuery(sql);

            /*
            Goes through the information returned and creates a Transaction object storing this information
            retrievedTransaction will be set to true here if a transaction has been found
             */
            while (rs.next()) {

                String wAccount = rs.getString("withdrawAccount");
                String dAccount = rs.getString("depositAccount");
                String tStamp = rs.getString("timestamp");
                String id = rs.getString("id");
                String amount = rs.getString("amount");
                String currency = rs.getString("currency");

                foundTransaction = new Transaction(wAccount, dAccount, tStamp, id, Float.parseFloat(amount), currency);
            }
            rs.close();
        }
        catch (SQLException e) {
            log.error("Database Creation Error", e);
        }
        return foundTransaction;
    }


    /*
    Method to find which of the two accounts involved in a transaction are in our bank
     */
    public static Account[] findFromOurBank(Transaction toReverse, Statement stmt) throws SQLException {
        /*
        loops through both the withdraw account id and the deposit account id
         */
        Account[] accountsInBank = {null,null};
        String[] accs = {toReverse.getWithdrawAccount(), toReverse.getDepositAccount()};

        for (int i = 0; i < 2; i++) {

            String sql = "SELECT * FROM Accounts WHERE id = '" + accs[i] + "'";
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("Checking if account " + accs[i] + " in our bank");

            /*
            When there is data returned, it gets the data and creates an account object which is added to the list
             */
            while (rs.next()) {
                Account tempAcc = new Account(rs.getString("id"),
                        rs.getString("name"),
                        rs.getFloat("balance"),
                        rs.getString("currency"),
                        rs.getString("accountType"),
                        rs.getInt("noOfTransactions"),
                        rs.getInt("noOfFailedTransactions"));
                System.out.println(tempAcc.toString());

                accountsInBank[i] = tempAcc;
                System.out.println("Account " + accs[i] + " IS in our bank!");
            }
        }

        return accountsInBank;

    }

    /*
    Method to reverse a transaction between two accounts in our bank, given the Transaction object
    I am a bit unsure on if I am going about this the right way, but my interpretation is that you delete the transaction from the table
    (This ensures a transaction cannot be repeatedly reversed)
    The correct amounts are then transferred in the opposite way as they were in the original transaction

    Will also reverse the sides of the transactions that ARE a part of this bank

    I also think this could then be added as its own transaction, which itself could be reversed, but I'm not sure if that's needed
    Also, I don't think a reversal request is needed, since the user stories say to do it "If the transaction is to/from another bank"
     */
    public static void reverseValues(Transaction toReverse, Account[] accountsInBank, Statement stmt) throws SQLException {

        /*
        Deletes the original transaction
         */
        String sql = "DELETE FROM Transactions WHERE id = '" + toReverse.getId() + "'";
        stmt.execute(sql);

        Account withdraw = accountsInBank[0];
        Account deposit = accountsInBank[1];

        /*
        Performs the opposite actions to what was in the original transaction, and updates their values in the database
        (or those that CAN be done by our bank)
         */

        if (withdraw != null) {
            System.out.println("OLD VALUES FOR 'withdraw': balance = " + withdraw.getBalance() + ", noOfTrans = " + withdraw.getNoOfTransactions());

            withdraw.deposit(toReverse.getAmount());
            withdraw.setNoOfTransactions(withdraw.getNoOfTransactions() - 1);

            sql = "UPDATE Accounts SET balance = " + withdraw.getBalance() + "," + "noOfTransactions = " + withdraw.getNoOfTransactions() +
                    " WHERE id = '" + withdraw.getId() + "'";
            stmt.executeUpdate(sql);
        }

        if (deposit != null) {
            System.out.println("OLD VALUES FOR 'deposit': balance = " + deposit.getBalance() + ", noOfTrans = " + deposit.getNoOfTransactions());

            deposit.withdraw(toReverse.getAmount());
            deposit.setNoOfTransactions(deposit.getNoOfTransactions() - 1);

            sql = "UPDATE Accounts SET balance = " + deposit.getBalance() + "," + "noOfTransactions = " + deposit.getNoOfTransactions() +
                    " WHERE id = '" + deposit.getId() + "'";
            stmt.executeUpdate(sql);
        }

        /*
        Creates a new transaction of the reversed transaction
        I think I'm creating the timestamp correctly, and I'm not sure on how new IDs are created, so I'm reusing the old one since it was deleted
         */
        String currentTime = new Timestamp(System.currentTimeMillis()).toString();
        currentTime = currentTime.replace(" ","T");
        currentTime += "Z";

        sql = "INSERT INTO Transactions " + "VALUES ('" + toReverse.getDepositAccount() + "', '"
                + toReverse.getWithdrawAccount() + "', '" + currentTime + "', '"
                + toReverse.getId() + "', '" + toReverse.getAmount() + "', '" + toReverse.getCurrency() + "')";
        stmt.executeUpdate(sql);

        System.out.println("Created new opposite transaction");

    }

    /*
    This method reverses a transaction given its ID
    */
    public static String reverseTransaction(Logger log, DataSource ds, String transactionID) {

        String returnText =  "Transaction reversed successfully.";

        try (Connection connection = ds.getConnection()) {

            /*
            Finds the transaction from the table where the id matches the transaction to be reversed
             */
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM Transactions WHERE id = '" + transactionID + "'";
            ResultSet rs = stmt.executeQuery(sql);

            Transaction toReverse = findTransactionSQL(ds, log, transactionID);

            /*
            If the transaction is found, checks to see if the two accounts involved are both from our bank
            If they both are, then the reversal goes ahead
            If not, a reversal request is sent.
            This is one part I'm not quite sure about, in that I don't know if anything needs to be done on our end
            If so, presumably we reverse what we can and the other bank does the rest?
             */
            if (toReverse != null) {

                System.out.println("Transaction found!");

                /*
                Find which of the 2 accounts are from our bank
                Stores their query results in this list since they are queried already
                 */
                Account[] accountsInBank = findFromOurBank(toReverse, stmt);

                /*
                If either bank isn't from our bank, one must belong to another bank
                 */
                if (accountsInBank[0] == null || accountsInBank[1] == null) {
                    System.out.println("One of the accounts not in our bank.");
                    reverseValues(toReverse, accountsInBank, stmt);

                    /*
                    Sends reversal request
                    Has to edit current timestamp to fit the format
                     */
                    String currentTime = new Timestamp(System.currentTimeMillis()).toString();
                    currentTime = currentTime.replace(" ","T");
                    currentTime += "Z";
                    HttpResponse<JsonNode> reversalResponse = Unirest.post("https://api.asep-strath.co.uk/api/team4/reversal")
                            .header("Accept","*/*")
                            .header("Content-Type","application/json")
                            .body("{\"transaction\":\""+toReverse.getId()+"\",\"timestamp\":\""+currentTime+"\"}")
                                    .asJson();
                    System.out.println("Timestamp: " + currentTime);
                    System.out.println("Reversal status: " + reversalResponse.getStatus());
                    System.out.println("Reversal message: " + reversalResponse.getStatusText());
                    System.out.println("Headers: " + reversalResponse.getHeaders());

                    returnText += " " + reversalResponse.getStatus() + " - " + reversalResponse.getStatusText() + ".";
                }
                else{
                    /*
                    Reverses the transaction in our bank
                     */
                    System.out.println("Both in our bank!");
                    reverseValues(toReverse, accountsInBank, stmt);
                }

            } else {
                log.info("Could not find given transaction.");
                return "Could not find given transaction.";
            }

        } catch (SQLException e) {
            log.error("Database Creation Error", e);
            return "Database Creation Error.";
        }

        return returnText;
    }

}
