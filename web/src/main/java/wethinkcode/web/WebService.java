package wethinkcode.web;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.rendering.JavalinRenderer;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Front-end web server for the LightSched project.
 * Handles communication and interactions with back-end services.
 */
public class WebService {

    public static final int DEFAULT_PORT = 80;

    public static void main(String[] args) {
        WebService webService = new WebService().initialize();
        webService.start();
    }

    private Javalin server;
    private int servicePort;

    @VisibleForTesting
    public WebService initialize() {
        configureHttpClient();
        server = configureHttpServer();
        return this;
    }

    public void start() {
        start(DEFAULT_PORT);
    }

    @VisibleForTesting
    public void start(int networkPort) {
        servicePort = networkPort;
        run();
    }

    public void stop() {
        server.stop();
    }

    public void run() {
        server.start(servicePort);
    }

    private void configureHttpClient() {
        HttpResponse<JsonNode> check = Unirest.get(serverUrl(7000) + "/provinces").asJson();
        System.out.println(check.getBody());
    }

    private Javalin configureHttpServer() {
        JavalinRenderer.register(JavalinThymeleaf.INSTANCE, ".html");
        return Javalin.create(config -> {
            config.addStaticFiles("/templates/", Location.CLASSPATH);
        })
                .get("/", ctx -> {
                    HttpResponse<JsonNode> check = Unirest
                            .get(serverUrl(7000) + "/provinces").asJson();
                    Collection<String> arr = check.getBody().getArray().toList();
                    Collection<String> provinces = List.of("Kwazulu-Natal", "Mpumalanga", "Gauteng");
                    ctx.render("/templates/index.html", Map.of("provinces", arr));
                })
                .get("/populatetowns", ctx -> {
                    String province = ctx.queryParam("province");
                    HttpResponse<JsonNode> check = Unirest
                            .get(serverUrl(7000) + "/towns/" + province).asJson();
                    JSONArray jsonArray = check.getBody().getArray();
                    List<String> arr = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        arr.add(jsonObject.getString("name"));
                    }
                    Collection<String> provinces = new ArrayList<>();
                    ctx.render("/templates/index.html", Map.of("towns", arr, "province", province, "provinces", provinces));
                })
                .get("/findSchedule", ctx -> {
                    String town = ctx.queryParam("town");
                    String province = ctx.queryParam("province");
                    //
                    ctx.render("/templates/index.html");
                });
    }

    private String serverUrl(int servicePort) {
        return "http://localhost:" + servicePort;
    }
}
