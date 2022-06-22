package net.creeperhost.minetogether.lib.web.apache;

import net.creeperhost.minetogether.lib.web.EngineRequest;
import net.creeperhost.minetogether.lib.web.EngineResponse;
import net.creeperhost.minetogether.lib.web.WebEngine;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

/**
 * An Apache HTTPClient implementation for {@link WebEngine}.
 * <p>
 * Created by covers1624 on 20/6/22.
 */
public class ApacheWebEngine implements WebEngine {

    private final CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec(CookieSpecs.DEFAULT)
                    .build()
            )
            .setDefaultCookieStore(new BasicCookieStore())
            .build();

    @Override
    public EngineRequest newRequest() {
        return new ApacheEngineRequest();
    }

    @Override
    public EngineResponse execute(EngineRequest r) throws IOException {
        if (!(r instanceof ApacheEngineRequest)) throw new IllegalArgumentException("Only supports executing ApacheEngineRequests.");

        HttpUriRequest uriRequest = ((ApacheEngineRequest) r).build();
        CloseableHttpResponse response = httpClient.execute(uriRequest);

        return new ApacheEngineResponse(response);
    }

}
