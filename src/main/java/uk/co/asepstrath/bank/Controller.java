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
     * This constructor can take in any dependencies the controller may need to
     * respond to a request
     */
    public Controller(DataSource ds, Logger log) {
        dataSource = ds;
        logger = log;
        data = new Dataface(ds, log);
    }

    @GET("/")
    public ModelAndView mainPage() {
        return new ModelAndView("index.hbs");
    }

    @GET("/viewaccounts")
    public ModelAndView displayAccounts(@QueryParam String name) {
        // Return value which has the handlebars with map of Accounts
        return new ModelAndView("accounts.hbs",
                data.getAccounts("http://api.asep-strath.co.uk/api/team4/accounts", name));
    }

    @GET()
    @Path("/viewaccounts/{name}")
    public ModelAndView displayAccountss(@PathParam("name") @QueryParam String name) {
        // Return value which has the handlebars with map of Accounts
        return new ModelAndView("accounts.hbs",
                data.getAccounts("http://api.asep-strath.co.uk/api/team4/accounts", name));
    }

    @GET("/viewaccountsjson")
    public String accountsFromDB(@QueryParam String name) {
        // Creates a Gson (which is a Json format) object and populates with the
        // Arraylist
        String accountAsString = new Gson().toJson(data.getAccountsSQL(name));
        return accountAsString;
    }

    @GET("/transactionsinformation")
    public ModelAndView transactionsFromDB() {
        return new ModelAndView("transactions.hbs", data.getTransactions("http://api.asep-strath.co.uk/api/team4/fraud"));
    }

    @GET("/transactionshistory")
    public ModelAndView viewTransactionsFromDB(@QueryParam String id) {
        return new ModelAndView("viewTransactions.hbs", data.gettransactionss(id));
    }

    @GET("/viewtransactionsjson")
    public String viewTransactionsFromDBJson(@QueryParam String id) {
        return data.GetTransactionsJSON(id);
    }

    @GET("/reversetransaction/{transactionid}")
    public ModelAndView reverseTransactions(@PathParam("transactionid") @QueryParam String transactionID) {
        if (data.reverse(transactionID).contains("successfully")) {
            return new ModelAndView("reverseSuccess.hbs");
        } else {
            return new ModelAndView("Fail.hbs");
        }
    }

    @GET("/repeattransaction/{transactionid}")
    public ModelAndView repeatTransactions(@PathParam("transactionid") @QueryParam String transactionID) {
        if (data.repeat(transactionID).contains("successfully")) {
            return new ModelAndView("repeatSuccess.hbs");
        } else {
            return new ModelAndView("Fail.hbs");
        }
    }

}
