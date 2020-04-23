//package net.creeperhost.minetogether.oauth;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//
//import fi.iki.elonen.NanoHTTPD;
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
//        String msg = "<html><body><h1>Hello server</h1>\n";
//        Map<String, String> parms = session.getParms();
//
//        String tempCode = null;
//
//        if ((tempCode = parms.get("code")) != null)
//        {
//            Main.code.set(tempCode);
//        }
//
//        return newFixedLengthResponse(msg + "</body></html>\n");
//    }
//}