package uk.co.asepstrath.bank;

import com.google.gson.Gson;

import io.jooby.ModelAndView;
import io.jooby.StatusCode;
import io.jooby.annotations.*;
import io.jooby.exception.StatusCodeException;
import kong.unirest.Unirest;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Account;

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

    @GET("/viewaccountsjson")
    public String accountsFromDB(@QueryParam String name) {
        // Create a connection
        try (Connection connection = dataSource.getConnection()) {
            // Create Statement (batch of SQL Commands)
            Statement statement = connection.createStatement();
            // Initialises SQL Query
            ResultSet set;
            // Checks if the user has specified a name parameter
            if (name == null) {
                // Perform SQL Query with all Accounts
                set = statement.executeQuery("SELECT * FROM Example");
            } else {
                // Perform SQL Query with Accounts with name "name"
                set = statement.executeQuery("SELECT * FROM Example WHERE name = '"+name+"'");
            }
            // Initialises ArrayList to store Account's
            ArrayList<Account> accounts = new ArrayList<Account>();
            // Loops through SQL Query Result
            while (set.next()){
                // Extract value from Result as an Account class and adds to ArrayList
                accounts.add(new Account(set.getString("id"),set.getString("name"),set.getFloat("balance"),set.getString("currency"),set.getString("accountType")));
            }
            // Creates a Gson (which is a Json format) object and populates with the Arraylist
            String accountAsString = new Gson().toJson(accounts);
            // Return value
            return  accountAsString;

        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred",e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }

    @GET("/viewaccounts")
    public ModelAndView accountsFromDBHTML(@QueryParam String name) {
        // Create a connection
        try (Connection connection = dataSource.getConnection()) {
            // Create Statement (batch of SQL Commands)
            Statement statement = connection.createStatement();
            // Initialises SQL Query
            ResultSet set;
            // Checks if the user has specified a name parameter
            if (name == null) {
                // Perform SQL Query with all Accounts
                set = statement.executeQuery("SELECT * FROM Example");
            } else {
                // Perform SQL Query with Accounts with name "name"
                set = statement.executeQuery("SELECT * FROM Example WHERE name = '"+name+"'");
            }
            // Read First Result
            // Initialises ArrayList to store Account's
            ArrayList<Account> accounts = new ArrayList<Account>();
            while (set.next()){
                // Extract value from Result as an Account class and adds to ArrayList
                accounts.add(new Account(set.getString("id"),set.getString("name"),set.getFloat("balance"),set.getString("currency"),set.getString("accountType")));
            }
            // Initialises Map model which is <String, Object> to match ModelAndView parameters
            Map<String, Object> model = new HashMap<>();
            // Add the ArrayList to the map with name accounts which will be used in accounts.hbs to loop
            model.put("accounts", accounts);
            // Return value which has the handlebars with map of Accounts
            return new ModelAndView("accounts.hbs", model);

        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred",e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }

    //this method works with the /viewaccounts/input method of specification where input is name
    //this is to match with video week 4, probably should be removed for marking maybe, stored somewhere else for later
    //it only runs if a name is specified which is why a normal method is still needed
    @GET
    @Path("/viewaccounts/{name}")
    public String accountFromDB(@PathParam("name") String name) {
        // Create a connection
        try (Connection connection = dataSource.getConnection()) {
            // Create Statement (batch of SQL Commands)
            Statement statement = connection.createStatement();
            // Perform SQL Query with Accounts with name "name"
            ResultSet set = statement.executeQuery("SELECT * FROM Example WHERE Key = '"+name+"'");
            // Initialises ArrayList to store Account's
            ArrayList<Account> accounts = new ArrayList<Account>();
            // Loops through SQL Query Result
            while (set.next()){
                // Extract value from Result as an Account class and adds to ArrayList
                accounts.add(new Account(set.getString("id"),set.getString("name"),set.getFloat("balance"),set.getString("currency"),set.getString("accountType")));
            }
            // Creates a Gson (which is a Json format) object and populates with the Arraylist
            String accountAsString = new Gson().toJson(accounts);
            // Return value
            return  accountAsString;

        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred",e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }

}
