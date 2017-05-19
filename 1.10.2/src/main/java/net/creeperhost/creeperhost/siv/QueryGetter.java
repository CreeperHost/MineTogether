package net.creeperhost.creeperhost.siv;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author Koen Beckers (K-4U)
 */
public class QueryGetter {
    
    private ExtendedServerData extendedServerData;
    private SipEndPoint        sipEndPoint;

    public QueryGetter(InetSocketAddress address){
        this(address.getAddress().getHostAddress(), address.getPort());
    }
    
    public QueryGetter(String host, int port) {
        //Find the actual port.
        Query query = new Query(host, port);
        try {
            query.receivePort();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        int tcpPort = query.getTcpPort();
        
        sipEndPoint = new SipEndPoint(host, tcpPort);
        this.extendedServerData = new ExtendedServerData();
        this.extendedServerData.setHasData(false);
    }

    public void run(){
        try {
            if(extendedServerData.isRequesting())
                return;
            extendedServerData.setRequesting(true);
            sipEndPoint.requestExtendedInfo(EnumSIPValues.TPS, EnumSIPValues.DIMENSIONS, EnumSIPValues.ENTITIES);
            extendedServerData.setHasData(true);

            /*LinkedTreeMap<String, String> time = (LinkedTreeMap<String, String>) sipEndPoint.getExtendedObject(EnumSIPValues.TIME);
            LinkedTreeMap<String, String> weather = (LinkedTreeMap<String, String>) sipEndPoint.getExtendedObject(EnumSIPValues.WEATHER);
            if(time == null) return;
            if(weather == null) return;
            if(sipEndPoint.getExtendedObject(EnumSIPValues.DAYNIGHT) == null) return;
            extendedServerData.setTime(time.get("0"));
            extendedServerData.setIsDay((Boolean) sipEndPoint.getExtendedObject(EnumSIPValues.DAYNIGHT));
            extendedServerData.setWeather(weather.get("0"));*/
            extendedServerData.setTPSList(sipEndPoint.getExtendedObject(EnumSIPValues.TPS, extendedServerData.getTpsList().getClass()));
            extendedServerData.setDimensions(sipEndPoint.getExtendedObject(EnumSIPValues.DIMENSIONS, extendedServerData.getDimensions().getClass()));
            extendedServerData.setEntities(sipEndPoint.getExtendedObject(EnumSIPValues.ENTITIES, extendedServerData.getEntities().getClass()));
//            extendedServerData.setTileEntities(sipEndPoint.getExtendedObject(EnumSIPValues.TILES, extendedServerData.getTileEntities().getClass()));
            //TODO: Rewrite the endpoints so that they can easily be requested
            extendedServerData.setRequesting(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public ExtendedServerData getExtendedServerData() {
        
        return extendedServerData;
    }

}
