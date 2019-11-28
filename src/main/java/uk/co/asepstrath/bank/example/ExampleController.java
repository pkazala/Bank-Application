package uk.co.asepstrath.bank.example;

import io.jooby.ModelAndView;
import io.jooby.StatusCode;
import io.jooby.StatusCodeException;
import io.jooby.annotations.*;
import kong.unirest.Unirest;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/*
    Example Controller is a Controller from the MVC paradigm.
    The @Path Annotation will tell Jooby what /path this Controller can respond to,
    in this case the controller will respond to requests from <host>/example
 */
@Path("/example")
public class ExampleController {

    private final DataSource dataSource;
    private final Logger logger;

    /*
    This constructor can take in any dependencies the controller may need to respond to a request
     */
    public ExampleController(DataSource ds, Logger log) {
        dataSource = ds;
        logger = log;
    }

    /*
    This is the simplest action a controller can perform
    The @GET annotation denotes that this function should be invoked when a GET HTTP request is sent to <host>/example
    The returned string will then be sent to the requester
     */
    @GET
    public String welcome() {
        return "Welcome to Jooby!";
    }

    /*
    This @Get annotation takes an optional path parameter which denotes the function should be invoked on GET <host>/example/hello
    Note that this function makes it's own request to another API (http://faker.hook.io/) and returns the response
     */
    @GET("/hello")
    public String sayHi() {
        return "Hello " + Unirest.get("http://faker.hook.io/").asString().getBody();
    }

    /*
    This request makes a call to the passed in data source (The Database) which has been set up in App.java
     */
    @GET("/welcome")
    public String welcomeFromDB() {
        String welcomeMessageKey = "WelcomeMessage";
        // Create a connection
        try (Connection connection = dataSource.getConnection()) {
            // Create Statement (batch of SQL Commands)
            Statement statement = connection.createStatement();
            // Perform SQL Query
            ResultSet set = statement.executeQuery("SELECT * FROM Example Where Key = '"+welcomeMessageKey+"'");
            // Read First Result
            set.next();
            // Extract value from Result
            String welcomeMessage = set.getString("Value");
            // Return value
            return welcomeMessage;
        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred",e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }
    }

    /*
    The dice endpoint displays two features of the Jooby framework, Parameters and Templates

    You can see that this function takes in a String name, the annotation @QueryParam tells the framework that
    the value of name should come from the URL Query String (<host>/example/dice?name=<value>)

    The function then uses this value and others to create a Map of values to be injected into a template.
    The ModelAndView constructor takes a template name and the model.
    The Template name is the name of the file containing the template, this name is relative to the folder src/main/resources/views

    We have set the Jooby framework up to use the Handlebars templating system which you can read more on here:
    https://handlebarsjs.com/guide/
     */
    @GET("/dice")
    public ModelAndView dice(@QueryParam String name) {

        // If no name has been sent within the query URL
        if (name == null) {
            name = "Your";
        } else {
            name = name + "'s";
        }

        // we must create a model to pass to the "dice" template
        Map<String, Object> model = new HashMap<>();
        model.put("random", new Random().nextInt(6));
        model.put("name", name);

        return new ModelAndView("dice.hbs", model);

    }

    /*
    The @POST annotation registers this function as a HTTP POST handler.
    It will look at the body of the POST request and try to deserialise into a MyMessage object
     */
    @POST
    public String post(MyMessage message) {
        return "You successfully POSTed: "+message.Message+ " To: "+message.Recipient;
    }
}
