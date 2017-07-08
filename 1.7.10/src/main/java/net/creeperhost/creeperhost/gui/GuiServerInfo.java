package net.creeperhost.creeperhost.gui;

import net.creeperhost.creeperhost.CreeperHost;
import net.creeperhost.creeperhost.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
//import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
//import net.minecraft.util.text.TextFormatting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.*;

/**
 * @author Koen Beckers (K-4U)
 */
@SideOnly(Side.CLIENT)
public class GuiServerInfo extends GuiScreen {
    
    boolean isPlayerOpped = true; //TODO: Make me get fetched.

    long ticks;

    public GuiServerInfo() {
    }

    private String header;

    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort(list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }

    public void doTick() {
        ticks++;
    }
    
    public boolean renderServerInfo() {

        Map<String, Integer> myList = new LinkedHashMap<String, Integer>();

        int i = 0;
        int j = 0;
        int k = 0;

        try {

            if (CreeperHost.instance.getQueryGetter().getExtendedServerData() == null) {
                return false;
            }

            Map<String, Double> tpsInfo = CreeperHost.instance.getQueryGetter().getExtendedServerData().getTPS(mc.thePlayer.dimension);
            String color = "\u00a7" + "f";
            if (tpsInfo.get("tps") < 20) {
                color = "\u00a7" + "e";
            }
            if (tpsInfo.get("tps") < 10) {
                color = "\u00a7" + "c";
            }

            header = String.format("%s %s%.2f(%.2f)", Util.localize("gui.tps"), color, tpsInfo.get("tps"), tpsInfo.get("ticktime"));

            /*drawString(mc.fontRendererObj, Util.localize("gui.tileentities"), tileEntityLeft + 5, tileEntityTop, 0xFFFFFFFF);
            drawHorizontalLine(tileEntityLeft + 5, tileEntityLeft + 5 + mc.fontRendererObj.getStringWidth(Util.localize("gui.tileentities")), tileEntityTop + mc.fontRendererObj.FONT_HEIGHT, 0xFFFFFFFF);
            index = 0;

            Map<String, Integer> tileEntities = CreeperHost.instance.getQueryGetter().getExtendedServerData().getTileEntitiesInDimension(Minecraft.getMinecraft().theWorld.provider.getDimension());
            //drawString(mc.fontRendererObj, Minecraft.getMinecraft().theWorld.provider.getDimensionType().getName(), tileEntityLeft + 5, tileEntityTop + 2 + (mc.fontRendererObj.FONT_HEIGHT + 2) * (index + 1), 0xFFFFFFFF);
            index++;
            for(Map.Entry<String, Integer> entity: tileEntities.entrySet()){
                myList.put(entity.getKey(), entity.getValue());
            }*/

            Map<String, Integer> entities = CreeperHost.instance.getQueryGetter().getExtendedServerData().getEntitiesInDimension(Minecraft.getMinecraft().theWorld.provider.dimensionId);
            for(Map.Entry entity: entities.entrySet()){
                String key = (String) entity.getKey();
                if (key.length() >= 9 && key.contains("item.tile.") || key.contains("item.item.")) {
                    key = key.substring(5);
                    if (!key.contains(".name")) {
                        key = key + ".name";
                    }
                }

                key = I18n.format(key);

                int keyLength = key.length();

                if (keyLength >= 15) {
                    for (int end = 15; end <= keyLength; end++)
                    {
                        int start = end - 15;
                        String subStr = key.substring(start, end);
                        k = this.mc.fontRenderer.getStringWidth(subStr);
                        i = Math.max(i, k);
                    }

                    int work = (keyLength - 15) + 12;

                    int start = (int)Math.floor(ticks / 20) % work;
                    if (start <= 5) {
                        start = 0;
                    } else {
                        start -= 6;
                    }

                    if (start + 15 > keyLength) {
                        start = keyLength - 15;
                    }

                    int end = start + 15;

                    key = key.substring(start, end);
                }  else
                {
                    k = this.mc.fontRenderer.getStringWidth(key);
                    i = Math.max(i, k);
                }

                Double value = (Double) entity.getValue();
                int finalVal = value.intValue();
                myList.put( key, finalVal);
            }

            myList = sortByValue(myList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, Integer> entString : myList.entrySet())
        {
            k = this.mc.fontRenderer.getStringWidth(" " + entString.getValue());

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
            list1 = this.mc.fontRenderer.listFormattedStringToWidth(this.header, width - 50);
            list1 = new ArrayList<String>(list1);
            list1.add(Util.localize("gui.entities"));

            for (String s : list1)
            {
                l1 = Math.max(l1, this.mc.fontRenderer.getStringWidth(s));
            }
        }

        List<String> list2 = null;

        if (list1 != null)
        {
            drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + list1.size() * this.mc.fontRenderer.FONT_HEIGHT, Integer.MIN_VALUE);

            for (String s2 : list1)
            {
                int i2 = this.mc.fontRenderer.getStringWidth(s2);
                this.mc.fontRenderer.drawStringWithShadow(s2, (width / 2 - i2 / 2), k1, -1);
                k1 += this.mc.fontRenderer.FONT_HEIGHT;
            }

            ++k1;
        }

        drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + i4 * 9, Integer.MIN_VALUE);

        int k4 = 0;
        for (Map.Entry<String, Integer> entString : myList.entrySet())
        {
            if (k4 == 60) {
                break;
            }
            int l4 = k4 / i4;
            int i5 = k4 % i4;
            int j2 = j1 + l4 * i1 + l4 * 5;
            int k2 = k1 + i5 * 9;
            drawRect(j2, k2, j2 + i1, k2 + 8, 553648127);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
/*            GL11.glEnable(GL11.GL_ALPHA);
            GL11.glEnable(GL11.GL_BLEND);*/
            GL11.glEnable(GL11.GL_ALPHA_TEST);
//            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            if (k4 < myList.size())
            {
                String s4 = entString.getKey();

                this.mc.fontRenderer.drawStringWithShadow(s4, j2, k2, -1);

                int k5 = j2 + i + 1;
                int l5 = k5 + l;

                if (l5 - k5 > 5)
                {
                    String s1 = "\u00a7" + "e" + "" + entString.getValue();
                    this.mc.fontRenderer.drawStringWithShadow(s1, (l5 - this.mc.fontRenderer.getStringWidth(s1)), k2, 16777215);
                }
            }
            k4++;
        }

        if (list2 != null)
        {
            k1 = k1 + i4 * 9 + 1;
            drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + list2.size() * this.mc.fontRenderer.FONT_HEIGHT, Integer.MIN_VALUE);

            for (String s3 : list2)
            {
                int j5 = this.mc.fontRenderer.getStringWidth(s3);
                this.mc.fontRenderer.drawStringWithShadow(s3, (width / 2 - j5 / 2), k1, -1);
                k1 += this.mc.fontRenderer.FONT_HEIGHT;
            }
        }

        return true;
    }

    public boolean getIsPlayerOpped() {
        return isPlayerOpped;
    }
}

