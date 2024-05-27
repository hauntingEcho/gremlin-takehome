package com.gremlin.mattBearyTakehome;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;

public class MainTest {
    /**
     * An actual sample pulled from the English API
     */
    static final String ACTUAL_ENGLISH = "{\"quoteText\":\"I have just three things to teach: simplicity, patience, compassion. These three are your greatest treasures.\", \"quoteAuthor\":\"Lao Tzu\", \"senderName\":\"\", \"senderLink\":\"\", \"quoteLink\":\"http://forismatic.com/en/41ec043bcf/\"}";

    /**
     * An actual sample pulled from the Russian API
     */
    static final String ACTUAL_RUSSIAN = "{\"quoteText\":\"Традиция — каменная граница, воздвигнутая прошлым вокруг настоящего, кто хочет проникнуть в будущее, должен перешагнуть ее. \", \"quoteAuthor\":\"Стефан Цвейг\", \"senderName\":\"\", \"senderLink\":\"\", \"quoteLink\":\"http://forismatic.com/ru/121e3d2372/\"}";

    private OkHttpClient buildMockClient(String response) {
        try {
            var mockResponseBody = Mockito.mock(ResponseBody.class);
            Mockito.when(mockResponseBody.string()).thenReturn(response);
            var mockResponse = Mockito.mock(Response.class);
            Mockito.when(mockResponse.body()).thenReturn(mockResponseBody);
            Mockito.when(mockResponse.isSuccessful()).thenReturn(true);
            var mockCall = Mockito.mock(Call.class);
            Mockito.when(mockCall.execute()).thenReturn(mockResponse);
            var mockClient = Mockito.mock(OkHttpClient.class);
            Mockito.when(mockClient.newCall(any())).thenReturn(mockCall);
            return mockClient;
        } catch (Exception x) {
            throw new RuntimeException("Internal exception while building mocks", x);
        }
    }

    @Test
    public void testRussian() throws Exception {
        var mockClient = buildMockClient(ACTUAL_RUSSIAN);
        Assertions.assertEquals(
                "Традиция — каменная граница, воздвигнутая прошлым вокруг настоящего, кто хочет проникнуть в будущее, должен перешагнуть ее. \n\n- Стефан Цвейг",
                Main.runInternal(new String[]{"Russian"}, mockClient));
        Mockito.verify(mockClient).newCall(ArgumentMatchers.assertArg(req -> Assertions.assertEquals("ru", req.url().queryParameter("lang"))));
    }

    @Test
    public void testEnglish() throws Exception {
        var mockClient = buildMockClient(ACTUAL_ENGLISH);
        Assertions.assertEquals(
                "I have just three things to teach: simplicity, patience, compassion. These three are your greatest treasures.\n\n- Lao Tzu",
                Main.runInternal(new String[]{"English"}, mockClient));
        Mockito.verify(mockClient).newCall(ArgumentMatchers.assertArg(req -> Assertions.assertEquals("en", req.url().queryParameter("lang"))));
    }

    @Test
    public void testInvalidJson() {
        var mockClient = buildMockClient("{asdf");
        Assertions.assertThrows(
                RuntimeException.class,
                () -> Main.runInternal(new String[]{"English"}, mockClient),
                "Didn't throw expected exception");
    }

    @Test
    public void testNullJson() {
        var mockClient = buildMockClient(null);
        Assertions.assertThrows(
                RuntimeException.class,
                () -> Main.runInternal(new String[]{"English"}, mockClient),
                "Didn't throw expected exception");
    }

    @Test
    public void testEmptyStringJson() {
        var mockClient = buildMockClient("");
        Assertions.assertThrows(
                RuntimeException.class,
                () -> Main.runInternal(new String[]{"English"}, mockClient),
                "Didn't throw expected exception");
    }

    @Test
    public void testEmptyJson() {
        var mockClient = buildMockClient("{}");
        Assertions.assertThrows(
                RuntimeException.class,
                () -> Main.runInternal(new String[]{"English"}, mockClient),
                "Didn't throw expected exception");
    }

    @Test
    public void testRequestFailure() {
        OkHttpClient mockClient;
        try {
            var mockResponse = Mockito.mock(Response.class);
            Mockito.when(mockResponse.code()).thenReturn(500);
            Mockito.when(mockResponse.isSuccessful()).thenReturn(false);
            var mockCall = Mockito.mock(Call.class);
            Mockito.when(mockCall.execute()).thenReturn(mockResponse);
            mockClient = Mockito.mock(OkHttpClient.class);
            Mockito.when(mockClient.newCall(any())).thenReturn(mockCall);
        } catch (Exception x) {
            throw new RuntimeException("Internal exception while building mocks", x);
        }

        Assertions.assertThrows(
                RuntimeException.class,
                () -> Main.runInternal(new String[]{"English"}, mockClient),
                "Didn't throw expected exception");
    }

    @Test
    public void testRequestError() {
        OkHttpClient mockClient;
        try {
            var mockCall = Mockito.mock(Call.class);
            Mockito.when(mockCall.execute()).thenThrow(new RuntimeException("It broke!"));
            mockClient = Mockito.mock(OkHttpClient.class);
            Mockito.when(mockClient.newCall(any())).thenReturn(mockCall);
        } catch (Exception x) {
            throw new RuntimeException("Internal exception while building mocks", x);
        }

        Assertions.assertThrows(
                RuntimeException.class,
                () -> Main.runInternal(new String[]{"English"}, mockClient),
                "Didn't throw expected exception");
    }
}
