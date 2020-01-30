package net.creeperhost.minetogether.oauth;

import com.github.scribejava.apis.KeycloakApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    static AtomicReference<String> code = new AtomicReference<>(null);

    public static void oauth() throws IOException, InterruptedException, ExecutionException {
        try {
            new WebServer(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final String apiKey = "mt-mod";
        final String apiSecret = "918801c4-d118-47b8-8a87-b06d28735e5b";
        final String callback = "http://localhost:25815";
        final String baseUrl = "https://auth.minetogether.io/";
        final String realm = "MineTogether";

        final String protectedResourceUrl = "https://auth.minetogether.io/auth/realms/MineTogether/linksearch";

        final OAuth20Service service = new ServiceBuilder(apiKey)
                .apiSecret(apiSecret)
                .scope("openid")
                .callback(callback)
                .build(KeycloakApi.instance(baseUrl, realm));

        System.out.println("=== Keyloack's OAuth Workflow ===");
        System.out.println();

        // Obtain the Authorization URL
        System.out.println("Fetching the Authorization URL...");
        final String authorizationUrl = service.getAuthorizationUrl();
        System.out.println("Got the Authorization URL!");
        System.out.println("Now go and authorize ScribeJava here:");
        System.out.println(authorizationUrl);
        System.out.println("Waiting for code");
        System.out.print(">>");

        String tempCode = null;

        while ((tempCode = code.get()) == null) {
            Thread.sleep(1000);
        }

        System.out.println();

        System.out.println("Trading the Authorization Code for an Access Token...");
        final OAuth2AccessToken accessToken = service.getAccessToken(tempCode);
        System.out.println("Got the Access Token!");
        System.out.println("(The raw response looks like this: " + accessToken.getRawResponse() + "')");
        System.out.println();

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access a protected resource...");
        final OAuthRequest request = new OAuthRequest(Verb.GET, protectedResourceUrl);
        service.signRequest(accessToken, request);
        Response response = service.execute(request);
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response.getCode());
        System.out.println(response.getBody());
        System.out.println();
        System.out.println("Thats it man! Go and build something awesome with ScribeJava! :)");
    }
}
