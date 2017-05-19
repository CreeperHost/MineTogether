package net.creeperhost.creeperhost.gui;

import net.creeperhost.creeperhost.CreeperHost;
import net.creeperhost.creeperhost.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * @author Koen Beckers (K-4U)
 */
@SideOnly(Side.CLIENT)
public class GuiServerInfo extends GuiScreen {
    
    boolean isPlayerOpped = true; //TODO: Make me get fetched.
    private static ResourceLocation background = new ResourceLocation(CreeperHost.MOD_ID, "textures/guisiv.png");
    protected      int              xSize      = 344;
    protected      int              ySize      = 285;
    /**
     * Starting X position for the Gui. Inconsistent use for Gui backgrounds.
     */
    protected int guiLeft;
    /**
     * Starting Y position for the Gui. Inconsistent use for Gui backgrounds.
     */
    protected int guiTop;
    
    public void renderServerInfo(ScaledResolution resolution) {
        
        int displayWidth = resolution.getScaledWidth();
        int displayHeight = resolution.getScaledHeight();
        guiLeft = (displayWidth - xSize) / 2;
        guiTop = (displayHeight - ySize) / 2;
        
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(background);
        int x = guiLeft;
        int y = guiTop;
        int width = xSize;
        int height = ySize;
        int textureX = 0;
        int textureY = 0;
    
        drawScaledCustomSizeModalRect(x, y, textureX, textureY,xSize, ySize, width, height, 512f, 512f);
        drawCenteredString(mc.fontRendererObj, Util.localize("gui.serverstats"), guiLeft + (xSize / 2), guiTop + 5, 0xFFFFFFFF);
        
        try {
            int tpsTop = guiTop + 30;
            //Do the TPS list:
            drawString(mc.fontRendererObj, Util.localize("gui.tps"), guiLeft + 5, tpsTop, 0xFFFFFFFF);
            drawHorizontalLine(guiLeft + 5, guiLeft + 5 + mc.fontRendererObj.getStringWidth(Util.localize("gui.tps")), tpsTop + mc.fontRendererObj.FONT_HEIGHT, 0xFFFFFFFF);
            
            int index = 0;
            for (Map.Entry<String, Double> dimension : CreeperHost.instance.getQueryGetter().getExtendedServerData().getDimensions().entrySet()) {
                Map<String, Double> tpsInfo = CreeperHost.instance.getQueryGetter().getExtendedServerData().getTPS(dimension.getValue().intValue());
                int color = 0xFF00FF00;
                if (tpsInfo.get("tps") < 20) {
                    color = 0xFFFFA500;
                }
                if (tpsInfo.get("tps") < 10) {
                    color = 0xFFFF0000;
                }
                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                drawString(mc.fontRendererObj, String.format("%s: %s(%s)", dimension.getKey(), tpsInfo.get("tps"), decimalFormat.format(tpsInfo.get("ticktime"))), guiLeft + 5, tpsTop + 2 + (mc.fontRendererObj.FONT_HEIGHT + 2) * (index + 1), color);
                index++;
            }
    
            
            //TODO: Rewrite SipEndPoint to more easily throw arguments at the server
            /*
            int tileEntityTop = tpsTop + 2 + (mc.fontRendererObj.FONT_HEIGHT + 2) * (index + 2);
            int tileEntityLeft = guiLeft + 5;
            drawString(mc.fontRendererObj, Util.localize("gui.tileentities"), tileEntityLeft + 5, tileEntityTop, 0xFFFFFFFF);
            drawHorizontalLine(tileEntityLeft + 5, tileEntityLeft + 5 + mc.fontRendererObj.getStringWidth(Util.localize("gui.tileentities")), tileEntityTop + mc.fontRendererObj.FONT_HEIGHT, 0xFFFFFFFF);
            index = 0;
    
            Map<String, Integer> tileEntities = CreeperHost.instance.getQueryGetter().getExtendedServerData().getTileEntitiesInDimension(Minecraft.getMinecraft().theWorld.provider.getDimension());
            drawString(mc.fontRendererObj, Minecraft.getMinecraft().theWorld.provider.getDimensionType().getName(), tileEntityLeft + 5, tileEntityTop + 2 + (mc.fontRendererObj.FONT_HEIGHT + 2) * (index + 1), 0xFFFFFFFF);
            index++;
            for(Map.Entry<String, Integer> entity: tileEntities.entrySet()){
                drawString(mc.fontRendererObj, String.format("%s: %s", entity.getKey(), entity.getValue()), tileEntityLeft + 10, tileEntityTop + 2 + (mc.fontRendererObj.FONT_HEIGHT + 2) * (index + 1), 0xFFFFFFFF);
                index++;
                if(index > 10){
                    break; //Show a max of 10
                }
            }*/
            
            
            //TODO: Sort the list
            int entityTop = guiTop + 30;
            int entityLeft = guiLeft + 176;
            drawString(mc.fontRendererObj, Util.localize("gui.entities"), entityLeft + 5, entityTop, 0xFFFFFFFF);
            drawHorizontalLine(entityLeft + 5, entityLeft + 5 + mc.fontRendererObj.getStringWidth(Util.localize("gui.entities")), entityTop + mc.fontRendererObj.FONT_HEIGHT, 0xFFFFFFFF);
            index = 0;
            
            Map<String, Integer> entities = CreeperHost.instance.getQueryGetter().getExtendedServerData().getEntitiesInDimension(Minecraft.getMinecraft().theWorld.provider.getDimension());
            drawString(mc.fontRendererObj, Minecraft.getMinecraft().theWorld.provider.getDimensionType().getName(), entityLeft + 5, entityTop + 2 + (mc.fontRendererObj.FONT_HEIGHT + 2) * (index + 1), 0xFFFFFFFF);
            index++;
            for(Map.Entry<String, Integer> entity: entities.entrySet()){
                drawString(mc.fontRendererObj, String.format("%s: %s", entity.getKey(), entity.getValue()), entityLeft + 10, entityTop + 2 + (mc.fontRendererObj.FONT_HEIGHT + 2) * (index + 1), 0xFFFFFFFF);
                index++;
            }
    
            
            
            
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
    
    public boolean getIsPlayerOpped() {
        
        return isPlayerOpped;
    }
}

