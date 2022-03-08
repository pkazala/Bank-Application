package uk.co.asepstrath.bank;
import com.google.gson.Gson;
import io.jooby.ModelAndView;
import io.jooby.StatusCode;
import io.jooby.exception.StatusCodeException;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
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
            // Create Statement (batch of SQL Commands)
            Statement statement = connection.createStatement();
            // Initialises SQL Query
            ResultSet set;
            // Checks if the user has specified a name parameter
            if (name == null) {
                // Perform SQL Query with all Accounts
                set = statement.executeQuery("SELECT * FROM Accounts");
            } else {
                // Perform SQL Query with Accounts with name "name"
                set = statement.executeQuery("SELECT * FROM Accounts WHERE name = '"+name+"'");
            }
            // Read First Result
            // Initialises ArrayList to store Account's
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
    public Map<String, Object> getAccounts(String name) {
        Map<String, Object> model = new HashMap<>();
        // Checks if the API is available then acts accordingly
        if (Unirest.get("http://api.asep-strath.co.uk/api/team4/accounts").asJson().getStatus()==200){
            model.put("accounts", getAccountsAPI(name));
            model.put("story", "This is the latest record from the team4 bank API, Story 1 data works");
        }
        else {
            model.put("accounts", getAccountsSQL(name));
            model.put("story","This is the latest record from the SQL data base as the API was not available, Story 2");
        }
        return model;}

    public Map<String,Object> getTransactions(){
        HashMap<Transaction, Account> transactionsMap = new HashMap<Transaction, Account>();

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
            float accountBalanceBefore;
            float accountBalance;
            int noOfTransacitons;
            int noOfFailedTransactions;
            for(Map.Entry<Transaction, Account> modelSet: transactionsMap.entrySet()) {
                if(modelSet.getKey().getWithdrawAccount() == modelSet.getValue().getId()) {
                    accountBalanceBefore = modelSet.getValue().getBalance() + modelSet.getKey().getAmount();
                    accountBalance = modelSet.getValue().getBalance();
                    noOfTransacitons = modelSet.getValue().getNoOfTransactions();
                    noOfFailedTransactions = modelSet.getValue().getNoOfFailedTransactions();
                    updatedTransactions.add(new Transactions(accountBalanceBefore, accountBalance, noOfTransacitons, noOfFailedTransactions));
                }
                else {
                    accountBalanceBefore = modelSet.getValue().getBalance();
                    accountBalance = modelSet.getValue().getBalance() + modelSet.getKey().getAmount();
                    noOfTransacitons = modelSet.getValue().getNoOfTransactions();
                    noOfFailedTransactions = modelSet.getValue().getNoOfFailedTransactions();
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
}
