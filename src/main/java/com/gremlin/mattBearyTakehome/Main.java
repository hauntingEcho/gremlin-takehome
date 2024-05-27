/**
 * I've tried to exclude a lot of Java's directory structure best-practices just to keep this readable,
 * in a larger-scale application it'd be in a folder structure based on its package name
 */

package com.gremlin.mattBearyTakehome;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class Main {
    public static final Map<String, String> LANGUAGES = Map.ofEntries(
            Map.entry("Russian", "ru"),
            Map.entry("English", "en"));

    /**
     * Internal logic of the program, separated to simplify testing.
     * @param args CLI args passed to the program. Should be one argument, "English" or "Russian"
     * @param client HTTP client
     * @return text to print for the user
     * @throws UsageException invalid command-line usage
     */
    public static String runInternal(String[] args, OkHttpClient client) throws UsageException {
        /*
         * In a larger project, I'd probably use something like ArgParse4J or Apache-Commons-Cli.
         * I've foregone those for this project, just to keep things quickly readable.
         */
        String language = null;
        if (!(args == null) && args.length > 0) {
            language = LANGUAGES.getOrDefault(args[0], null);
        }

        if (language == null) {
            throw new UsageException();
        }

        var targetUrl = new HttpUrl.Builder()
                .scheme("http")
                .host("api.forismatic.com")
                .encodedPath("/api/1.0/")
                .addQueryParameter("method", "getQuote")
                .addQueryParameter("format", "json")
                .addQueryParameter("lang", language)
                .build();

        Request request = new Request.Builder()
                .url(targetUrl)
                .build();

        String responseBody;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("unsuccessful response code: " + response.code());
            }
            responseBody = response.body().string();
        } catch (Exception x) {
            throw new RuntimeException("Failed to retrieve response from server", x);
        }

        /*
         * In a project with more-intensive needs,
         * I'd probably reach for something with more fully-featured class deserialization like Jackson,
         * but find the org.json library more readable for light use like this.
         */
        JSONObject obj;
        try {
            obj = new JSONObject(responseBody);
            return obj.getString("quoteText") + "\n\n" + "- " + obj.getString("quoteAuthor");
        } catch (JSONException x) {
            throw new RuntimeException("Failed to parse JSON from server", x);
        }
    }

    public static void main(String[] args) {
        try {
            /*
             * In a larger project I'd reach for a fully-featured DI library like Spring.
             * For the purposes of this project though,
             * it seemed quicker and more readable to do it the old-fashioned way.
             */
            System.out.println(runInternal(args, new OkHttpClient()));
        } catch (UsageException x) {
            System.err.println(
                    "Usage: java -jar gremlin-takehome.jar lang\n" +
                            "lang must be one of the following: " +
                            String.join(", ", LANGUAGES.keySet().toArray(new String[0]))
            );
            System.exit(1);
        }
    }
}
