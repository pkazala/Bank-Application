package uk.co.asepstrath.bank;

import com.google.gson.Gson;

import io.jooby.ModelAndView;
import io.jooby.StatusCode;
import io.jooby.annotations.*;
import io.jooby.exception.StatusCodeException;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;
import kong.unirest.json.JSONArray;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Path("/")
public class Controller {

    private final DataSource dataSource;
    private final Logger logger;

    /*
    This constructor can take in any dependencies the controller may need to respond to a request
     */
    public Controller(DataSource ds, Logger log) {
        dataSource = ds;
        logger = log;
    }

    /*
    This method takes a query with a specified name and returns the SQL results (all if name not specified) back as an
    ArrayList which is then used differently by handlers and java endpoints alike
     */
    private ArrayList<Account> getAccountsSQL(String name){
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

    @GET("/Story1")
    public ModelAndView accountsFromAPIHTML(@QueryParam String name)  {
        // Access the api returning a json reponse
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
            }}
        }

        // Initialises Map model which is <String, Object> to match ModelAndView parameters
        Map<String, Object> model = new HashMap<>();
        // Add the ArrayList to the map with name accounts which will be used in accounts.hbs to loop
        model.put("accounts", accounts);
        // Add the header to distinguish between the different stories
        model.put("story", "This is the latest record from the team4 bank API, Story 1");
        // Return value which has the handlebars with map of Accounts
        return new ModelAndView("accounts.hbs", model);
    }

    @GET("/Story2")
    public ModelAndView accountsFromDBHTML(@QueryParam String name) {
            Map<String, Object> model = new HashMap<>();
            // Add the ArrayList to the map with name accounts which will be used in accounts.hbs to loop
            model.put("accounts", getAccountsSQL(name));
            model.put("story", "This is the latest record in our SQL database, Story 2");
            // Return value which has the handlebars with map of Accounts
            return new ModelAndView("accounts.hbs", model);

    }

    //this method works with the /viewaccounts/input method of specification where input is name
    //this is to match with video week 4, probably should be removed for marking maybe, stored somewhere else for later
    //it only runs if a name is specified which is why a normal method is still needed
    @GET
    @Path("/viewaccounts/{name}")
    public String accountFromDB(@PathParam("name") String name) {
            // get the ArrayList containing required results from the SQL database and set to string
            String accountAsString = new Gson().toJson(getAccountsSQL(name));
            // Return value
            return  accountAsString;
    }

    @GET("/viewaccountsjson")
    public String accountsFromDB(@QueryParam String name) {
            // Creates a Gson (which is a Json format) object and populates with the Arraylist
        System.out.println(getAccountsSQL(name).size());
            String accountAsString = new Gson().toJson(getAccountsSQL(name));
            // Return value
            return  accountAsString;
    }

}