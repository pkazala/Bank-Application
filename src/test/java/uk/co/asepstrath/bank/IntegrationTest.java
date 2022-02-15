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
                .url("http://localhost:" + serverPort+"/example/viewaccountsjson?name=Rachel")
                .build();

        try (Response rsp = client.newCall(req).execute()) {
            assertEquals("[{\"name\":\"Rachel\",\"balance\":50.0}]", rsp.body().string());
            assertEquals(StatusCode.OK.value(), rsp.code());
        }
    }

    @Test
    public void shouldPrintUsers(int serverPort) throws IOException {
        Request req = new Request.Builder()
                .url("http://localhost:" + serverPort+"/example/viewaccountsjson")
                .build();

        try (Response rsp = client.newCall(req).execute()) {
            assertEquals("[{\"name\":\"Rachel\",\"balance\":50.0}," +
                    "{\"name\":\"Monica\",\"balance\":100.0}," +
                    "{\"name\":\"Phoebe\",\"balance\":76.0}," +
                    "{\"name\":\"Joey\",\"balance\":23.9}," +
                    "{\"name\":\"Chandler\",\"balance\":3.0}," +
                    "{\"name\":\"Ross\",\"balance\":54.32}]", rsp.body().string());
            assertEquals(StatusCode.OK.value(), rsp.code());
        }
    }
}
