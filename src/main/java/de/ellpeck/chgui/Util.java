package de.ellpeck.chgui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public final class Util{

    private static List<String> cookies;

    public static final ResourceLocation GUI_TEXTURES = new ResourceLocation(CreeperHostGui.MOD_ID,"textures/gui.png");

    public static String localize(String key, Object... format){
        return I18n.format(CreeperHostGui.MOD_ID+"."+key, format);
    }

    public static String getWebResponse(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (cookies != null) {
                for (String cookie : cookies) {
                    conn.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                }
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder respData = new StringBuilder();
            while ((line = rd.readLine()) != null)
            {
                respData.append(line);
            }

            List<String> setCookies = conn.getHeaderFields().get("Set-Cookie");

            if (setCookies != null) {
                cookies = setCookies;
            }

            rd.close();
            return respData.toString();
        } catch (Throwable t) {
            CreeperHostGui.logger.warn("An error occurred while fetching " + urlString, t);
        }

        return "error";

    }

    public static GuiScreen getGuiFromEvent(Object event) {
        GuiScreen gui = null;
        try
        {
            Field guiField = ReflectionHelper.findField(event.getClass(), "gui");
            guiField.setAccessible(true);
            gui = (GuiScreen) guiField.get(event);
        } catch (Throwable t)
        {
        }

        if (gui == null) {
            // We need to go deeper
            Class lastClass = event.getClass();
            Class superClass = null;
            while (gui == null) {
                superClass = lastClass.getSuperclass();

                if (superClass == lastClass)
                {
                    // We've hit the bottom. Oh.
                    break;
                }

                try
                {
                    Field guiField;

                    guiField = ReflectionHelper.findField(superClass, "gui");
                    guiField.setAccessible(true);
                    gui = (GuiScreen) guiField.get(event);
                } catch (Throwable t)
                {
                }
                lastClass = superClass;
            }
        }

        return gui;
    }

    public static boolean setGuiInEvent(Object event, GuiScreen replacement) {
        try
        {
            Field guiField = ReflectionHelper.findField(event.getClass(), "gui");
            guiField.setAccessible(true);
            guiField.set(event, replacement);
            return true;
        } catch (IllegalAccessException e)
        {
        }

        return false;
    }

    public static List<GuiButton> getButtonList(Object event) {
        List<GuiButton> list = null;
        try
        {
            Field guiField = ReflectionHelper.findField(event.getClass(), "buttonList");
            guiField.setAccessible(true);
            list = (List<GuiButton>) guiField.get(event);
        } catch (Throwable t)
        {
        }

        if (list == null) {
            // We need to go deeper
            Class lastClass = event.getClass();
            Class superClass = null;
            while (list == null) {
                superClass = lastClass.getSuperclass();

                if (superClass == lastClass)
                {
                    // We've hit the bottom. Oh.
                    break;
                }

                try
                {
                    Field guiField;

                    guiField = ReflectionHelper.findField(superClass, "buttonList");
                    guiField.setAccessible(true);
                    list = (List<GuiButton>) guiField.get(event);
                } catch (Throwable t)
                {
                }
                lastClass = superClass;
            }
        }

        return list;
    }

    public static GuiButton getButton(Object event) {
        GuiButton button = null;

        try
        {
            Field guiField = ReflectionHelper.findField(event.getClass(), "buttonList");
            guiField.setAccessible(true);
            button = (GuiButton) guiField.get(event);
        } catch (Throwable e)
        {
        }

        if (button == null) {
            // We need to go deeper
            Class lastClass = event.getClass();
            Class superClass = null;
            while (button == null && lastClass != null) {
                superClass = lastClass.getSuperclass();

                if (superClass == lastClass)
                {
                    // We've hit the bottom. Oh.
                    break;
                }

                try
                {
                    Field guiField;

                    guiField = ReflectionHelper.findField(superClass, "button");
                    guiField.setAccessible(true);
                    button = (GuiButton) guiField.get(event);
                } catch (Throwable t)
                {
                }
                lastClass = superClass;
            }
        }

        return button;
    }

}
