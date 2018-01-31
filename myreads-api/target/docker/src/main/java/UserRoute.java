import io.vertx.ext.web.RoutingContext;

public class UserRoute {
    // Get all users
    public void getAllUsers(RoutingContext routingContext) {
        routingContext.response()
                .putHeader("content-type", "text/plain")
                .end("getAllUsers");
    }

    // Post a new user
    public void postUser(RoutingContext routingContext) {
        routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "text/plain")
                .end("postUser");
    }

    // Get a specific user, /users/{userId}
    public void getUser(RoutingContext routingContext) {
        routingContext.response()
                .putHeader("content-type", "text/plain")
                .end("getUser");
    }

    // Update a user, /users/{userId}
    public void putUser(RoutingContext routingContext) {
        routingContext.response()
                .putHeader("content-type", "text/plain")
                .end("putUser");
    }

    // Delete a user, /users/{userId}
    public void deleteUser(RoutingContext routingContext) {
        routingContext.response()
                .setStatusCode(204)
                .putHeader("content-type", "text/plain")
                .end("deleteUser");
    }
}
