//package net.creeperhost.minetogether.oauth;
//
//import java.io.IOException;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.Base64;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.ExecutionException;
//
//import com.github.scribejava.core.model.OAuth2AccessToken;
//import com.github.scribejava.core.model.OAuthRequest;
//import com.github.scribejava.core.model.Response;
//import com.github.scribejava.core.model.Verb;
//import fi.iki.elonen.NanoHTTPD;
//
//import static java.nio.charset.StandardCharsets.UTF_8;
//// NOTE: If you're using NanoHTTPD >= 3.0.0 the namespace is different,
////       instead of the above import use the following:
//// import org.nanohttpd.NanoHTTPD;
//
//public class WebServer extends NanoHTTPD {
//
//    public WebServer(boolean daemon) throws IOException {
//        super(25815);
//        start(NanoHTTPD.SOCKET_READ_TIMEOUT, daemon);
//        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
//    }
//
//    @Override
//    public Response serve(IHTTPSession session) {
//        try {
//            return realServe(session);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Something went wrong! Please go back to MineTogether.");
//    }
//
//    private Response realServe(IHTTPSession session) throws InterruptedException, ExecutionException, IOException {
//        String msg = "<html><body><h1>Hello server</h1>\n";
//        Map<String, String> parms = session.getParms();
//
//        String tempCode = null;
//
//        System.out.println(session.getUri());
//
//        if ((tempCode = parms.get("code")) != null)
//        {
//            Main.code.set(tempCode);
//
//            System.out.println();
//
//            System.out.println("Trading the Authorization Code for an Access Token...");
//            final OAuth2AccessToken accessToken;
//            accessToken = Main.service.getAccessToken(tempCode);
//            System.out.println("Got the Access Token!");
//            System.out.println("(The raw response looks like this: " + accessToken.getRawResponse() + "')");
//
//            String session_state = accessToken.getParameter("session_state");
//            String clientId = "mt-mod";
//            String nonce = UUID.randomUUID().toString();
//            String provider = "minecraft";
//            System.out.println();
//
//            String input = nonce + session_state + clientId + provider;
//
//            MessageDigest md = null;
//            try {
//                md = MessageDigest.getInstance("SHA-256");
//            } catch (NoSuchAlgorithmException e) {
//                throw new RuntimeException(e);
//            }
//
//            byte[] check = md.digest(input.getBytes(UTF_8));
//            String hash =  Base64.getUrlEncoder().withoutPadding().encodeToString(check);
//
//            //final String protectedResourceUrl = "https://auth.minetogether.io/auth/realms/MineTogether/broker/minecraft/link?client_id="+clientId+"&redirect_uri="+"http://localhost:25815"+"&nonce="+nonce+"&hash="+hash;
//
//            final String protectedResourceUrl = "https://auth.minetogether.io/auth/realms/MineTogether/protocol/openid-connect/token";
//
//            System.out.println("Now we're going to access a protected resource...");
//            final OAuthRequest request = new OAuthRequest(Verb.GET, protectedResourceUrl);
//            Main.service.signRequest(accessToken, request);
//            com.github.scribejava.core.model.Response response = Main.service.execute(request);
//            System.out.println("Got it! Lets see what we found...");
//            System.out.println();
//            System.out.println(response.getCode());
//            System.out.println(response.getBody());
//            System.out.println(response.getHeader("Location"));
//            System.out.println();
//            System.out.println("Thats it man! Go and build something awesome with ScribeJava! :)");
//
//            /*Response response = newFixedLengthResponse(Response.Status.REDIRECT, NanoHTTPD.MIME_PLAINTEXT, "");
//            response.addHeader("Location", protectedResourceUrl);
//            return response;*/
//
//
//        }
//
//        return newFixedLengthResponse(msg + "</body></html>\n");
//    }
//}
////package net.creeperhost.minetogether.oauth;
////
////import java.io.IOException;
////import java.util.List;
////import java.util.Map;
////
////import fi.iki.elonen.NanoHTTPD;
////// NOTE: If you're using NanoHTTPD >= 3.0.0 the namespace is different,
//////       instead of the above import use the following:
////// import org.nanohttpd.NanoHTTPD;
////
////public class WebServer extends NanoHTTPD {
////
////    public WebServer(boolean daemon) throws IOException {
////        super(25815);
////        start(NanoHTTPD.SOCKET_READ_TIMEOUT, daemon);
////        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
////    }
////
////    @Override
////    public Response serve(IHTTPSession session) {
////        String msg = "<html><body><h1>Hello server</h1>\n";
////        Map<String, String> parms = session.getParms();
////
////        String tempCode = null;
////
////        if ((tempCode = parms.get("code")) != null)
////        {
////            Main.code.set(tempCode);
////        }
////
////        return newFixedLengthResponse(msg + "</body></html>\n");
////    }
////}