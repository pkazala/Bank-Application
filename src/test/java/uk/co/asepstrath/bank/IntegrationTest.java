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

/* @Test
   public void shouldPrintUsers(int serverPort) throws IOException {
     Request req = new Request.Builder()
          .url("http://localhost:" + serverPort+"/example/viewaccountsjson")
          .build();

     try (Response rsp = client.newCall(req).execute()) {
         assertEquals("[{\"id\":\"e495e94e-c431-462e-9c96-4b4971e52c6a\",\"name\":\"Tyrese Sanford\",\"balance\":781461.25,\"currency\":\"TTD\",\"accountType\":\"Home Loan Account\"}," +
                 "{\"id\":\"0a52e00c-1df2-499e-9fc3-5a5098f7effd\",\"name\":\"Armand McLaughlin\",\"balance\":380576.1,\"currency\":\"JPY\",\"accountType\":\"Home Loan Account\"}," +
                 "{\"id\":\"7a60a882-0701-4dfb-b2e9-da25428ae441\",\"name\":\"Adeline McGlynn\",\"balance\":68066.35,\"currency\":\"PAB\",\"accountType\":\"Investment Account\"}," +
                 "{\"id\":\"51983f0e-00f2-47ec-b67e-ca9c033b74de\",\"name\":\"Dillan Jenkins\",\"balance\":939637.1,\"currency\":\"CLP\",\"accountType\":\"Credit Card Account\"}," +
             "{\"id\":\"26befae3-fca0-4a10-afee-963a76e9ba13\",\"name\":\"Mia Auer\",\"balance\":171471.7,\"currency\":\"PHP\",\"accountType\":\"Auto Loan Account\"}," +
             "{\"id\":\"93f064b1-f712-4745-9d59-ef8c02d6d556\",\"name\":\"Hailee Rempel\",\"balance\":485058.47,\"currency\":\"GIP\",\"accountType\":\"Checking Account\"}," +
                 "{\"id\":\"0c384faa-8100-409f-ad1d-c34cb32d79a1\",\"name\":\"Jovanny Legros\",\"balance\":874803.25,\"currency\":\"GNF\",\"accountType\":\"Personal Loan Account\"}," +
             "{\"id\":\"6ff3ec26-8fb1-4c2f-a74d-98f6f513e77e\",\"name\":\"Ava Moen\",\"balance\":265848.8,\"currency\":\"LVL\",\"accountType\":\"Auto Loan Account\"}," +
             "{\"id\":\"9aefd31a-48f7-4de7-9a7a-2f4ee0e345d3\",\"name\":\"Richard Orn\",\"balance\":348455.22,\"currency\":\"KPW\",\"accountType\":\"Checking Account\"}," +
                 "{\"id\":\"bf6d3279-a96a-4a15-9233-791766f9ef58\",\"name\":\"Conner Lubowitz\",\"balance\":551152.75,\"currency\":\"LAK\",\"accountType\":\"Home Loan Account\"}," +
             "{\"id\":\"6ca1dda0-6e83-4bf3-be22-705829c32120\",\"name\":\"Kimberly Roberts\",\"balance\":172870.44,\"currency\":\"UAH\",\"accountType\":\"Auto Loan Account\"}," +
             "{\"id\":\"ccfe1808-8235-4042-8d7a-bb7dfa81ef7d\",\"name\":\"Adell Hills\",\"balance\":158969.1,\"currency\":\"EGP\",\"accountType\":\"Investment Account\"}," +
                 "{\"id\":\"d23888c6-b8e5-440c-bbc6-a49910e7c630\",\"name\":\"May Denesik\",\"balance\":746543.56,\"currency\":\"GEL\",\"accountType\":\"Home Loan Account\"}," +
             "{\"id\":\"a5a98686-6003-41cb-9ea1-0d49a94ee29e\",\"name\":\"Cornelius Schaefer\",\"balance\":613276.75,\"currency\":\"BRL\",\"accountType\":\"Home Loan Account\"}," +
             "{"id":"7b3b64f0-a92a-42d1-8444-772b7cbf6f91","name":"Jermain McCullough","balance":741776.75,"currency":"YER","accountType":"Checking Account"}," +
                 "{"id":"87583e06-8608-44d0-94ea-0e49a79ea6f5","name":"Cleveland Kiehn","balance":389561.44,"currency":"BND","accountType":"Investment Account"}," +
                 "{"id":"be4ee393-d802-4edd-ab9c-cefd7edf908d","name":"Gail Buckridge","balance":810277.94,"currency":"SOS","accountType":"Auto Loan Account"}," +
             "{"id":"2966ab54-34a6-4185-844c-4cf6fe12e26b","name":"Torrance Schulist","balance":293039.03,"currency":"MWK","accountType":"Home Loan Account"}," +
             "{"id":"6501a903-4a81-487e-90c0-2abf23cafad8","name":"Dedric Mueller","balance":773395.75,"currency":"SYP","accountType":"Checking Account"}," +
                 "{"id":"5d36ba45-89c9-4c53-956a-cc58bdf95c63","name":"Lorenzo Cummerata","balance":467842.5,"currency":"KYD","accountType":"Investment Account"}," +
                 "{"id":"3ce5c877-b701-47dd-927b-66f8beb8c1f7","name":"Kailee Rodriguez","balance":148141.8,"currency":"SOS","accountType":"Credit Card Account"}," +
             "{"id":"c2a8fdce-76b0-4e95-a49a-a0ffdba79160","name":"Verla Kunde","balance":728866.44,"currency":"DZD","accountType":"Personal Loan Account"}," +
             "{"id":"e8b8056a-ea26-450e-bda2-83260a040b27","name":"Marge Raynor","balance":378398.28,"currency":"NZD","accountType":"Home Loan Account"}," +
             "{"id":"5044a4c0-476c-4858-ad63-0284da98ab5a","name":"Adrain Ortiz","balance":380199.3,"currency":"RON","accountType":"Credit Card Account"}," +
             "{"id":"7fd45a98-96eb-4777-8525-6696ded416bd","name":"Alexa Ferry","balance":43806.89,"currency":"PKR","accountType":"Money Market Account"}," +
             "{"id":"c06dab69-73f5-4e5c-8d0e-e4c876b341e2","name":"Kevin Nitzsche","balance":585422.1,"currency":"MVR","accountType":"Personal Loan Account"}," +
             "{"id":"a4f2f2f6-0b90-4cff-a033-b1df9f814276","name":"Genesis Cormier","balance":312190.4,"currency":"AFN","accountType":"Investment Account"}," +
                 "{"id":"e593759f-4664-4b50-a691-f704a33de9c6","name":"Mafalda Walker","balance":843047.9,"currency":"HTG","accountType":"Credit Card Account"}," +
             "{"id":"e74ee746-e580-402d-bd17-601af0fc8150","name":"Lambert Homenick","balance":238904.52,"currency":"MUR","accountType":"Auto Loan Account"}," +
             "{"id":"caef9b96-46eb-48e1-a8a4-cf71d01b575c","name":"Nola Dickinson","balance":682829.0,"currency":"GBP","accountType":"Checking Account"}," +
                 "{"id":"6d706209-ea70-45e5-80ad-e089a29f1faf","name":"Oliver Kertzmann","balance":133067.31,"currency":"USD","accountType":"Checking Account"}," +
                 "{"id":"4ca1d85a-674d-4127-a692-191b41c0c3e0","name":"Muhammad Goldner","balance":847936.94,"currency":"KPW","accountType":"Auto Loan Account"}," +
             "{"id":"57401351-52d1-47dc-bac5-27cc8689b6af","name":"Viva Macejkovic","balance":983738.25,"currency":"STN","accountType":"Auto Loan Account"}," +
             "{"id":"c501a3f5-f795-45ad-96b5-13ebff0c738a","name":"Stefanie Smitham","balance":151377.7,"currency":"BHD","accountType":"Credit Card Account"}," +
             "{"id":"0a340206-a7bc-4a65-a03f-4ae2ac6ffe09","name":"Ashleigh Ryan","balance":256545.17,"currency":"ZAR","accountType":"Investment Account"}," +
                 "{"id":"f3640bbe-a708-4f44-b995-4baacc7839f5","name":"Nola Rutherford","balance":60764.23,"currency":"GHS","accountType":"Money Market Account"}," +
             "{"id":"4dd024a8-79aa-42d4-a049-6e8e2b1a386d","name":"Blaise Sanford","balance":48554.58,"currency":"TJS","accountType":"Savings Account"}," +
                 "{"id":"3b8cda4c-95b4-4543-8004-36f3831f6dd1","name":"George Reichel","balance":564735.44,"currency":"MRO","accountType":"Home Loan Account"}," +
             "{"id":"31f278df-423c-4bd7-8091-9038cf61c4a3","name":"Stone Tremblay","balance":828208.56,"currency":"SRD","accountType":"Personal Loan Account"}," +
             "{"id":"ff287706-3a27-4f26-b3a4-4e2f0fec469e","name":"Martina Mills","balance":356719.8,"currency":"NOK","accountType":"Home Loan Account"}," +
             "{"id":"add4cb46-4b49-4b22-bf6c-222792029813","name":"Maybelle Grimes","balance":925631.06,"currency":"SLL","accountType":"Money Market Account"}," +
             "{"id":"58cf4041-3278-4020-9839-97491bdbc027","name":"Nicholaus Kuvalis","balance":19709.86,"currency":"SDG","accountType":"Savings Account"}," +
                 "{"id":"a482152c-b9ef-4c4c-8c6c-d8bc07172a86","name":"Daren Rogahn","balance":807890.25,"currency":"JOD","accountType":"Money Market Account"}," +
             "{"id":"6cae78ad-2e4b-459b-abaf-a346a19d92d8","name":"Javier Zemlak","balance":691932.75,"currency":"TWD","accountType":"Credit Card Account"}," +
             "{"id":"87c03b4d-fdd4-49a3-921d-d42a40bedd33","name":"Mittie Wolf","balance":763918.44,"currency":"GEL","accountType":"Credit Card Account"}," +
             "{"id":"067c6b71-1903-472d-99e4-2c2f13b4ffce","name":"Wilhelmine Mitchell","balance":142392.66,"currency":"BIF","accountType":"Checking Account"}," +
                 "{"id":"820ab355-1df1-4723-af23-831cdc0f931b","name":"Rosalinda Wehner","balance":996562.2,"currency":"HTG","accountType":"Personal Loan Account"}," +
             "{"id":"962fc30b-3203-45d4-8fd6-308ea23744cb","name":"Jeremie Kozey","balance":208505.45,"currency":"XPD","accountType":"Personal Loan Account"}," +
             "{"id":"9e5d4486-82ad-42a2-b2ec-19aa0028cc7f","name":"Maxime Kirlin","balance":54570.57,"currency":"LKR","accountType":"Credit Card Account"}," +
             "{"id":"a00dcfe6-096f-4663-aacf-0b58c3070230","name":"Christiana Gutkowski","balance":491153.28,"currency":"BYR","accountType":"Savings Account"}," +
                 "{"id":"857b2479-81d5-4c90-80c9-c7c0cfea4326","name":"Eunice Hoeger","balance":26260.43,"currency":"SBD","accountType":"Savings Account"}," +
                 "{"id":"b3bd1b4d-0cdd-4239-94bf-1d53e750cbdf","name":"Modesto Kutch","balance":895787.2,"currency":"BYR","accountType":"Investment Account"}," +
                 "{"id":"ed410558-b490-4d7f-8060-1816c466dea5","name":"Gardner Cummerata","balance":99106.28,"currency":"PLN","accountType":"Money Market Account"}," +
             "{"id":"3eb287cc-90d7-45bb-8f3e-7004f9173430","name":"Rozella Murphy","balance":401030.4,"currency":"XBB","accountType":"Investment Account"}," +
                 "{"id":"b259383e-248f-49fe-b27a-e371f3e09f59","name":"Jordane Huel","balance":532648.44,"currency":"TOP","accountType":"Savings Account"}," +
                 "{"id":"5788a8e9-d933-4076-9088-3e629114b08a","name":"Chandler Prohaska","balance":714546.56,"currency":"IDR","accountType":"Personal Loan Account"}," +
             "{"id":"c879e2a8-1719-4f54-bd63-7f714e68db55","name":"Yoshiko Maggio","balance":815962.94,"currency":"KGS","accountType":"Checking Account"}," +
                 "{"id":"eafadf9c-5e08-46e6-bb0c-67ef10c35eb9","name":"Clovis Kreiger","balance":993590.6,"currency":"BSD","accountType":"Checking Account"}," +
                 "{"id":"f1c2f3c2-c12d-4dda-a6f0-fb1f85ec6fb1","name":"Zelda Jast","balance":497713.6,"currency":"BSD","accountType":"Checking Account"}," +
                 "{"id":"3999a951-9eb9-44bc-b9f6-a4bebe647239","name":"Lysanne Hartmann","balance":974836.0,"currency":"AFN","accountType":"Savings Account"}," +
                 "{"id":"715b6562-1e0f-428b-a3bf-712cc6575803","name":"Arnaldo Dooley","balance":751804.7,"currency":"KZT","accountType":"Savings Account"}," +
                 "{"id":"7b5bf4a9-b2c0-4273-89c9-78d34a0b9115","name":"Lafayette Kohler","balance":211187.44,"currency":"ARS","accountType":"Auto Loan Account"}," +
             "{"id":"f187ac29-60b6-41e6-807c-9506a2b4660b","name":"Karlee Reilly","balance":894846.25,"currency":"CUP","accountType":"Home Loan Account"}," +
             "{"id":"c021f126-41bd-405e-9519-6963818b230a","name":"Marlee Greenholt","balance":731900.75,"currency":"MDL","accountType":"Auto Loan Account"}," +
             "{"id":"97f9a400-d665-4f01-8939-def884e9ac49","name":"Kieran Lehner","balance":386206.78,"currency":"CUC","accountType":"Savings Account"}," +
                 "{"id":"a17764a3-4c43-4ce2-a881-8013974b5fa4","name":"Helga Weissnat","balance":521170.1,"currency":"KGS","accountType":"Auto Loan Account"}," +
             "{"id":"69fd4c63-e1e6-442d-93a8-253e9005714a","name":"Everette Breitenberg","balance":936322.0,"currency":"ANG","accountType":"Savings Account"}," +
                 "{"id":"7af44f07-4b97-4828-874e-c72f16a399d1","name":"Lilliana McClure","balance":56904.13,"currency":"LAK","accountType":"Checking Account"}," +
                 "{"id":"ddf36a4c-1d35-45cc-b1e0-4ce37e049b07","name":"Narciso Willms","balance":788230.3,"currency":"THB","accountType":"Personal Loan Account"}," +
             "{"id":"f7d21f7a-5019-449e-a737-209e2af404f6","name":"Angus Bechtelar","balance":980689.2,"currency":"SGD","accountType":"Money Market Account"}," +
             "{"id":"52fd696f-0f87-4246-9c9a-6603af421605","name":"Janiya D\u0027Amore","balance":976237.75,"currency":"SHP","accountType":"Savings Account"}," +
                 "{"id":"d65ba31c-c5e0-49fc-9815-eeb0ddfdbaa8","name":"Eldred Kreiger","balance":792816.8,"currency":"SRD","accountType":"Savings Account"}," +
                 "{"id":"8c57b1bb-8338-4bb0-940b-aab2134e3cff","name":"Enola Collier","balance":986547.3,"currency":"HKD","accountType":"Personal Loan Account"}," +
             "{"id":"bd9b7564-248c-4380-acc7-ef7f8c044598","name":"Maxime Leannon","balance":317393.62,"currency":"MOP","accountType":"Credit Card Account"}," +
             "{"id":"5e41b67d-1a9d-4c08-bbf4-631c4f6dcb84","name":"Sven Balistreri","balance":672556.0,"currency":"BTN","accountType":"Checking Account"}," +
                 "{"id":"193876e2-9369-4f07-b7c7-e97347cd2339","name":"Geovanni Blick","balance":721108.1,"currency":"AUD","accountType":"Investment Account"}," +
                 "{"id":"31b73016-0310-4211-b1c4-d0467aa3161b","name":"Reynold Jaskolski","balance":575955.4,"currency":"SHP","accountType":"Checking Account"}," +
                 "{"id":"c10f2897-dfe1-40e3-85fc-6f66e64b664e","name":"Effie Kohler","balance":430880.03,"currency":"XCD","accountType":"Money Market Account"}," +
             "{"id":"fcb3b6e1-d4f9-4777-9a52-635fbb590a67","name":"Marilie Vandervort","balance":275566.84,"currency":"KRW","accountType":"Auto Loan Account"}," +
             "{"id":"b6597ba6-e417-40c2-88b6-b96234c32965","name":"Juvenal Schmeler","balance":681429.4,"currency":"CNY","accountType":"Checking Account"}," +
                 "{"id":"0062f74c-1fd6-4e0d-9213-285a56772069","name":"Pierce Koepp","balance":957726.5,"currency":"KRW","accountType":"Checking Account"}," +
                 "{"id":"5486bb77-8c59-409d-8ed7-13b4109045ea","name":"Amara Schultz","balance":325712.2,"currency":"PGK","accountType":"Checking Account"}," +
                 "{"id":"ccb3b974-a14a-4683-80c4-a6ff7f54df3f","name":"Karina Krajcik","balance":528497.75,"currency":"TMT","accountType":"Auto Loan Account"}," +
             "{"id":"58e285d6-6394-487b-a99c-213163636e89","name":"Ara Wisozk","balance":438040.28,"currency":"ALL","accountType":"Investment Account"}," +
                 "{"id":"f5a748ec-a34d-4ebb-acd8-164d5fcc8988","name":"Hugh King","balance":184740.6,"currency":"MVR","accountType":"Money Market Account"}," +
             "{"id":"30505486-274a-4e25-99bc-70f3b8767e6e","name":"Joe Ullrich","balance":663869.2,"currency":"BIF","accountType":"Checking Account"}," +
                 "{"id":"67282eb2-c8bf-4f90-9211-923f3ab8e527","name":"Rodrigo Wilderman","balance":293351.38,"currency":"DZD","accountType":"Personal Loan Account"}," +
             "{"id":"adc8af77-d6fa-4c96-be6c-ad3f82b9d166","name":"Martina Schoen","balance":39663.34,"currency":"LKR","accountType":"Auto Loan Account"}," +
             "{"id":"94ad1089-fca0-45fc-82f8-94cd2744e75f","name":"Adrianna Gislason","balance":807872.9,"currency":"JPY","accountType":"Savings Account"}," +
                 "{"id":"685f20f6-304c-4215-afc1-4226bda4289c","name":"Guiseppe Altenwerth","balance":632857.3,"currency":"SAR","accountType":"Home Loan Account"}," +
             "{"id":"af121343-9c7c-4d5d-8dbc-bfedfd806913","name":"Marie White","balance":692292.75,"currency":"VND","accountType":"Auto Loan Account"}," +
             "{"id":"d2e057f8-325c-4c40-9bfb-88d4f3d309c8","name":"Brian Kunze","balance":140047.77,"currency":"SBD","accountType":"Home Loan Account"}," +
             "{"id":"f4dd307a-f572-4b3d-9751-90179578a003","name":"Jenifer Schulist","balance":689809.25,"currency":"MUR","accountType":"Home Loan Account"}," +
             "{"id":"e4f8b89d-830a-4f43-8c5d-e4fc1629f106","name":"Muhammad Luettgen","balance":703801.1,"currency":"TWD","accountType":"Money Market Account"}," +
             "{"id":"ed7f3290-e051-4b54-b8de-54e4b77b2e83","name":"Jena Flatley","balance":232051.95,"currency":"NPR","accountType":"Credit Card Account"}," +
             "{"id":"2284210a-aec3-4b2c-8422-01ae9c57bb71","name":"Minerva Rogahn","balance":971594.9,"currency":"TWD","accountType":"Checking Account"}," +
                 "{"id":"63a74cb4-f1d4-4dc3-b67b-29982e476c1e","name":"Justice Weimann","balance":312935.1,"currency":"KWD","accountType":"Savings Account"}," +
                 "{"id":"53f7e557-bc1b-4ba7-b3fa-9d7c23e217d3","name":"Fredrick Feeney","balance":26138.4,"currency":"MKD","accountType":"Auto Loan Account"}," +
             "{"id":"41ce49bc-c074-410f-a66e-46a48dcc9fd5","name":"Karl Gorczany","balance":678575.75,"currency":"MDL","accountType":"Savings Account"}," +
                 "{"id":"7bd1e32a-6431-4ede-aab3-43ac32d558ab","name":"Emmanuelle Marks","balance":29168.46,"currency":"ZMK","accountType":"Auto Loan Account"}]",rsp.body().string());
          //assertEquals( Controller.getAccountsSQL();, rsp.body().string());
          assertEquals(StatusCode.OK.value(), rsp.code());
     }
   }*/



}
