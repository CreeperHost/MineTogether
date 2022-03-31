//package net.creeperhost.minetogethergui.gif;
//
//import java.io.IOException;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
//public class ImageUtils
//{
//    public static boolean isImageUrl(URL url) throws IOException
//    {
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("HEAD");
//        String contentType = connection.getContentType();
//        return contentType.startsWith("image/");
//    }
//
//    public static String getContentType(URL url) throws IOException
//    {
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("HEAD");
//        return connection.getContentType();
//    }
//}
