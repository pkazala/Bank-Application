package uk.co.asepstrath.bank;

import io.jooby.Jooby;
import io.jooby.OpenAPIModule;
import io.jooby.handlebars.HandlebarsModule;
import io.jooby.helper.UniRestExtension;
import io.jooby.hikari.HikariModule;
import io.jooby.json.JacksonModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static io.jooby.KoobyKt.require;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatafaceTest extends Jooby {
    private DataSource ds;
    private Logger log;
    private Dataface df;


    /*
    Unit tests should be here
    Example can be found in example/UnitTest.java
     */
    @BeforeAll
    public void setUp() {
        install(new UniRestExtension());
        install(new HandlebarsModule());
        install(new HikariModule("mem"));
        install(new JacksonModule());
        install(new OpenAPIModule());

        ds = require(DataSource.class);
        log = getLog();
        df = new Dataface(ds, log);
        String success = df.initialiseData();
    }

    @Test
    public void SQLVerifier(){
        Assertions.assertTrue(df.SQLverify("I'm").equals("I''m"));

    }


    @Test
    public void getAccountsAPITester(){
        ArrayList<Account> testingAccounts = new ArrayList<>();
        Account mockPlayer = new Account("0062f74c-1fd6-4e0d-9213-285a56772069", "Pierce Koepp", 0,
                null, null, 0, 0);
        testingAccounts.add(mockPlayer);
        ArrayList<Account> resultAccounts = df.getAccountsAPI("Pierce Koepp");
        Assertions.assertTrue(testingAccounts.get(0).getId().equals(resultAccounts.get(0).getId()));
        Assertions.assertTrue(testingAccounts.get(0).getName().equals(resultAccounts.get(0).getName()));
        Assertions.assertTrue(testingAccounts.get(0).getBalance() < resultAccounts.get(0).getBalance());
        Assertions.assertTrue(resultAccounts.get(0).getCurrency() != null);
        Assertions.assertTrue(resultAccounts.get(0).getAccountType() != null);
        Assertions.assertTrue(testingAccounts.get(0).getNoOfTransactions() <= resultAccounts.get(0).getNoOfTransactions());
        Assertions.assertTrue(testingAccounts.get(0).getNoOfFailedTransactions() <= resultAccounts.get(0).getNoOfFailedTransactions());
    }

    @Test
    public void getAccountsSQLTester(){
        ArrayList<Account> testingAccounts = new ArrayList<>();
        Account mockPlayer = new Account("0062f74c-1fd6-4e0d-9213-285a56772069", "Pierce Koepp", 0,
                null, null, 0, 0);
        testingAccounts.add(mockPlayer);
        ArrayList<Account> resultAccounts = df.getAccountsSQL("Pierce Koepp");
        Assertions.assertTrue(testingAccounts.get(0).getId().equals(resultAccounts.get(0).getId()));
        Assertions.assertTrue(testingAccounts.get(0).getName().equals(resultAccounts.get(0).getName()));
        Assertions.assertTrue(testingAccounts.get(0).getBalance() < resultAccounts.get(0).getBalance());
        Assertions.assertTrue(resultAccounts.get(0).getCurrency() != null);
        Assertions.assertTrue(resultAccounts.get(0).getAccountType() != null);
        Assertions.assertTrue(testingAccounts.get(0).getNoOfTransactions() <= resultAccounts.get(0).getNoOfTransactions());
        Assertions.assertTrue(testingAccounts.get(0).getNoOfFailedTransactions() <= resultAccounts.get(0).getNoOfFailedTransactions());
    }

    @Test
    public void getAccountsTester(){
        Map<String, Object> result;
        result =  df.getAccounts("http://api.asep-strath.co.uk/api/team4/accounts",null);
        Assertions.assertTrue(result.get("story").equals("This is the latest record from the team4 bank API, Story 1 data works"));
        result =  df.getAccounts("http://api.asep-strath.co.uk/api/CrazyTeam/accounts.",null);
        Assertions.assertTrue(result.get("story").equals("This is the latest record from the SQL data base as the API was not available, Story 2"));
    }

    @Test
    public void getTransactionsTester(){
        Map<String, Object> result;
        result =  df.getTransactions("http://api.asep-strath.co.uk/api/team4/fraud");
        Assertions.assertTrue(result.get("story").equals("This is the latest record from the API, Transaction Information"));
        result =  df.getTransactions("http://api.asep-strath.co.uk/api/team4/fraudasdsadsadas");
        Assertions.assertTrue(result.get("story").equals("The API was not available, Transaction Information"));

    }

}
