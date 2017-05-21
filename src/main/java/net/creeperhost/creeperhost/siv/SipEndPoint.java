package net.creeperhost.creeperhost.siv;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Koen Beckers (K-4U)
 *         Modified for use in SIV
 */
public class SipEndPoint {
    
    /**
     * The target address and port
     */
    private InetSocketAddress   address;
    /**
     * <code>null</code> if no successful request has been sent, otherwise a Map
     * containing any metadata received except the player list
     */
    private Map<String, String> values;
    /**
     * <code>null</code> if no successful request has been sent, otherwise an
     * array containing all online player usernames
     */
    private String[]            onlineUsernames;
    
    private Map<String, Object> extendedValues;
    
    /**
     * Convenience constructor
     *
     * @param host The target host
     * @param port The target port
     * @see SipEndPoint#SipEndPoint(InetSocketAddress)
     */
    public SipEndPoint(String host, int port) {
        
        this(new InetSocketAddress(host, port));
    }
    
    /**
     * Create a new instance of this class
     *
     * @param address The servers IP-address
     */
    public SipEndPoint(InetSocketAddress address) {
        
        this.address = address;
    }
    
    /**
     * Get the additional values if the Query has been sent
     *
     * @return The data
     * @throws IllegalStateException if the query has not been sent yet or there has been an error
     */
    public Map<String, String> getValues() {
        
        if (values == null)
            throw new IllegalStateException("Query has not been sent yet!");
        else
            return values;
    }
    
    /**
     * Get the online usernames if the Query has been sent
     *
     * @return The username array
     * @throws IllegalStateException if the query has not been sent yet or there has been an error
     */
    public String[] getOnlineUsernames() {
        
        if (onlineUsernames == null)
            throw new IllegalStateException("Query has not been sent yet!");
        else
            return onlineUsernames;
    }
    
    
    public void requestExtendedInfo(EnumSIPValues... values) throws IOException {
        
        Socket socket = new Socket(this.address.getAddress(), this.address.getPort());
        try {
            socket.setSoTimeout(5000);
            
            //Setup json:
            List<String> toRequest = new ArrayList<String>();
            for (EnumSIPValues value : values) {
                toRequest.add(value.toString());
            }
            
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            String endString = gson.toJson(toRequest);
            
            PrintWriter os = new PrintWriter(socket.getOutputStream());
            os.write(endString + "\n");
            os.flush();
            
            //Now wait for data:
            BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            String json = "";
            while ((line = is.readLine()) != null) {
                json += line;
            }
    
            Type myTypeMap = new TypeToken<Map<String,Object>>(){}.getType();
            extendedValues = gson.fromJson(json, myTypeMap);
        } finally {
            socket.close();
        }
    }
    
    
    public <T> T getExtendedObject(EnumSIPValues key, Class<T> clazz) {
        if(extendedValues != null) {
            if (extendedValues.containsKey(key.toString())) {
                //Fuck it:
                try {
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    String endString = gson.toJson(extendedValues.get(key.toString()));
                    return gson.fromJson(endString, clazz);
                }catch (JsonSyntaxException e){
                    e.printStackTrace();
                }
            } else {
                return null;
            }
        }
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
