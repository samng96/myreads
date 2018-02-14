import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {

    private UserRoute userRoute;
    public static String AppId = "uplifted-road-163307";

    public MainVerticle() {
        userRoute = new UserRoute();
    }

    @Override
    public void start(Future<Void> fut) throws Exception {
        Router router = Router.router(vertx);

        router.route("/").handler(routingContext -> {
                    HttpServerResponse response = routingContext.response();
                    response
                            .putHeader("content-type", "text/plain")
                            .end("myReads API service");
                });

        router.mountSubRouter("/users", setupUsers());

        vertx.createHttpServer().requestHandler(router::accept).listen(8080, result -> {
            if (result.succeeded()) {
                fut.complete();
            } else {
                fut.fail(result.cause());
            }});
        System.out.println("HTTP server started on port 8080");
    }

    private Router setupUsers() {
        Router router = Router.router(vertx);

        router.get("/:userId").handler(routingContext -> { userRoute.getUser(routingContext); });
        router.put("/:userId").handler(routingContext -> { userRoute.putUser(routingContext); });
        router.delete("/:userId").handler(routingContext -> { userRoute.deleteUser(routingContext); });

        router.get().handler(routingContext -> { userRoute.getAllUsers(routingContext); });
        router.post().handler(routingContext -> { userRoute.postUser(routingContext); });

        return router;
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(new JsonObject()
                .put("http.port", 8081)
                .put("myreads_host", "localhost")
        );
        vertx.deployVerticle(MainVerticle.class.getName(), deploymentOptions);
    }
}
