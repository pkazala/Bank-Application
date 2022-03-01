package uk.co.asepstrath.bank;
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
                        jsonAccounts.getJSONObject(i).getString("accountType")));
            }
        } else {
            // Loop for every element in the JSON Array "jsonAccounts" adding every instance to the ArrayList
            for(int i = 0; i< jsonAccounts.length();i++) {
                // Extract value from Result as an Account class and adds to ArrayList
                if (jsonAccounts.getJSONObject(i).getString("name").equals(name)){
                    accounts.add (new Account(jsonAccounts.getJSONObject(i).getString("id"), jsonAccounts.getJSONObject(i).getString("name"),
                            jsonAccounts.getJSONObject(i).getFloat("balance"), jsonAccounts.getJSONObject(i).getString("currency") ,
                            jsonAccounts.getJSONObject(i).getString("accountType")));
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
                        set.getString("accountType")));
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
}
