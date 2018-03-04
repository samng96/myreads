import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import me.samng.myreads.api.entities.ReadingListEntity;
import me.samng.myreads.api.entities.UserEntity;

public class TestHelper {
    private static int port = 8080;

    public static Future<Void> getAllReadingLists(
        TestContext context,
        WebClient client,
        long userId,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.get(port, "localhost", "/users/" + userId + "/readingLists")
            .send(ar -> {
                HttpResponse<Buffer> response = ar.result();

                context.assertEquals(response.statusCode(), expectedStatusCode);
                fut.complete();
            });

        return fut;
    }

    public static Future<Void> deleteUser(
        TestContext context,
        WebClient client,
        long userId,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.delete(port, "localhost", "/users/" + Long.toString(userId))
            .send(ar -> {
                HttpResponse<Buffer> r = ar.result();

                context.assertEquals(r.statusCode(), expectedStatusCode);
                fut.complete();
            });

        return fut;
    }

    public static Future<Long> postUser(
        TestContext context,
        WebClient client,
        UserEntity entity,
        int expectedStatusCode) {
        Future<Long> fut = Future.future();

        client.post(port, "localhost", "/users")
            .sendJson(entity,
                ar -> {
                    HttpResponse<Buffer> response = ar.result();

                    context.assertEquals(response.statusCode(), expectedStatusCode);
                    fut.complete(Long.decode(response.bodyAsString()));
                });
        return fut;
    }

    public static Future<Void> deleteReadingList(
        TestContext context,
        WebClient client,
        long userId,
        long listId,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.delete(port, "localhost", "/users/" + userId + "/readingLists/" + listId)
            .send(ar -> {
                HttpResponse<Buffer> r = ar.result();

                context.assertEquals(r.statusCode(), expectedStatusCode);
                fut.complete();
            });

        return fut;
    }

    public static Future<Long> postReadingList(
        TestContext context,
        WebClient client,
        ReadingListEntity entity,
        long userId,
        int expectedStatusCode) {
        Future<Long> fut = Future.future();

        client.post(port, "localhost", "/users/" + Long.toString(userId) + "/readingLists")
            .sendJson(entity,
                ar -> {
                    HttpResponse<Buffer> response = ar.result();

                    context.assertEquals(response.statusCode(), expectedStatusCode);
                    fut.complete(Long.decode(response.bodyAsString()));
                });
        return fut;
    }

    public static Future<Void> putReadingList(
        TestContext context,
        WebClient client,
        ReadingListEntity entity,
        long userId,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.put(port, "localhost", "/users/" + Long.toString(userId) + "/readingLists/" + entity.id)
            .sendJson(entity,
                ar -> {
                    HttpResponse<Buffer> response = ar.result();

                    context.assertEquals(response.statusCode(), expectedStatusCode);
                    fut.complete();
                });
        return fut;
    }

    public static Future<ReadingListEntity> getReadingList(
        TestContext context,
        WebClient client,
        long userId,
        long listId,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.get(port, "localhost", "/users/" + userId + "/readingLists/" + listId)
            .send(ar -> {
                HttpResponse<Buffer> response = ar.result();

                context.assertEquals(response.statusCode(), expectedStatusCode);

                if (expectedStatusCode != 404) {
                    ReadingListEntity entity = Json.decodeValue(response.body(), ReadingListEntity.class);
                    fut.complete(entity);
                }
                else {
                    fut.complete(null);
                }
            });
        return fut;
    }
}
