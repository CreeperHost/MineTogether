package net.creeperhost.minetogetherconnect;

import java.io.IOException;
import java.io.InputStreamReader;

public class ConnectUtil {
    private final InputStreamReader inputReader;
    private StringBuilder builder = new StringBuilder();

    public ConnectUtil(InputStreamReader inputReader) {
        this.inputReader = inputReader;
    }

    public String readLine() throws IOException {
        char[] cb = new char[10];
        int readBytes;
        readBytes = inputReader.read(cb);
        for(int i = 0; i < readBytes; i++) {
            if (cb[i] == '\n') {
                String line = builder.toString();
                builder = new StringBuilder();
                return line;
            }
            builder.append(cb[i]);
        }
        return null;
    }

    public static void CloseMultiple(java.io.Closeable... closeables) {
        for (java.io.Closeable closeable : closeables) {
            try {
                if(closeable != null) closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
