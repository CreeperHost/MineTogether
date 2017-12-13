package net.creeperhost.minetogether.siv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Koen Beckers (K-4U)
 */
public class ExtendedServerData {
    
    private boolean hasData;
    private boolean isDay;
    private String                             time         = "";
    private String                             weather      = "";
    private boolean                            requesting   = false;
    private Map<Object, Map<String, Double>>   tpsList      = new HashMap<Object, Map<String, Double>>();
    private Map<String, Double>                dimensions   = new HashMap<String, Double>();
    private Map<Integer, Map<String, Integer>> entities     = new HashMap<Integer, Map<String, Integer>>();
    private Map<Integer, Map<String, Integer>> tileEntities = new HashMap<Integer, Map<String, Integer>>();
    
    public boolean isDay() {
        
        return isDay;
    }
    
    public void setIsDay(boolean isDay) {
        
        this.isDay = isDay;
    }
    
    public String getTime() {
        
        return time;
    }
    
    public void setTime(String time) {
        
        this.time = time;
    }
    
    public boolean isHasData() {
        
        return hasData;
    }
    
    public void setHasData(boolean hasData) {
        
        this.hasData = hasData;
    }
    
    public String getWeather() {
        
        return weather;
    }
    
    public void setWeather(String weather) {
        
        this.weather = weather;
    }
    
    public boolean isRequesting() {
        
        return requesting;
    }
    
    public void setRequesting(boolean requesting) {
        
        this.requesting = requesting;
    }
    
    public Map<String, Double> getTPS(int worldIndex) {
        
        if (tpsList.containsKey(String.format("%s", worldIndex))) {
            return tpsList.get(String.format("%s", worldIndex));
        }
        return null;
    }
    
    public Map<String, Double> getDimensions() {
        
        return dimensions;
    }
    
    public List<Double> getDimensionIds() {
        
        List<Double> ret = new ArrayList<Double>();
        for (Map.Entry<String, Double> dimension : dimensions.entrySet()) {
            ret.add(dimension.getValue());
        }
        return ret;
    }
    
    public void setDimensions(Map<String, Double> newDimensions) {
        
        dimensions.clear();
        dimensions.putAll(newDimensions);
        
    }
    
    public void setTPSList(Map<Integer, Map<String, Double>> newList) {
        
        tpsList.clear();
        tpsList.putAll(newList);
    }
    
    public Map<Object, Map<String, Double>> getTpsList() {
        
        return tpsList;
    }
    
    public void setEntities(Map<Integer, Map<String, Integer>> newEntities) {
        
        entities.clear();
        entities.putAll(newEntities);
    }
    
    public void setTileEntities(Map<Integer, Map<String, Integer>> newTileEntities) {
        
        tileEntities.clear();
        tileEntities.putAll(newTileEntities);
    }
    
    public Map<Integer, Map<String, Integer>> getEntities() {
        
        return entities;
    }
    
    public Map<Integer, Map<String, Integer>> getTileEntities() {
        
        return tileEntities;
    }
    
    public Map<String, Integer> getEntitiesInDimension(int worldIndex) {
        
        if (entities.containsKey(String.format("%s", worldIndex))) {
            return entities.get(String.format("%s", worldIndex));
        }
        return null;
    }
    
    public Map<String, Integer> getTileEntitiesInDimension(int worldIndex) {
        
        if (tileEntities.containsKey(String.format("%s", worldIndex))) {
            return tileEntities.get(String.format("%s", worldIndex));
        }
        return null;
    }
}
