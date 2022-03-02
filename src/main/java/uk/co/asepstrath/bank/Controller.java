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

    @GET("/transactions")
    public ModelAndView transactionsFromDB() {

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

            return new ModelAndView("transactions.hbs", model);

        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred",e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }

    }

    @GET("/viewtransactions")
    public ModelAndView viewTransactionsFromDB(@QueryParam String id) {

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

            return new ModelAndView("viewTransactions.hbs", model);

        } catch (SQLException e) {
            // If something does go wrong this will log the stack trace
            logger.error("Database Error Occurred",e);
            // And return a HTTP 500 error to the requester
            throw new StatusCodeException(StatusCode.SERVER_ERROR, "Database Error Occurred");
        }

    }

    public String viewTransactionsFromDBJson(@QueryParam String id) {

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
