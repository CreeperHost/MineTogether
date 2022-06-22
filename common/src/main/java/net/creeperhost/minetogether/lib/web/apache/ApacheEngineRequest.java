package net.creeperhost.minetogether.lib.web.apache;

import net.covers1624.quack.io.IOUtils;
import net.creeperhost.minetogether.lib.web.EngineRequest;
import net.creeperhost.minetogether.lib.web.HeaderList;
import net.creeperhost.minetogether.lib.web.WebBody;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.AbstractHttpEntity;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by covers1624 on 21/6/22.
 */
public class ApacheEngineRequest implements EngineRequest {

    @Nullable
    private RequestBuilder builder;
    private final HeaderList headers = new HeaderList();

    @Override
    public EngineRequest method(String method, @Nullable WebBody body) {
        assert builder == null : "Method already set";
        builder = RequestBuilder.create(method);
        if (body != null) {
            builder.setEntity(new AbstractHttpEntity() {
                {
                    setContentType(body.contentType());
                }

                @Override
                public void writeTo(OutputStream os) throws IOException {
                    try (InputStream is = getContent()) {
                        IOUtils.copy(is, os);
                    }
                }

                // @formatter:off
                @Override public boolean isRepeatable() { return false; }
                @Override public long getContentLength() { return body.length(); }
                @Override public InputStream getContent() throws IOException, UnsupportedOperationException { return body.open(); }
                @Override public boolean isStreaming() { return true; }
                // @formatter:on
            });
        }
        return this;
    }

    @Override
    public EngineRequest url(String url) {
        assert builder != null : "method(String, Body) must be called first";
        builder.setUri(url);
        return this;
    }

    @Override
    public EngineRequest header(String key, String value) {
        assert builder != null : "method(String, Body) must be called first";
        headers.add(key, value);
        return this;
    }

    @Override
    public EngineRequest headers(Map<String, String> headers) {
        assert builder != null : "method(String, Body) must be called first";
        this.headers.addAll(headers);
        return this;
    }

    @Override
    public EngineRequest headers(HeaderList headers) {
        assert builder != null : "method(String, Body) must be called first";
        this.headers.addAll(headers);
        return this;
    }

    @Override
    public EngineRequest removeHeader(String key) {
        assert builder != null : "method(String, Body) must be called first";
        headers.removeAll(key);
        return this;
    }

    @Override
    public String getUrl() {
        assert builder != null : "method(String, Body) must be called first";
        assert builder.getUri() != null : "Url not set";
        return builder.getUri().toString();
    }

    @Override
    public HeaderList getHeaders() {
        assert builder != null : "method(String, Body) must be called first";
        return headers;
    }

    public HttpUriRequest build() {
        assert builder != null : "method(String, Body) must be called first";
        assert builder.getUri() != null : "Url not set";
        for (HeaderList.Entry header : headers) {
            builder.addHeader(header.name, header.value);
        }
        return builder.build();
    }
}
