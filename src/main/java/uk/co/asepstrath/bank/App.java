package uk.co.asepstrath.bank;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.ObjectMapper;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import uk.co.asepstrath.bank.Controller;
import io.jooby.Jooby;
import io.jooby.handlebars.HandlebarsModule;
import io.jooby.helper.*;
import io.jooby.hikari.HikariModule;
import org.slf4j.Logger;
import io.jooby.json.JacksonModule;

import javax.sql.DataSource;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;
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
            // Create the table structure
            stmt.executeUpdate("CREATE TABLE Accounts (id varchar(255), name varchar(255), balance varchar(255), currency varchar(255), accountType varchar(255))");
            HttpResponse<JsonNode> response = Unirest.get("http://api.asep-strath.co.uk/api/team4/accounts").asJson();
            JSONArray accountsArray = response.getBody().getArray();
            for (int i = 0; i < accountsArray.length(); i++) {
                JSONObject tempaccount = accountsArray.getJSONObject(i);
                stmt.addBatch("INSERT INTO Accounts " + "VALUES ('" + tempaccount.getString("id") + "', '"
                        + SQLverify(tempaccount.getString("name")) + "', '" + tempaccount.getString("balance") +"', '"
                        + tempaccount.getString("currency")+"', '"+tempaccount.getString("accountType")+"')");
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
