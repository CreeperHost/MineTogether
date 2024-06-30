package net.creeperhost.minetogether.orderform;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by brandon3055 on 15/06/2024
 */
public class WorldUploader {
    private static final ExecutorService UPLOAD_EXECUTOR = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("MT World Upload Thread %d").setDaemon(true).build());
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.138 Safari/537.36 Vivaldi/1.8.770.56 MineTogether/1.0.0";
    private static final Logger LOGGER = LogManager.getLogger();
    private final Path worldFolder;
    private Path tempZipFile;

    private volatile int stage = 0;
    private volatile double uploadProgress = 0;

    private Future<?> uploadTask = null;
    private String error;
    private String resultFileURL;
    private boolean finished = false;

    public WorldUploader(Path worldFolder) {
        this.worldFolder = worldFolder;
    }

    public boolean running() {
        return uploadTask != null && !uploadTask.isDone();
    }

    public Component getStatus() {
        if (stage == 0){
            return Component.translatable("minetogether:gui.order.upload_stage.start").withStyle(ChatFormatting.GRAY);
        }else if (stage == 1) {
            return Component.translatable("minetogether:gui.order.upload_stage.compress").withStyle(ChatFormatting.GREEN);
        } else if (stage == 2) {
            return Component.translatable("minetogether:gui.order.upload_stage.upload", Math.round(uploadProgress * 10000D) / 100D).withStyle(ChatFormatting.BLUE);
        }
        return Component.empty();
    }

    public boolean errored() {
        return error != null;
    }

    public String getError() {
        return error;
    }

    public boolean isFinished() {
        return finished;
    }

    public String getResultFileURL() {
        return resultFileURL;
    }

    public void start() {
        if (running()) return;
        stage = 0;
        finished = false;
        error = null;
        uploadProgress = 0;
        uploadTask = UPLOAD_EXECUTOR.submit(this::doUpload);
    }

    public void cancel() {
        if (uploadTask != null) {
            uploadTask.cancel(true);
            uploadTask = null;
        }
    }

    private void doUpload() {
        try {
            stage = 1;
            if (!compress()) return;
            stage = 2;
            if (!upload()) return;
            finished = true;
            uploadTask = null;
        } catch (Throwable e) {
            LOGGER.error("An error occurred while uploading world", e);
            error = "An error occurred while uploading world";
        }
    }

    private boolean compress() throws IOException {
        tempZipFile = Files.createTempFile("world_upload", ".zip");
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(tempZipFile)));

        try {
            Path folderName = Paths.get(this.worldFolder.getFileName().toString());
            Files.walkFileTree(this.worldFolder, new SimpleFileVisitor<>() {
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    if (!path.endsWith("session.lock")) {
                        String s1 = folderName.resolve(worldFolder.relativize(path)).toString().replace('\\', '/');
                        ZipEntry zipentry = new ZipEntry(s1);
                        zos.putNextEntry(zipentry);
                        com.google.common.io.Files.asByteSource(path.toFile()).copyTo(zos);
                        zos.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Throwable ex) {
            try {
                zos.close();
            } catch (Throwable ignored) {}
            LOGGER.error("An error occurred while compressing world: {}", worldFolder, ex);
            error = "Failed to compress world!";
            return false;
        }

        zos.close();
        return true;
    }

    private boolean upload() throws IOException {
        HttpPut httpput = new HttpPut("https://transfer.ch.tools/world.zip");
        httpput.setEntity(new InputStreamEntity(new InputStreamWrapper(Files.newInputStream(tempZipFile), Files.size(tempZipFile))));

        try (CloseableHttpClient client = HttpClients.custom().setUserAgent(USER_AGENT).build()) {
            CloseableHttpResponse response = client.execute(httpput);
            StatusLine status = response.getStatusLine();

            if (status.getStatusCode() != 200) {
                LOGGER.error("Upload request failed. Returned response code: {}, Reason: {}", status.getStatusCode(), status.getReasonPhrase());
                error = "Upload failed with status code: " + status.getStatusCode();
                return false;
            }

            HttpEntity entity = response.getEntity();
            resultFileURL = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
            return true;
        }
    }


    public class InputStreamWrapper extends InputStream {
        private InputStream is;
        private long length;
        private long readPos;

        public InputStreamWrapper(InputStream inputStream, long length) {
            this.is = inputStream;
            this.length = length;
        }


        @Override
        public int read(byte[] b) throws IOException {
            return (int) onRead(is.read(b));
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return (int) onRead(is.read(b, off, len));
        }

        @Override
        public long skip(long n) throws IOException {
            return onRead(is.skip(n));
        }

        @Override
        public int read() throws IOException {
            int read = is.read();
            onRead(1);
            return read;
        }

        private long onRead(long readCount) {
            if (readCount != -1) {
                readPos += readCount;
                uploadProgress = readPos * 1.0 / length;
            }
            return readCount;
        }
    }
}
