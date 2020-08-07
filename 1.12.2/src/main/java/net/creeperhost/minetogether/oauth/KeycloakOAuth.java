package net.creeperhost.minetogether.oauth;

import com.github.scribejava.apis.KeycloakApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.AccessTokenRequestParams;
import com.github.scribejava.core.oauth.AuthorizationUrlBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.*;
import net.creeperhost.minetogether.CreeperHost;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.concurrent.ExecutionException;


public class KeycloakOAuth {
    static OAuthWebServer server;
    public static void main(String[] args) {
        Random random = new Random();
        final String apiKey = "mt-ingame";
        final String baseUrl = "https://auth.minetogether.io/";
        final String realm = "MineTogether";
        final String secretState = "oHaiICanHazSecret" + random.nextInt(999999);


        final String protectedResourceUrl = "https://auth.minetogether.io/auth/realms/MineTogether/linksearch";

        int port = -1;
        if (server != null && server.isAlive())
        {
            server.stop();
        }
        server = null;
        for (int i = 0; i < 5 && server == null; i++)
        {
            try {
                port = 1000 + random.nextInt(64535);
                server = new OAuthWebServer(false, port);
            } catch (IOException ignored) {}
        }

        if (server == null) {
            // handle error here
            return;
        }

        final String callback = "http://localhost:" + port;

        final OAuth20Service service = new ServiceBuilder(apiKey)
                .apiSecret("d3b0c03e-4447-400b-ba48-e08902cd95d6")
                // Yes, we use a secret - because public doesn't work in Keycloak for some reason. But we use PKCE aswell. So no downside over public with PKCE.
                .defaultScope("openid")
                .callback(callback)
                .responseType("code")
                .build(KeycloakApi.instance(baseUrl, realm));


        final AuthorizationUrlBuilder authorizationUrlBuilder = service.createAuthorizationUrlBuilder()
                .state(secretState)
                .initPKCE();

        try {
            openURL(new URL(authorizationUrlBuilder.build()));
        } catch (MalformedURLException ignored) {
            server.stop();
            //ERROR, handle gui side
        }


        server.setCodeHandler((code, state) -> {
            try {
                if (!state.equals(secretState)) {
                    // error or something
                    return;
                }

                System.out.println();

                System.out.println("Trading the Authorization Code for an Access Token...");
                final OAuth2AccessToken accessToken;
                accessToken = service.getAccessToken(AccessTokenRequestParams.create(code)
                        .pkceCodeVerifier(authorizationUrlBuilder.getPkce().getCodeVerifier()));
                System.out.println("Got the Access Token!");
                System.out.println("(The raw response looks like this: " + accessToken.getRawResponse() + "')");

                System.out.println();

                System.out.println("Now we're going to access a protected resource...");
                final OAuthRequest request = new OAuthRequest(Verb.GET, protectedResourceUrl);
                service.signRequest(accessToken, request);
                com.github.scribejava.core.model.Response response = service.execute(request);
                System.out.println(response.getCode());
                if (response.getCode() != 200) {
                    // errorrrrrr
                    return;
                }
                JsonParser parser = new JsonParser();
                JsonElement parse;
                try {
                    parse = parser.parse(response.getBody());
                } catch (JsonParseException e) {
                    // errrorrrrrrrr
                    return;
                }
                if (parse.isJsonObject()) {
                    boolean doAuth = true;
                    try {
                        JsonObject profile = parse.getAsJsonObject();
                        if (profile.has("federatedIdentities")) {
                            JsonArray identities = profile.getAsJsonArray("federatedIdentities");
                            for(JsonElement identity: identities)
                            {
                                if (identity.isJsonObject() && identity.getAsJsonObject().has("identityProvider") && identity.getAsJsonObject().getAsJsonPrimitive("identityProvider").getAsString().equals("mcauth")) {
                                    // already have existing
                                    if (identity.getAsJsonObject().get("userId").getAsString().equals(Minecraft.getMinecraft().player.getCachedUniqueIdString())) {
                                        //doAuth = false;
                                    }
                                }
                            }
                        }
                    } catch (Throwable e) {
                        doAuth = true;
                    }

                    if (doAuth) {
                        ServerAuthTest.auth((authed, mcauthcode) -> {
                            if (authed) {
                                //if ()
                                OAuthRequest request2 = new OAuthRequest(Verb.POST, "https://auth.minetogether.io/auth/realms/MineTogether/linksearch/linkmc/" + mcauthcode);
                                service.signRequest(accessToken, request2);
                                try {
                                    com.github.scribejava.core.model.Response response2 = service.execute(request2);
                                    System.out.println(response2.getBody());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            return null;
                        });
                    }
                }
                closeServer();
            } catch (Exception e) {
                e.printStackTrace();
                closeServer();
                // errorrrrr
            }
        });
    }

    public static boolean openURL(URL url) {
        String[] cmdLine;

        Util.EnumOS os = Util.getOSType();

        switch(os) {
            case WINDOWS:
                cmdLine = new String[]{"rundll32", "url.dll,FileProtocolHandler", url.toString()};
                break;
            case OSX:
                cmdLine = new String[]{"open", url.toString()};
                break;
            default:
                cmdLine = new String[]{"xdg-open", url.toString()};
        }

        try {
            Process browserProcess = AccessController.doPrivileged((PrivilegedExceptionAction<Process>) () -> Runtime.getRuntime().exec(cmdLine));
            Iterator errorIterator = IOUtils.readLines(browserProcess.getErrorStream()).iterator();

            while(errorIterator.hasNext()) {
                String errorLine = (String)errorIterator.next();
                CreeperHost.logger.error(errorLine);
            }

            browserProcess.getInputStream().close();
            browserProcess.getErrorStream().close();
            browserProcess.getOutputStream().close();
            return true;
        } catch (IOException | PrivilegedActionException var5) {
            CreeperHost.logger.error("Couldn't open url '{}'", url, var5);
            return false;
        }
    }

    public static void closeServer() {
        Timer timer = new java.util.Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                server.stop();
                timer.cancel();
            }
        }, 1000);
    }
}
