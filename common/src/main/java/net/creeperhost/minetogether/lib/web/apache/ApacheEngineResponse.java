package net.creeperhost.minetogether.lib.web.apache;

import net.creeperhost.minetogether.lib.web.EngineResponse;
import net.creeperhost.minetogether.lib.web.HeaderList;
import net.creeperhost.minetogether.lib.web.WebBody;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by covers1624 on 21/6/22.
 */
public class ApacheEngineResponse implements EngineResponse {

    private final CloseableHttpResponse response;
    private final HeaderList headers = new HeaderList();
    @Nullable
    private final WebBody body;

    public ApacheEngineResponse(CloseableHttpResponse response) {
        this.response = response;
        for (Header header : response.getAllHeaders()) {
            headers.add(header.getName(), header.getValue());
        }
        HttpEntity entity = response.getEntity();
        body = entity != null ? new ResponseBodyWrapper(entity) : null;
    }

    @Override
    public int statusCode() {
        return response.getStatusLine().getStatusCode();
    }

    @Override
    public String message() {
        return response.getStatusLine().getReasonPhrase();
    }

    @Override
    public HeaderList headers() {
        return headers;
    }

    @Override
    public WebBody body() {
        return body;
    }

    @Override
    public void close() throws IOException {
        response.close();
    }

    private static class ResponseBodyWrapper implements WebBody {

        private final HttpEntity entity;
        @Nullable
        private final String contentType;

        private ResponseBodyWrapper(HttpEntity entity) {
            this.entity = entity;
            Header contentType = entity.getContentType();
            this.contentType = contentType != null ? contentType.getValue() : null;
        }

        @Override
        public InputStream open() throws IOException {
            return entity.getContent();
        }

        @Override
        public long length() {
            return entity.getContentLength();
        }

        @Nullable
        @Override
        public String contentType() {
            return contentType;
        }
    }
}
