package uk.co.asepstrath.bank;
import com.google.gson.Gson;
import io.jooby.ModelAndView;
import io.jooby.annotations.*;
import org.slf4j.Logger;
import javax.sql.DataSource;


@Path("/")
public class Controller {
    private final DataSource dataSource;
    private final Logger logger;
    private final Dataface data;

    /*
    This constructor can take in any dependencies the controller may need to respond to a request
     */
    public Controller(DataSource ds, Logger log) {
        dataSource = ds;
        logger = log;
        data = new Dataface(ds,log);
    }

    @GET("/viewaccounts")
    public ModelAndView displayAccounts(@QueryParam String name){
        // Return value which has the handlebars with map of Accounts
        return new ModelAndView("accounts.hbs", data.getAccounts(name));
    }
    @GET()
    @Path("/viewaccounts/{name}")
    public ModelAndView displayAccountss(@PathParam("name") @QueryParam String name) {
        // Return value which has the handlebars with map of Accounts
        return new ModelAndView("accounts.hbs", data.getAccounts(name));
    }

    //this method works with the /viewaccounts/input method of specification where input is name
    //this is to match with video week 4, probably should be removed for marking maybe, stored somewhere else for later
    //it only runs if a name is specified which is why a normal method is still needed

    @GET("/viewaccountsjson")
    public String accountsFromDB(@QueryParam String name) {
            // Creates a Gson (which is a Json format) object and populates with the Arraylist
            String accountAsString = new Gson().toJson(data.getAccountsSQL(name));
            return  accountAsString;
    }
}
