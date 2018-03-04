package me.samng.myreads.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import me.samng.myreads.api.routes.FollowedListRoute;
import me.samng.myreads.api.routes.ReadingListElementRoute;
import me.samng.myreads.api.routes.ReadingListRoute;
import me.samng.myreads.api.routes.UserRoute;

public class MainVerticle extends AbstractVerticle {

    private UserRoute userRoute;
    private ReadingListRoute readingListRoute;
    private FollowedListRoute followedListRoute;
    private ReadingListElementRoute readingListElementRoute;
    public static String AppId = "uplifted-road-163307";

    public MainVerticle() {
        userRoute = new UserRoute();
        readingListRoute = new ReadingListRoute();
        readingListElementRoute = new ReadingListElementRoute();
        followedListRoute = new FollowedListRoute();
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

        router.mountSubRouter("/users", setupFollowedLists());
        router.mountSubRouter("/users", setupReadingListElements());
        router.mountSubRouter("/users", setupReadingLists());
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

        router.route().handler(BodyHandler.create());

        router.get("/:userId").handler(routingContext -> { userRoute.getUser(routingContext); });
        router.put("/:userId").handler(routingContext -> { userRoute.putUser(routingContext); });
        router.delete("/:userId").handler(routingContext -> { userRoute.deleteUser(routingContext); });

        router.get().handler(routingContext -> { userRoute.getAllUsers(routingContext); });
        router.post().handler(routingContext -> { userRoute.postUser(routingContext); });

        return router;
    }

    private Router setupReadingLists() {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.post("/:userId/readingLists/:readingListId/addReadingListElements").handler(routingContext -> { readingListRoute.addReadingListElementsToReadingList(routingContext); });
        router.delete("/:userId/readingLists/:readingListId/readingListElements/:readingListElementId").handler(routingContext -> { readingListRoute.deleteReadingListElementFromReadingList(routingContext); });

        router.get("/:userId/readingLists/:readingListId").handler(routingContext -> { readingListRoute.getReadingList(routingContext); });
        router.put("/:userId/readingLists/:readingListId").handler(routingContext -> { readingListRoute.putReadingList(routingContext); });
        router.delete("/:userId/readingLists/:readingListId").handler(routingContext -> { readingListRoute.deleteReadingList(routingContext); });

        router.get("/:userId/readingLists").handler(routingContext -> { readingListRoute.getAllReadingLists(routingContext); });
        router.post("/:userId/readingLists").handler(routingContext -> { readingListRoute.postReadingList(routingContext); });

        return router;
    }

    private Router setupFollowedLists() {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.delete("/:userId/followedLists/:followedListId").handler(routingContext -> { followedListRoute.deleteFollowedList(routingContext); });

        router.get("/:userId/followedLists").handler(routingContext -> { followedListRoute.getAllFollowedLists(routingContext); });
        router.post("/:userId/followedLists").handler(routingContext -> { followedListRoute.postFollowedList(routingContext); });

        return router;
    }

    private Router setupReadingListElements() {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.get("/:userId/readingListElements/:readingListElementId").handler(routingContext -> { readingListElementRoute.getReadingListElement(routingContext); });
        router.put("/:userId/readingListElements/:readingListElementId").handler(routingContext -> { readingListElementRoute.putReadingListElement(routingContext); });
        router.delete("/:userId/readingListElements/:readingListElementId").handler(routingContext -> { readingListElementRoute.deleteReadingListElement(routingContext); });

        router.get("/:userId/readingListElements").handler(routingContext -> { readingListElementRoute.getAllReadingListElements(routingContext); });
        router.post("/:userId/readingListElements").handler(routingContext -> { readingListElementRoute.postReadingListElement(routingContext); });

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
