package uk.co.asepstrath.bank;

import io.jooby.JoobyTest;
import io.jooby.StatusCode;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JoobyTest(App.class)
public class IntegrationTest {

    static OkHttpClient client = new OkHttpClient();

    @Test
    public void shouldPrintUser(int serverPort) throws IOException {
        Request req = new Request.Builder()
                .url("http://localhost:" + serverPort+"/viewaccountsjson?name=Joe%20Ullrich")
                .build();
        try (Response rsp = client.newCall(req).execute()) {
            assertEquals("[{\"id\":\"30505486-274a-4e25-99bc-70f3b8767e6e\",\"name\":\"Joe Ullrich\",\"balance\"" +
                    ":663869.2,\"currency\":\"BIF\",\"accountType\":\"Checking Account\"}]", rsp.body().string());
            assertEquals(StatusCode.OK.value(), rsp.code());
        }
    }

    @Test
    public void shouldPrintUsers(int serverPort) throws IOException {
        Request req = new Request.Builder()
                .url("http://localhost:" + serverPort+"/example/viewaccountsjson")
                .build();

        try (Response rsp = client.newCall(req).execute()) {
            //assertEquals( Controller.getAccountsSQL();, rsp.body().string());
            assertEquals(StatusCode.OK.value(), rsp.code());
        }
    }
}
