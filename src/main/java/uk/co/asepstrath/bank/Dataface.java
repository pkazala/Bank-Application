package uk.co.asepstrath.bank;
import com.google.gson.Gson;
import io.jooby.StatusCode;
import io.jooby.exception.StatusCodeException;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Dataface {
    private final DataSource dataSource;
    private final Logger logger;
    private final Connection connection;

    public Dataface (DataSource ds, Logger log) {
        dataSource = ds;
        logger = log;
        try (Connection tempConnection = dataSource.getConnection()) {
        connection = tempConnection;}
        catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred",e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }
    public String SQLverify (String paramter){
        return paramter.replace("'", "''");
    }

    public ArrayList<Account> getAccountsAPI(String name){
        HttpResponse<JsonNode> response = Unirest.get("http://api.asep-strath.co.uk/api/team4/accounts").asJson();
        // Extract the json data in the JSONArrayformat
        JSONArray jsonAccounts = response.getBody().getArray();
        // Initialises ArrayList to store Account's
        ArrayList<Account> accounts = new ArrayList<>();
        if (name == null) {
            // Perform SQL Query with all Accounts
            // Loop for every element in the JSON Array "jsonAccounts" adding every instance to the ArrayList
            for(int i = 0; i< jsonAccounts.length();i++) {
                // Extract value from Result as an Account class and adds to ArrayList
                accounts.add (new Account(jsonAccounts.getJSONObject(i).getString("id"), jsonAccounts.getJSONObject(i).getString("name"),
                        jsonAccounts.getJSONObject(i).getFloat("balance"), jsonAccounts.getJSONObject(i).getString("currency") ,
                        jsonAccounts.getJSONObject(i).getString("accountType"), 0, 0));
            }
        } else {
            // Loop for every element in the JSON Array "jsonAccounts" adding every instance to the ArrayList
            for(int i = 0; i< jsonAccounts.length();i++) {
                // Extract value from Result as an Account class and adds to ArrayList
                if (jsonAccounts.getJSONObject(i).getString("name").equals(name)){
                    accounts.add (new Account(jsonAccounts.getJSONObject(i).getString("id"), jsonAccounts.getJSONObject(i).getString("name"),
                            jsonAccounts.getJSONObject(i).getFloat("balance"), jsonAccounts.getJSONObject(i).getString("currency") ,
                            jsonAccounts.getJSONObject(i).getString("accountType"),0,0));
                }
            }
        }
        return accounts;
    }
        /*
        This method takes a query with a specified name and returns the SQL results (all if name not specified) back as an
        ArrayList which is then used differently by handlers and java endpoints alike
         */
    public ArrayList<Account> getAccountsSQL(String name){
        // Create a connection
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet set;
            if (name == null) {
                set = statement.executeQuery("SELECT * FROM Accounts");
            } else {
                set = statement.executeQuery("SELECT * FROM Accounts WHERE name = '"+name+"'");
            }
            ArrayList<Account> accounts = new ArrayList<Account>();
            while (set.next()){
                // Extract value from Result as an Account class and adds to ArrayList
                accounts.add(new Account(set.getString("id"),set.getString("name"),
                        set.getFloat("balance"),set.getString("currency"),
                        set.getString("accountType"),0,0));
            }
            return accounts;
        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred",e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }

    /* This method is for getting accounts, it also checks if the API is available, and if not it will access the
        pre-existing SQL data base and retreive data from there while also informing the user of where the data came from*/
    public Map<String, Object> getAccounts(String API, String name) {
        Map<String, Object> model = new HashMap<>();
        // Checks if the API is available then acts accordingly
        if (Unirest.get(API).asJson().getStatus()==200){
            model.put("accounts", getAccountsAPI(name));
            model.put("story", "This is the latest record from the team4 bank API, Story 1 data works");
        }
        else {
            model.put("accounts", getAccountsSQL(name));
            model.put("story","This is the latest record from the SQL data base as the API was not available, Story 2");
        }
        return model;}


    public Map<String,Object> getTransactions(){
        HashMap<Transaction, Account[]> transactionsMap = new HashMap<Transaction, Account[]>();

        try (Connection connection = dataSource.getConnection()) {
            // Create Statement (batch of SQL Commands)
            Statement statement = connection.createStatement();
            // Initialises SQL Query
            ResultSet set;
            // Checks if the user has specified a name parameter
            set = statement.executeQuery("SELECT * FROM Transactions");

            // Read First Result
            // Initialises ArrayList to store Account's
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
            while (set.next()){
                // Extract value from Result as an Account class and adds to ArrayList
                Transaction test = new Transaction(set.getString("withdrawAccount"),set.getString("depositAccount"),
                        set.getString("timestamp"),set.getString("id"),
                        Float.valueOf(set.getString("amount")), set.getString("currency"));
                transactions.add(test);
            }
            set = statement.executeQuery("SELECT * FROM Accounts");
            ArrayList<Account> accounts = new ArrayList<Account>();
            while (set.next()){
                // Extract value from Result as an Account class and adds to ArrayList
                accounts.add(new Account(set.getString("id"),set.getString("name"),
                        Float.valueOf(set.getString("balance")),set.getString("currency"),
                        set.getString("accountType"), Integer.valueOf(set.getString("noOfTransactions")), Integer.valueOf(set.getString("noOfFailedTransactions"))));
            }
            String fraudResponse = Unirest.get("http://api.asep-strath.co.uk/api/team4/fraud").header("accept", "application/json").asString().getBody();
            String fraudArray[] = fraudResponse.replace("[", "").replace("]", "").replaceAll("\"", "").split(",");

            transactionsMap = ProcessTransactions.processTransactions(accounts, transactions, fraudArray);
            Map<String, Object> model = new HashMap<>();
            ArrayList<Transactions> updatedTransactions = new ArrayList<Transactions>();
            float accountBalanceBefore, accountBalance;
            int noOfTransacitons, noOfFailedTransactions;
            for(Map.Entry<Transaction, Account[]> modelSet: transactionsMap.entrySet()) {

                Account[] arr = modelSet.getValue();

                for(int i = 0; i < 2; i++) {

                    if (modelSet.getKey().getWithdrawAccount() == arr[i].getId()) {
                        accountBalanceBefore = arr[i].getBalance() + modelSet.getKey().getAmount();
                        accountBalance = arr[i].getBalance();
                    } else {
                        accountBalanceBefore = arr[i].getBalance();
                        accountBalance = arr[i].getBalance() + modelSet.getKey().getAmount();
                    }
                    noOfTransacitons = arr[i].getNoOfTransactions();
                    noOfFailedTransactions = arr[i].getNoOfFailedTransactions();
                    updatedTransactions.add(new Transactions(accountBalanceBefore, accountBalance, noOfTransacitons, noOfFailedTransactions));
                }
            }

            model.put("accounts", updatedTransactions);
            int totalNoOfTransactions = 0;
            for(Transactions element: updatedTransactions) {
                totalNoOfTransactions += element.getNoOfTransactions();
            }
            model.put("transaction", totalNoOfTransactions);
            model.put("story", "This is the latest record in our SQL database, Transaction Information");

            return model;


    }
        catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred",e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }

    public Map<String,Object> gettransactionss(String id){
        try (Connection connection = dataSource.getConnection()) {
            // Create Statement (batch of SQL Commands)
            Statement statement = connection.createStatement();
            // Initialises SQL Query
            ResultSet set;
            // Checks if the user has specified a name parameter
            if (id == null) {
                // Perform SQL Query with all Accounts
                set = statement.executeQuery("SELECT * FROM Transactions");
            } else {
                // Perform SQL Query with Accounts with name "name"
                set = statement.executeQuery("SELECT * FROM Transactions WHERE id = '"+id+"'");
            }
            // Read First Result
            // Initialises ArrayList to store Account's
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
            while (set.next()){
                // Extract value from Result as an Account class and adds to ArrayList
                transactions.add(new Transaction(set.getString("withdrawAccount"),set.getString("depositAccount"),
                        set.getString("timestamp"),set.getString("id"),
                        set.getFloat("amount"), set.getString("currency")));
            }
            Map<String, Object> model = new HashMap<>();
            model.put("transactions", transactions);
            model.put("story", "This is the latest record in our SQL database, Story Transaction Information");
            return  model;

        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred",e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }

    public String GetTransactionsJSON(String id){
        try (Connection connection = dataSource.getConnection()) {
            // Create Statement (batch of SQL Commands)
            Statement statement = connection.createStatement();
            // Initialises SQL Query
            ResultSet set;
            // Checks if the user has specified a name parameter
            if (id == null) {
                // Perform SQL Query with all Accounts
                set = statement.executeQuery("SELECT * FROM Transactions");
            } else {
                // Perform SQL Query with Accounts with name "name"
                set = statement.executeQuery("SELECT * FROM Transactions WHERE id = '"+id+"'");
            }
            // Read First Result
            // Initialises ArrayList to store Account's
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
            while (set.next()){
                // Extract value from Result as an Account class and adds to ArrayList
                transactions.add(new Transaction(set.getString("withdrawAccount"),set.getString("depositAccount"),
                        set.getString("timestamp"),set.getString("id"),
                        set.getFloat("amount"), set.getString("currency")));
            }

            // Creates a Gson (which is a Json format) object and populates with the Arraylist

            String transactionsAsString = new Gson().toJson(transactions);
            // Return value
            return transactionsAsString;

        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred",e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }
    public String initialiseData(){
        try (Connection connection = dataSource.getConnection()) {
            // Initialise the connection
            Statement stmt = connection.createStatement();
            ArrayList<Account> accounts = new ArrayList<Account>();
            stmt.executeUpdate("CREATE TABLE Accounts (id varchar(36), name varchar(100), balance varchar(50), " +
                    "currency varchar(3), accountType varchar(50), noOfTransactions varchar(16), " +
                    "noOfFailedTransactions varchar(16))");
            HttpResponse<JsonNode> accountsResponse = Unirest.get("http://api.asep-strath.co.uk/api/team4/accounts")
                    .asJson();
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
            stmt.executeUpdate("CREATE TABLE Transactions (withdrawAccount varchar(36), depositAccount varchar(36), timestamp varchar(30), id varchar(36), amount varchar(50), currency varchar(3))");
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

            HashMap<Transaction, Account[]> toProcess = ProcessTransactions.processTransactions(accounts, transactions, fraudArray);
            for(Map.Entry<Transaction, Account[]> set: toProcess.entrySet()) {
                Account[] arr = set.getValue();
                for (int i = 0; i < 2; i++) {
                    String accountId = String.valueOf(arr[i].getId());
                    String accountBalance = String.valueOf(arr[i].getBalance());
                    String accountNoOfTransactions = String.valueOf(arr[i].getNoOfTransactions());
                    String accountNoOfFailedTransactions = String.valueOf(arr[i].getNoOfFailedTransactions());
                    //stmt.addBatch("UPDATE Accounts SET balance = " + accountBalance + "," + "noOfTransactions = " + accountNoOfTransactions + "," + "noOfFailedTransactions = " + accountNoOfFailedTransactions);
                    stmt.addBatch("UPDATE Accounts SET balance = " + accountBalance + "," + "noOfTransactions = " + accountNoOfTransactions + "," + "noOfFailedTransactions = " + accountNoOfFailedTransactions +
                            " WHERE id = '" + accountId + "'");
                }
            }
            stmt.executeBatch();



        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred",e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
        return "Data was read from the api successfully.";
    }
}
