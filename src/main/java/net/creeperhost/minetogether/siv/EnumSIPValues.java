package net.creeperhost.minetogether.siv;

/**
 * @author Koen Beckers (K-4U)
 * Update this file whenever SIP gets a new release!
 */
public enum EnumSIPValues {
    INVALID
    , MISFORMED
    , TIME
    , PLAYERS
    , DAYNIGHT
    , DIMENSIONS
    , UPTIME
    , DEATHS
    , WEATHER
    , BLOCKINFO
    , FLUID
    , INVENTORY
    , TPS
    , ENTITIES
    , VERSIONS
    , TILES
    , TILELIST
    ;

    public static EnumSIPValues fromString(String str) {
        for(EnumSIPValues v : values()){
            if(v.toString().toLowerCase().equals(str.toLowerCase())){
                return v;
            }
        }
        return INVALID;
    }


    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
