package net.creeperhost.minetogether.irc;

import net.creeperhost.minetogether.MineTogether;
import net.creeperhost.minetogether.Profile;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.chat.ChatUtil;
import org.kitteh.irc.client.library.util.Format;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.creeperhost.minetogether.chat.ChatHandler.privateChatList;

public class test
{
    public static final String nickname = "MT6C416B82DFBD050EA2279038E981";
    public static AtomicBoolean first = new AtomicBoolean(true);
    private static OutputStreamWriter outputStreamWriter;
    private static BufferedWriter bufferedWriter;

    public static void start(ChatUtil.IRCServer ircServer)
    {
        try
        {
            Socket socket = new Socket(ircServer.address, ircServer.port);
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            CompletableFuture.runAsync(() ->
            {
                String line = null;
                try
                {
                    while ((line = bufferedReader.readLine()) != null)
                    {
                        if (line.startsWith("PING "))
                        {
                            System.out.println(line);
                            sendString("PONG " + line.substring(5) + "\r\n");
                            if(first.get()) {
                                sendString("USER " + nickname + " 8 * :" + "{\"p\":\"m35\",\"b\":\"MzUxNzQ\\u003d\"}");
                                sendString("JOIN " + ircServer.channel);
                                first.getAndSet(false);
                            }
                        } else {
                            handleInput(line);
                            System.out.println(line);
                        }
                    }
                } catch (Exception ignored)
                {
                    ignored.printStackTrace();
                }
            });

            sendString("NICK " + nickname);

            while (true)
            {
                Thread.sleep(100);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void sendString(String str) {
        try {
            System.out.println(str);
            bufferedWriter.write(str + "\r\n");
            bufferedWriter.flush();
        }
        catch (Exception e) {
            System.out.println("Exception: "+e);
        }
    }

    public static void sendMessage(String message)
    {
        String channel = ChatHandler.CHANNEL;
        sendString("PRIVMSG " + channel + " " + message);
    }

    public static void sendCTPCMessage(String target, String type, String value)
    {
        sendString("PRIVMSG " + target + " " + "\\x01" + type + " " + value + "\\x01");
    }

    public static void handleInput(String s)
    {
        if(s.contains(" :Nickname is already in use") && s.contains("433"))
        {
            ChatHandler.reconnectTimer.set(30000);
//                        ChatHandler.addStatusMessage("You appear to be connected elsewhere delaying reconnect for 30 seconds");
        }
        else if(s.contains("PRIVMSG"))
        {
            CompletableFuture.runAsync(() ->
            {
                Pattern pattern = Pattern.compile(".*(MT[A-Za-z0-9]{28}).*PRIVMSG.*(\\#\\w+) \\:(.*)");
                Matcher matcher = pattern.matcher(s);
                if (matcher.matches())
                {
                    String name = matcher.group(1);
                    String channel = matcher.group(2);
                    String message = matcher.group(3);
                    ChatHandler.addMessageToChat(channel, name, Format.stripAll(message));
                                /*if(channel.equalsIgnoreCase(ChatHandler.CHANNEL))
                                {
                                    ChatHandler.addMessageToChat(ChatHandler.CHANNEL, name, Format.stripAll(message));
                                }*/
                } else {
                    pattern = Pattern.compile("\\:(\\w+).*PRIVMSG.*\\:\\x01(.*)\\x01");
                    matcher = pattern.matcher(s);
                    if (matcher.matches()) {
                        String name = matcher.group(1);
                        String message = matcher.group(2);

                        ChatHandler.Listener.onCTCP(name, message);
                    } else {
                        pattern = Pattern.compile("\\:(\\w+).*PRIVMSG.*\\:(.*)");
                        matcher = pattern.matcher(s);
                        if (matcher.matches()) {
                            String name = matcher.group(1);
                            String message = matcher.group(2);
                            ChatHandler.addMessageToChat(name, name, Format.stripAll(message));
                        } else {
                            System.out.println("Unknown message! "+s);
                        }
                    }
                }
            }, MineTogether.chatMessageExecutor);
        } else if(s.contains("NOTICE"))
        {
            CompletableFuture.runAsync(() ->
            {
                Pattern pattern = Pattern.compile("\\:(\\w+).*NOTICE (?:MT.{28}|\\#\\w+) \\:(.*)");
                Matcher matcher = pattern.matcher(s);
                if(matcher.matches())
                {
                    String name = matcher.group(1);
                    String message = matcher.group(2);
                    boolean op = !name.startsWith("MT");

                    if (op)
                    {
                        //Channel Message
                        if (s.contains(ChatHandler.CHANNEL))
                        {
                            ChatHandler.Listener.onChannelNotice(name, message);
                        }
                        else if(s.contains(ChatHandler.nick))
                        {
                            ChatHandler.Listener.onNotice(name, message);
                        }
                    }
                }
            }, MineTogether.chatMessageExecutor);
        }
        else if(s.contains("MODE"))
        {
            CompletableFuture.runAsync(() ->
            {
                Pattern pattern = Pattern.compile("\\:(\\w+).*MODE.*\\#\\w+ (.)b (MT[a-zA-Z0-9]{28}).*");
                Matcher matcher = pattern.matcher(s);
                if(matcher.matches())
                {
                    String nick = matcher.group(3);
                    String modify = matcher.group(2);
                    if(modify.equals("+"))
                    {
                        ChatHandler.Listener.onUserBanned(nick);
                        ChatHandler.rebuildChat = true;
                    }
                    if(modify.equals("-"))
                    {
                        Profile profile = ChatHandler.knownUsers.findByNick(nick);
                        if(profile != null) {
                            profile.setBanned(false);
                            ChatHandler.knownUsers.update(profile);
                            ChatHandler.rebuildChat = true;
                        }
                        if(ChatHandler.backupBan.get().contains(nick)) {
                            ChatHandler.backupBan.getAndUpdate((bans) -> {
                                bans.remove(nick);
                                return bans;
                            });
                        }
                    }
                }
            }, MineTogether.ircEventExecutor);
        } else if(s.contains("JOIN"))
        {
            CompletableFuture.runAsync(() ->
            {
                Pattern pattern = Pattern.compile(":(MT.{28})!.*JOIN (#\\w+) . \\:(\\{.*\\})");
                Matcher matcher = pattern.matcher(s);
                if (matcher.matches()) {
                    String nick = matcher.group(1);
                    String channel = matcher.group(2);
                    String json = matcher.group(3);
                    if(ChatHandler.curseSync.containsKey(nick)) {
                        if(!ChatHandler.curseSync.get(nick).equals(json))
                        {
                            ChatHandler.curseSync.remove(nick);
                            ChatHandler.curseSync.put(nick, json);
                        }
                    } else {
                        ChatHandler.curseSync.put(nick, json);
                    }
                    Profile profile = ChatHandler.knownUsers.findByNick(nick);
                    if (profile != null) {
                        profile.setPackID(json);
                        ChatHandler.knownUsers.update(profile);
                    }
                }
            }, MineTogether.ircEventExecutor);
        } else if(s.contains(" 352 ") && s.split(" ")[1].contains("352"))//WHOIS responses
        {
            CompletableFuture.runAsync(() ->
            {
                Pattern pattern = Pattern.compile(":.*352 MT.{28} (\\#\\w+) .*(MT.{28}) \\w\\+? :\\d (\\{.*\\})");
                Matcher matcher = pattern.matcher(s);
                if (matcher.matches()) {
                    String nick = matcher.group(2);
                    String channel = matcher.group(1);
                    String json = matcher.group(3);
                    if(ChatHandler.curseSync.containsKey(nick)) {
                        if(!ChatHandler.curseSync.get(nick).equals(json))
                        {
                            ChatHandler.curseSync.remove(nick);
                            ChatHandler.curseSync.put(nick, json);
                        }
                    } else {
                        ChatHandler.curseSync.put(nick, json);
                    }
                    Profile profile = ChatHandler.knownUsers.findByNick(nick);
                    if (profile != null) {
                        if(profile.isFriend()) {
                            profile.setOnline(true);
                        }
                        profile.setPackID(json);
                        ChatHandler.knownUsers.update(profile);
                    }
                }
            }, MineTogether.ircEventExecutor);
        }
        else if(s.contains(" 401 ") && s.split(" ")[1].contains("401"))//WHOIS failure responses
        {
            CompletableFuture.runAsync(() ->
            {
                Pattern pattern = Pattern.compile(":.*401 MT.{28} (MT.{28}) :.*");
                Matcher matcher = pattern.matcher(s);
                if (matcher.matches()) {
                    String nick = matcher.group(1);
                    Profile profile = ChatHandler.knownUsers.findByNick(nick);
                    if (profile != null) {
                        if(profile.isFriend()) {
                            profile.setOnline(false);
                            ChatHandler.knownUsers.update(profile);
                        }
                    }
                }
            }, MineTogether.ircEventExecutor);
        }
        else if(s.contains("QUIT") || s.contains("LEAVE") || s.contains("PART"))
        {
            Pattern pattern = Pattern.compile("\\:(MT\\w{28})!");
            Matcher matcher = pattern.matcher(s);
            if(matcher.matches())
            {
                String name = matcher.group(1);
//                if (privateChatList != null && privateChatList.owner.equals(name)) {
//                    ChatHandler.host.closeGroupChat();
//                }
            }
        } else {
//            if(debugHandler.isDebug) System.out.println("Unhandled IRC message!\n"+s);
        }
    }
}
