package net.creeperhost.minetogether.lib.web;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static net.creeperhost.minetogether.lib.web.WebConstants.JSON;

/**
 * Created by covers1624 on 20/6/22.
 */
public abstract class ApiRequest<R> {

    protected static final Gson GSON = new Gson();
    protected static final Type STRING_MAP_TYPE = new TypeToken<>() { }.getType();

    public final String method;
    public final String url;
    public final Type responseClass;

    public final Set<String> requiredAuthHeaders = new HashSet<>();

    public final List<Pair<String, String>> queryParameters = new LinkedList<>();
    public final HeaderList headers = new HeaderList();

    @Nullable
    protected WebBody body;

    protected ApiRequest(String method, String url, Type responseClass) {
        this.method = method;
        this.url = url;
        this.responseClass = responseClass;
    }

    /**
     * Get the {@link Gson} instance for serializing this request/response.
     *
     * @return The {@link Gson} instance.
     */
    public Gson getGson() {
        return GSON;
    }

    /**
     * Overload of {@link #jsonBody(Gson, Object)} which
     * uses {@link #getGson()} for the {@link Gson} instance.
     *
     * @param obj  The object to serialize.
     */
    protected void jsonBody(Object obj) {
        jsonBody(getGson(), obj);
    }

    /**
     * Adds a JSON body to this request.
     *
     * @param gson The {@link Gson} instance to serialize {@code obj} with.
     * @param obj  The object to serialize.
     */
    protected void jsonBody(Gson gson, Object obj) {
        jsonBody(gson, obj, obj.getClass());
    }

    /**
     * Overload of {@link #jsonBody(Gson, Object, Type)} which
     * uses {@link #getGson()} for the {@link Gson} instance.
     *
     * @param obj  The object to serialize.
     * @param type The type of the object. Usually this would be the result of a
     *             {@link TypeToken} if your {@code obj} is parameterized.
     *             If your {@code obj} is not parameterized, you want to use
     *             {@link #jsonBody(Object)}
     */
    protected void jsonBody(Object obj, Type type) {
        jsonBody(getGson(), obj, type);
    }

    /**
     * Adds a JSON body to this request.
     *
     * @param gson The {@link Gson} instance to serialize {@code obj} with.
     * @param obj  The object to serialize.
     * @param type The type of the object. Usually this would be the result of a
     *             {@link TypeToken} if your {@code obj} is parameterized.
     *             If your {@code obj} is not parameterized, you want to use
     *             {@link #jsonBody(Gson, Object)}
     */
    protected void jsonBody(Gson gson, Object obj, Type type) {
        assert body == null : "Body has already been set.";
        assert WebUtils.permitsRequestBody(method) : "Request does not permit a body";

        body = WebBody.string(gson.toJson(obj, type), JSON);
    }

    protected void fillRequest(EngineRequest request) {
        assert !WebUtils.requiresRequestBody(method) || body != null : "HTTP Method " + method + " requires a body.";

        request.method(method, body);
        request.url(WebUtils.encodeQueryParameters(url, queryParameters));
        request.headers(headers);
    }
}
