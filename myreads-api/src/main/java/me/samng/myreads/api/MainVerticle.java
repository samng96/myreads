package me.samng.myreads.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import me.samng.myreads.api.routes.*;

import java.util.HashSet;
import java.util.Set;

// TODO: We need to implement auth. We should ensure that every API call is auth'ed, and that we check
// TODO: for ownership of an item only on write, but not on read.

public class MainVerticle extends AbstractVerticle {

    public static int port = 8080;
    private UserRoute userRoute;
    private ReadingListRoute readingListRoute;
    private FollowedListRoute followedListRoute;
    private ReadingListElementRoute readingListElementRoute;
    private CommentRoute commentRoute;
    private TagRoute tagRoute;
    public static String AppId = "uplifted-road-163307";

    public MainVerticle() {
        userRoute = new UserRoute();
        readingListRoute = new ReadingListRoute();
        readingListElementRoute = new ReadingListElementRoute();
        followedListRoute = new FollowedListRoute();
        commentRoute = new CommentRoute();
        tagRoute = new TagRoute();
    }

    @Override
    public void start(Future<Void> fut) throws Exception {
        Router router = Router.router(vertx);

        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("x-requested-with");
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("origin");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("accept");
        allowedHeaders.add("Authorization");

        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.POST);
        allowedMethods.add(HttpMethod.OPTIONS);
        /*
         * these methods aren't necessary for this sample,
         * but you may need them for your projects
         */
        allowedMethods.add(HttpMethod.DELETE);
        allowedMethods.add(HttpMethod.PATCH);
        allowedMethods.add(HttpMethod.PUT);

        router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));

        // Set up all the routes.
        router.route("/").handler(routingContext -> {
                    HttpServerResponse response = routingContext.response();
                    response
                            .putHeader("content-type", "text/plain")
                            .end("myReads API service");
                });

        router.mountSubRouter("/users", setupComments());
        router.mountSubRouter("/users", setupFollowedLists());
        router.mountSubRouter("/users", setupReadingListElements());
        router.mountSubRouter("/users", setupReadingLists());
        router.mountSubRouter("/users", setupUsers());
        router.mountSubRouter("/", setupTags());

        vertx.createHttpServer().requestHandler(router::accept).listen(MainVerticle.port, result -> {
            if (result.succeeded()) {
                fut.complete();
            } else {
                fut.fail(result.cause());
            }});
        System.out.println("HTTP server started on port " + MainVerticle.port);
    }

    private Router setupTags() {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.get("/tags/:tagId").handler(routingContext -> { tagRoute.getTag(routingContext); });
        router.get("/tagByName/:tagName").handler(routingContext -> { tagRoute.getTagByName(routingContext); });
        router.get("/tagsByUser/:userId").handler(routingContext -> { tagRoute.getTagsByUser(routingContext); });

        router.get("/tags").handler(routingContext -> { tagRoute.getAllTags(routingContext); });
        router.post("/tags").handler(routingContext -> { tagRoute.postTag(routingContext); });

        return router;
    }

    private Router setupComments() {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.put("/:userId/readingListElements/:readingListElementId/comments/:commentId").handler(routingContext -> { commentRoute.putComment(routingContext); });
        router.delete("/:userId/readingListElements/:readingListElementId/comments/:commentId").handler(routingContext -> { commentRoute.deleteComment(routingContext); });
        router.get("/:userId/readingListElements/:readingListElementId/comments/:commentId").handler(routingContext -> { commentRoute.getComment(routingContext); });

        router.post("/:userId/readingListElements/:readingListElementId/comments").handler(routingContext -> { commentRoute.postComment(routingContext); });
        router.get("/:userId/readingListElements/:readingListElementId/comments").handler(routingContext -> { commentRoute.getAllComments(routingContext); });

        return router;
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
        router.post("/:userId/readingLists/:readingListId/addTags").handler(routingContext -> { readingListRoute.addTagsToReadingList(routingContext); });
        router.get("/:userId/readingLists/:readingListId/tags").handler(routingContext -> { readingListRoute.getTagsForReadingList(routingContext); });
        router.delete("/:userId/readingLists/:readingListId/tags/:tagId").handler(routingContext -> { readingListRoute.removeTagFromReadingList(routingContext); });

        router.post("/:userId/readingLists/:readingListId/addReadingListElements").handler(routingContext -> { readingListRoute.addReadingListElementsToReadingList(routingContext); });
        router.delete("/:userId/readingLists/:readingListId/readingListElements/:readingListElementId").handler(routingContext -> { readingListRoute.deleteReadingListElementFromReadingList(routingContext); });

        router.get("/:userId/readingLists/:readingListId").handler(routingContext -> { readingListRoute.getReadingList(routingContext); });
        router.put("/:userId/readingLists/:readingListId").handler(routingContext -> { readingListRoute.putReadingList(routingContext); });
        router.delete("/:userId/readingLists/:readingListId").handler(routingContext -> { readingListRoute.deleteReadingList(routingContext); });

        router.get("/:userId/readingLists/*").handler(routingContext -> { readingListRoute.getAllReadingLists(routingContext); });
        router.post("/:userId/readingLists").handler(routingContext -> { readingListRoute.postReadingList(routingContext); });

        router.post("/:userId/readingListsByTag").handler(routingContext -> { readingListRoute.getAllReadingListsByTag(routingContext); });

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
        router.post("/:userId/readingListElements/:readingListElementId/addTags").handler(routingContext -> { readingListElementRoute.addTagsToReadingListElement(routingContext); });
        router.get("/:userId/readingListElements/:readingListElementId/tags").handler(routingContext -> { readingListElementRoute.getTagsForReadingListElement(routingContext); });
        router.delete("/:userId/readingListElements/:readingListElementId/tags/:tagId").handler(routingContext -> { readingListElementRoute.removeTagFromReadingListElement(routingContext); });

        router.get("/:userId/readingListElements/:readingListElementId").handler(routingContext -> { readingListElementRoute.getReadingListElement(routingContext); });
        router.put("/:userId/readingListElements/:readingListElementId").handler(routingContext -> { readingListElementRoute.putReadingListElement(routingContext); });
        router.delete("/:userId/readingListElements/:readingListElementId").handler(routingContext -> { readingListElementRoute.deleteReadingListElement(routingContext); });

        router.get("/:userId/readingListElements/*").handler(routingContext -> { readingListElementRoute.getAllReadingListElements(routingContext); });
        router.post("/:userId/readingListElements").handler(routingContext -> { readingListElementRoute.postReadingListElement(routingContext); });

        router.post("/:userId/readingListElementsByTag").handler(routingContext -> { readingListElementRoute.getAllReadingListElementsByTag(routingContext); });

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
