package de.ellpeck.chgui.paul;

import java.util.List;

/**
 * Created by Aaron on 28/04/2017.
 */
public class OrderSummary
{
    public final String vpsDisplay;
    public final List<String> vpsFeatures;

    public OrderSummary(String vpsDisplay, List<String> vpsFeatures) {
        this.vpsDisplay = vpsDisplay;
        this.vpsFeatures = vpsFeatures;
    }
}
