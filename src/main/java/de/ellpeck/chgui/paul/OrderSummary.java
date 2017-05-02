package de.ellpeck.chgui.paul;

import java.util.List;

/**
 * Created by Aaron on 28/04/2017.
 */
public class OrderSummary
{
    public final String vpsDisplay;
    public final List<String> vpsFeatures;
    public final double preDiscount;
    public final double subTotal;
    public final double total;
    public final double tax;
    public final double discount;
    public final String suffix;
    public final String prefix;
    public final String currency;
    public final String productID;

    public OrderSummary(String productID, String vpsDisplay, List<String> vpsFeatures, double preDiscount, double subTotal, double total, double tax, double discount, String suffix, String prefix, String currency) {
        this.productID = productID;
        this.vpsDisplay = vpsDisplay;
        this.vpsFeatures = vpsFeatures;
        this.preDiscount = preDiscount;
        this.subTotal = subTotal;
        this.total = total;
        this.discount = discount;
        this.tax = tax;
        this.suffix = suffix;
        this.prefix = prefix;
        this.currency = currency;
    }
}
