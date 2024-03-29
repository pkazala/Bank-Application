package uk.co.asepstrath.bank;
import io.jooby.Jooby;
import io.jooby.OpenAPIModule;
import io.jooby.handlebars.HandlebarsModule;
import io.jooby.helper.*;
import io.jooby.hikari.HikariModule;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.slf4j.Logger;
import io.jooby.json.JacksonModule;
import javax.sql.DataSource;

@OpenAPIDefinition(
        info = @Info(
                title = "Team 4 Bank Project",
                description = "Swagger API Documentation"
        )
)
public class App extends Jooby {
    {
        /*
        This section is used for setting up the Jooby Framework modules
         */
        install(new UniRestExtension());
        install(new HandlebarsModule());
        install(new HikariModule("mem"));
        install(new JacksonModule());
        install(new OpenAPIModule());


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
    This function will be called when the application starts up,
    it should be used to ensure that the DB is properly setup
     */
    public void onStart() {
        Logger log = getLog();
        log.info("Starting Up...");
        DataSource ds = require(DataSource.class);
        Dataface data = new Dataface(ds,log);
        // Initialise data.
        log.info(data.initialiseData());
    }
    /*
    This function will be called when the application shuts down
     */
    public void onStop() {
        System.out.println("Shutting Down...");
    }

}
