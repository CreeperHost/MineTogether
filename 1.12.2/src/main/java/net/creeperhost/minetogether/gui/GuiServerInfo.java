package net.creeperhost.minetogether.gui;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

/**
 * @author Koen Beckers (K-4U)
 */
@SideOnly(Side.CLIENT)
public class GuiServerInfo extends GuiScreen
{
    boolean isPlayerOpped = true;
    
    long ticks;
    private String header;
    
    public GuiServerInfo() {}
    
    @SuppressWarnings("Duplicates")
    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue(Map<K, V> map)
    {
        List<Map.Entry<K, V>> list =  new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, (o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));
        
        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    public void doTick()
    {
        ticks++;
    }
    
    public boolean renderServerInfo()
    {
        Map<String, Integer> myList = new LinkedHashMap<String, Integer>();
        int i = 0;
        int j = 0;
        int k = 0;
        try
        {
            if (CreeperHost.instance.getQueryGetter().getExtendedServerData() == null)
            {
                return false;
            }
            
            Map<String, Double> tpsInfo = CreeperHost.instance.getQueryGetter().getExtendedServerData().getTPS(mc.player.dimension);
            TextFormatting color = TextFormatting.WHITE;
            if (tpsInfo.get("tps") < 20)
            {
                color = TextFormatting.YELLOW;
            }
            if (tpsInfo.get("tps") < 10)
            {
                color = TextFormatting.RED;
            }
            
            header = String.format("%s %s%.2f(%.2f)", Util.localize("gui.tps"), color, tpsInfo.get("tps"), tpsInfo.get("ticktime"));
            
            Map<String, Integer> entities = CreeperHost.instance.getQueryGetter().getExtendedServerData().getEntitiesInDimension(Minecraft.getMinecraft().world.provider.getDimension());
            for (Map.Entry entity : entities.entrySet())
            {
                String key = (String) entity.getKey();
                if (key.length() >= 9 && key.contains("item.tile.") || key.contains("item.item."))
                {
                    key = key.substring(5);
                    if (!key.contains(".name"))
                    {
                        key = key + ".name";
                    }
                }
                
                key = I18n.format(key);
                
                int keyLength = key.length();
                
                if (keyLength >= 15)
                {
                    for (int end = 15; end <= keyLength; end++)
                    {
                        int start = end - 15;
                        String subStr = key.substring(start, end);
                        k = this.mc.fontRendererObj.getStringWidth(subStr);
                        i = Math.max(i, k);
                    }
                    
                    int work = (keyLength - 15) + 12;
                    
                    int start = (int) Math.floor(ticks / 20) % work;
                    if (start <= 5)
                    {
                        start = 0;
                    }
                    else
                    {
                        start -= 6;
                    }
                    
                    if (start + 15 > keyLength)
                    {
                        start = keyLength - 15;
                    }
                    
                    int end = start + 15;
                    
                    key = key.substring(start, end);
                }
                else
                {
                    k = this.mc.fontRendererObj.getStringWidth(key);
                    i = Math.max(i, k);
                }
                Double value = (Double) entity.getValue();
                int finalVal = value.intValue();
                myList.put(key, finalVal);
            }
            myList = sortByValue(myList);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        for (Map.Entry<String, Integer> entString : myList.entrySet())
        {
            k = this.mc.fontRendererObj.getStringWidth(" " + entString.getValue());
            j = Math.max(j, k);
        }
        
        int l3 = Math.min(myList.size(), 60);
        int i4 = l3;
        int j4;
        
        for (j4 = 1; i4 > 20; i4 = (l3 + j4 - 1) / j4)
        {
            ++j4;
        }
        
        int l;
        
        l = j;
        
        int i1 = Math.min(j4 * ((0) + i + l), width - 50) / j4;
        
        int j1 = width / 2 - (i1 * j4 + (j4 - 1) * 5) / 2;
        int k1 = 10;
        int l1 = i1 * j4 + (j4 - 1) * 5;
        List<String> list1 = null;
        
        if (this.header != null)
        {
            list1 = this.mc.fontRendererObj.listFormattedStringToWidth(this.header, width - 50);
            list1 = new ArrayList<String>(list1);
            list1.add(Util.localize("gui.entities"));
            
            for (String s : list1)
            {
                l1 = Math.max(l1, this.mc.fontRendererObj.getStringWidth(s));
            }
        }
        
        List<String> list2 = null;
        
        if (list1 != null)
        {
            drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + list1.size() * this.mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);
            
            for (String s2 : list1)
            {
                int i2 = this.mc.fontRendererObj.getStringWidth(s2);
                this.mc.fontRendererObj.drawStringWithShadow(s2, (float) (width / 2 - i2 / 2), (float) k1, -1);
                k1 += this.mc.fontRendererObj.FONT_HEIGHT;
            }
            ++k1;
        }
        
        drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + i4 * 9, Integer.MIN_VALUE);
        
        int k4 = 0;
        for (Map.Entry<String, Integer> entString : myList.entrySet())
        {
            if (k4 == 60)
            {
                break;
            }
            int l4 = k4 / i4;
            int i5 = k4 % i4;
            int j2 = j1 + l4 * i1 + l4 * 5;
            int k2 = k1 + i5 * 9;
            drawRect(j2, k2, j2 + i1, k2 + 8, 553648127);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            
            if (k4 < myList.size())
            {
                String s4 = entString.getKey();
                
                this.mc.fontRendererObj.drawStringWithShadow(s4, (float) j2, (float) k2, -1);
                
                int k5 = j2 + i + 1;
                int l5 = k5 + l;
                
                if (l5 - k5 > 5)
                {
                    String s1 = TextFormatting.YELLOW + "" + entString.getValue();
                    this.mc.fontRendererObj.drawStringWithShadow(s1, (float) (l5 - this.mc.fontRendererObj.getStringWidth(s1)), (float) k2, 16777215);
                }
            }
            k4++;
        }
        
        if (list2 != null)
        {
            k1 = k1 + i4 * 9 + 1;
            drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + list2.size() * this.mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);
            
            for (String s3 : list2)
            {
                int j5 = this.mc.fontRendererObj.getStringWidth(s3);
                this.mc.fontRendererObj.drawStringWithShadow(s3, (float) (width / 2 - j5 / 2), (float) k1, -1);
                k1 += this.mc.fontRendererObj.FONT_HEIGHT;
            }
        }
        return true;
    }
    
    public boolean getIsPlayerOpped()
    {
        return isPlayerOpped;
    }
}

