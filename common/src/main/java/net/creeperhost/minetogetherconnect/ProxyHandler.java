package net.creeperhost.minetogetherconnect;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ProxyHandler {
    public static void Start(int localPort, String address, int remotePort, String secret, BiConsumer<Boolean, String> callback) {
        InputStream localInput = null;
        OutputStream localOutput = null;
        InputStream remoteInput = null;
        OutputStream remoteOutput = null;
        Socket socket = null;
        Socket remoteSocket = null;
        InputStreamReader localInputReader = null;
        InputStreamReader remoteInputReader = null;
        try {
            SocketChannel localChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", localPort));
            socket = localChannel.socket();
            SocketChannel remoteChannel = SocketChannel.open(new InetSocketAddress(address, remotePort));
            remoteSocket = remoteChannel.socket();
            remoteOutput = remoteSocket.getOutputStream();
            remoteInput = remoteSocket.getInputStream();
            remoteInputReader = new InputStreamReader(remoteInput);
            localOutput = socket.getOutputStream();
            localInput = socket.getInputStream();
            localInputReader = new InputStreamReader(localInput);

            ConnectUtil connectUtil = new ConnectUtil(remoteInputReader);
            remoteOutput.write((secret + "\n").getBytes(StandardCharsets.UTF_8));
            long startTime = System.currentTimeMillis();
            while(remoteSocket.isConnected()) {
                long now = System.currentTimeMillis();
                if(now - startTime > 3000) { // only wait a few seconds for ready
                    //callback.accept(false, "timeout");
                    return;
                }
                String line = connectUtil.readLine();
                if (line == null) continue;

                if (!line.equals("Ready")) {
                    //callback.accept(false, line);
                    return;
                }
                break;
            }

            //ConnectUtil.CloseMultiple(localInputReader, localInput, localOutput, remoteInputReader, remoteInput, remoteOutput);

            startProxying(localChannel, remoteChannel, socket, remoteSocket);
            //callback.accept(true, "");
        } catch (Exception e) {
            //callback.accept(false, "closed");
            ConnectUtil.CloseMultiple(localInputReader, localInput, localOutput, remoteInputReader, remoteInput, remoteOutput, socket, remoteSocket);
        }
    }

    public static void startProxying(SocketChannel localChannel, SocketChannel remoteChannel, Socket localSocket, Socket remoteSocket) {
        proxyOne(localChannel, remoteChannel, localSocket, remoteSocket);
        proxyOne(remoteChannel, localChannel, remoteSocket, localSocket);
    }

    public static void proxyOne(SocketChannel firstChannel, SocketChannel secondChannel, Socket firstSocket, Socket secondSocket) {
        CompletableFuture.runAsync(() -> {
            ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
            while (firstChannel.isConnected()) {
                try {
                    int read = firstChannel.read(buffer);
                    if (read > 0) {
                        buffer.flip();
                        secondChannel.write(buffer);
                        buffer.clear();
                    }
                } catch (Exception e) {
                    ConnectUtil.CloseMultiple(firstChannel, secondChannel, firstSocket, secondSocket);
                }
            }
            ConnectUtil.CloseMultiple(firstChannel, secondChannel, firstSocket, secondSocket);
        }); // TODO: expanding thread pool for this purpose
    }
}
