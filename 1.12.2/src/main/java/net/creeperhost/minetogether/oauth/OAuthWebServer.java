package net.creeperhost.minetogether.oauth;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import fi.iki.elonen.NanoHTTPD;

public class OAuthWebServer extends NanoHTTPD {

    BiConsumer<String, String> codeHandler;

    public OAuthWebServer(boolean daemon, int port) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, daemon);
        System.out.println("\nRunning! Point your browsers to http://localhost:" + port + "/ \n");
    }

    public void setCodeHandler(BiConsumer<String, String> handler) {
        codeHandler = handler;
    }

    @Override
    public Response serve(IHTTPSession session) {
        return realServe(session);
    }

    private Response realServe(IHTTPSession session) {
        Map<String, String> parms = session.getParms();

        String location = "https://minetogether.io/wut";

        String tempCode;
        if ((tempCode = parms.get("code")) != null)
        {
            codeHandler.accept(tempCode, parms.get("state"));
            location = "https://minetogether.io/clientloggedin";
        }
        Response response = newFixedLengthResponse("msg");
        response.addHeader("Location", location);
        response.setStatus(Response.Status.REDIRECT);
        return response;
    }
}