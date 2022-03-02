package uk.co.asepstrath.bank;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import io.jooby.Jooby;
import io.jooby.handlebars.HandlebarsModule;
import io.jooby.helper.*;
import io.jooby.hikari.HikariModule;
import org.slf4j.Logger;
import io.jooby.json.JacksonModule;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class App extends Jooby {
    {
        /*
        This section is used for setting up the Jooby Framework modules
         */
        install(new UniRestExtension());
        install(new HandlebarsModule());
        install(new HikariModule("mem"));
        install(new JacksonModule());
        /*
        This will host any files in src/main/resources/assets on <host>/assets
        For example in the dice template (dice.hbs) it references "assets/dice.png" which is in resources/assets folder
         */
        assets("/assets/*", "/assets");
        /*
        Now we set up our controllers and their dependencies
         */
        DataSource ds = require(DataSource.class);
        Logger log = getLog();

        mvc(new Controller(ds,log));

        /*
        Finally, we register our application lifecycle methods
         */
        onStarted(() -> onStart());
        onStop(() -> onStop());
    }

    public static void main(final String[] args) {
        runApp(args, App::new);
    }

    /*
    This method verifies that names will not break the SQL entering.
    */
    public String SQLverify (String paramter){
        return paramter.replace("'", "''");
    }

    /*
    This function will be called when the application starts up,
    it should be used to ensure that the DB is properly setup
     */
    public void onStart() {
        Logger log = getLog();
        log.info("Starting Up...");
        // Fetch DB Source
        DataSource ds = require(DataSource.class);
        // Open Connection to DB
        try (Connection connection = ds.getConnection()) {
            // Initialise the connection
            Statement stmt = connection.createStatement();

            ArrayList<Account> accounts = new ArrayList<Account>();

            stmt.executeUpdate("CREATE TABLE Accounts (id varchar(255), name varchar(255), balance varchar(255), currency varchar(255), accountType varchar(255), noOfTransactions varchar(255), noOfFailedTransactions varchar(255))");
            HttpResponse<JsonNode> accountsResponse = Unirest.get("http://api.asep-strath.co.uk/api/team4/accounts").asJson();
            JSONArray accountsArray = accountsResponse.getBody().getArray();

            for (int i = 0; i < accountsArray.length(); i++) {
                JSONObject tempaccount = accountsArray.getJSONObject(i);
                stmt.addBatch("INSERT INTO Accounts " + "VALUES ('" + tempaccount.getString("id") + "', '"
                        + SQLverify(tempaccount.getString("name")) + "', '" + tempaccount.getString("balance") +"', '"
                        + tempaccount.getString("currency")+"', '"+tempaccount.getString("accountType")+ "','" + "0" + "','" + "0" + "')");

                accounts.add(new Account(tempaccount.getString("id"), tempaccount.getString("name"), Float.valueOf(tempaccount.getString("balance")).floatValue(), tempaccount.getString("currency"), tempaccount.getString("accountType"), 0, 0));
            }
            stmt.executeBatch();

            ArrayList<Transaction> transactions = new ArrayList<Transaction>();

            stmt.executeUpdate("CREATE TABLE Transactions (withdrawAccount varchar(255), depositAccount varchar(255), timestamp varchar(255), id varchar(255), amount varchar(255), currency varchar(255))");

            HttpResponse<JsonNode> transactionsResponse = Unirest.get("http://api.asep-strath.co.uk/api/team4/transactions?PageSize=999").asJson();
            JSONArray transactionArray = transactionsResponse.getBody().getArray();

            for (int i = 0; i < transactionArray.length(); i++) {
                JSONObject tempaccount = transactionArray.getJSONObject(i);

                stmt.addBatch("INSERT INTO Transactions " + "VALUES ('" + tempaccount.getString("withdrawAccount") + "', '"
                            + tempaccount.getString("depositAccount") + "', '" + tempaccount.getString("timestamp") + "', '"
                            + tempaccount.getString("id") + "', '" + tempaccount.getString("amount") + "', '" + tempaccount.getString("currency") + "')");

                transactions.add(new Transaction(tempaccount.getString("withdrawAccount"), tempaccount.getString("depositAccount"), tempaccount.getString("timestamp"), tempaccount.getString("id"), Float.valueOf(tempaccount.getString("amount")), tempaccount.getString("currency")));

            }
            stmt.executeBatch();

            String fraudResponse = Unirest.get("http://api.asep-strath.co.uk/api/team4/fraud").header("accept", "application/json").asString().getBody();

            String fraudArray[] = fraudResponse.replace("[", "").replace("]", "").replaceAll("\"", "").split(",");

            HashMap<Transaction, Account> toProcess = ProcessTransactions.processTransactions(accounts, transactions, fraudArray);

            for(Map.Entry<Transaction, Account> set: toProcess.entrySet()) {

                String accountBalance = String.valueOf(set.getValue().getBalance());
                String accountNoOfTransactions = String.valueOf(set.getValue().getNoOfTransactions());
                String accountNoOfFailedTransactions = String.valueOf(set.getValue().getNoOfFailedTransactions());



                stmt.addBatch("UPDATE Accounts SET balance = " + accountBalance + "," + "noOfTransactions = " + accountNoOfTransactions + "," + "noOfFailedTransactions = " + accountNoOfFailedTransactions);

            }

            stmt.executeBatch();

        } catch (SQLException e) {
            log.error("Database Creation Error",e);
        }
    }
    /*
    This function will be called when the application shuts down
     */
    public void onStop() {
        System.out.println("Shutting Down...");
    }

}
