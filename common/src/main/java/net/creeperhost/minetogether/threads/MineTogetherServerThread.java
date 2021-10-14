package net.creeperhost.minetogether.threads;

import com.google.gson.*;
import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.MineTogetherServer;
import net.creeperhost.minetogether.lib.util.WebUtils;

import java.util.HashMap;
import java.util.Map;

public class MineTogetherServerThread
{
    static Thread mtThread;
    public static boolean isActive;
    public static boolean failed;
    public static int tries = 0;

    public static void startMineTogetherServerThread(String serverIP, String displayName, String projectid, int port, Discoverability discoverMode)
    {
        mtThread = new Thread(() ->
        {
            MineTogether.logger.info("Enabling server list. Servers found to be breaking Mojang's EULA may be removed if complaints are received.");
            boolean first = true;
            while (MineTogetherServer.serverOn)
            {
                Map send = new HashMap<String, String>();

                if (!serverIP.isEmpty())
                {
                    send.put("ip", serverIP);
                }

                if (MineTogetherServer.secret != null) send.put("secret", MineTogetherServer.secret);
                send.put("name", displayName);
                send.put("projectid", projectid);
                send.put("port", String.valueOf(port));

                send.put("invite-only", discoverMode == Discoverability.INVITE ? "1" : "0");

                send.put("version", 2);

                Gson gson = new GsonBuilder().disableHtmlEscaping().create();

                String sendStr = gson.toJson(send);
                String resp = WebUtils.putWebResponse("https://api.creeper.host/serverlist/update", sendStr, true, true);

                int sleepTime = 90000;

                try
                {
                    JsonElement jElement = new JsonParser().parse(resp);
                    if (jElement.isJsonObject())
                    {
                        JsonObject jObject = jElement.getAsJsonObject();
                        if (jObject.get("status").getAsString().equals("success"))
                        {
                            tries = 0;
                            MineTogetherServer.updateID = jObject.get("id").getAsNumber().intValue();
                            if (jObject.has("secret")) MineTogetherServer.secret = jObject.get("secret").getAsString();
                            isActive = true;
                        }
                        else
                        {
                            if (tries >= 4)
                            {
                                MineTogether.logger.error("Unable to do call to server list - disabling for 45 minutes. Reason: " + jObject.get("message").getAsString());
                                tries = 0;
                                sleepTime = 60 * 1000 * 45;
                            }
                            else
                            {
                                MineTogether.logger.error("Unable to do call to server list - will try again in 90 seconds. Reason: " + jObject.get("message").getAsString());
                                tries++;
                            }
                            failed = true;
                        }

                        if (first)
                        {
                            //TODO add back invite command / server invites
                            //                            CommandInvite.reloadInvites(new String[0]);
                            first = false;
                        }
                    }
                } catch (Exception ignored)
                {
                }

                try
                {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored)
                {
                }
            }
        });
        mtThread.setDaemon(true);
        mtThread.start();
    }

    public static void stopThread()
    {
        if(mtThread != null) mtThread.start();
    }

    public static Thread getMtThread()
    {
        return mtThread;
    }

    public enum Discoverability
    {
        UNLISTED, PUBLIC, INVITE
    }
}
