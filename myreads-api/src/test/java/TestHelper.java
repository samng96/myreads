import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import me.samng.myreads.api.entities.FollowedListEntity;
import me.samng.myreads.api.entities.ReadingListElementEntity;
import me.samng.myreads.api.entities.ReadingListEntity;
import me.samng.myreads.api.entities.UserEntity;

public class TestHelper {
    private static int port = 8080;

    public static Future<Void> getAllUsers(
        TestContext context,
        WebClient client,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.get(port, "localhost", "/users")
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

    public static Future<Void> putUser(
        TestContext context,
        WebClient client,
        UserEntity entity,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.put(port, "localhost", "/users/" + entity.id)
            .sendJson(entity,
                ar -> {
                    HttpResponse<Buffer> response = ar.result();

                    context.assertEquals(response.statusCode(), expectedStatusCode);
                    fut.complete();
                });
        return fut;
    }

    public static Future<UserEntity> getUser(
        TestContext context,
        WebClient client,
        long userId,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.get(port, "localhost", "/users/" + userId)
            .send(ar -> {
                HttpResponse<Buffer> response = ar.result();

                context.assertEquals(response.statusCode(), expectedStatusCode);

                if (expectedStatusCode != 404) {
                    UserEntity entity = Json.decodeValue(response.body(), UserEntity.class);
                    fut.complete(entity);
                }
                else {
                    fut.complete(null);
                }
            });
        return fut;
    }

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

    public static Future<Long> postFollowedList(
        TestContext context,
        WebClient client,
        FollowedListEntity followedListEntity,
        long userId,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.post(port, "localhost", "/users/" + userId + "/followedLists")
            .sendJson(followedListEntity,
                ar -> {
                    HttpResponse<Buffer> response = ar.result();

                    context.assertEquals(response.statusCode(), expectedStatusCode);
                    fut.complete(Long.decode(response.bodyAsString()));
                });
        return fut;
    }

    public static Future<FollowedListEntity[]> getFollowedLists(
        TestContext context,
        WebClient client,
        long userId,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.get(port, "localhost", "/users/" + userId + "/followedLists")
            .send(ar -> {
                HttpResponse<Buffer> response = ar.result();

                context.assertEquals(response.statusCode(), expectedStatusCode);

                FollowedListEntity[] results = Json.decodeValue(response.body(), FollowedListEntity[].class);
                fut.complete(results);
            });
        return fut;
    }

    public static Future<Void> deleteFollowedList(
        TestContext context,
        WebClient client,
        long userId,
        long listId,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.delete(port, "localhost", "/users/" + userId + "/followedLists/" + listId)
            .send(ar -> {
                HttpResponse<Buffer> r = ar.result();

                context.assertEquals(r.statusCode(), expectedStatusCode);
                fut.complete();
            });

        return fut;
    }

    public static Future<Void> getAllReadingListElements(
        TestContext context,
        WebClient client,
        long userId,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.get(port, "localhost", "/users/" + userId + "/readingListElements")
            .send(ar -> {
                HttpResponse<Buffer> response = ar.result();

                context.assertEquals(response.statusCode(), expectedStatusCode);
                fut.complete();
            });

        return fut;
    }

    public static Future<Void> deleteReadingListElement(
        TestContext context,
        WebClient client,
        long userId,
        long rleId,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.delete(port, "localhost", "/users/" + userId + "/readingListElements/" + rleId)
            .send(ar -> {
                HttpResponse<Buffer> r = ar.result();

                context.assertEquals(r.statusCode(), expectedStatusCode);
                fut.complete();
            });

        return fut;
    }

    public static Future<Long> postReadingListElement(
        TestContext context,
        WebClient client,
        ReadingListElementEntity entity,
        long rleId,
        int expectedStatusCode) {
        Future<Long> fut = Future.future();

        client.post(port, "localhost", "/users/" + Long.toString(rleId) + "/readingListElements")
            .sendJson(entity,
                ar -> {
                    HttpResponse<Buffer> response = ar.result();

                    context.assertEquals(response.statusCode(), expectedStatusCode);
                    fut.complete(Long.decode(response.bodyAsString()));
                });
        return fut;
    }

    public static Future<Void> putReadingListElement(
        TestContext context,
        WebClient client,
        ReadingListElementEntity entity,
        long userId,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.put(port, "localhost", "/users/" + Long.toString(userId) + "/readingListElements/" + entity.id)
            .sendJson(entity,
                ar -> {
                    HttpResponse<Buffer> response = ar.result();

                    context.assertEquals(response.statusCode(), expectedStatusCode);
                    fut.complete();
                });
        return fut;
    }

    public static Future<ReadingListElementEntity> getReadingListElement(
        TestContext context,
        WebClient client,
        long userId,
        long rleId,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.get(port, "localhost", "/users/" + userId + "/readingListElements/" + rleId)
            .send(ar -> {
                HttpResponse<Buffer> response = ar.result();

                context.assertEquals(response.statusCode(), expectedStatusCode);

                if (expectedStatusCode != 404) {
                    ReadingListElementEntity entity = Json.decodeValue(response.body(), ReadingListElementEntity.class);
                    fut.complete(entity);
                }
                else {
                    fut.complete(null);
                }
            });
        return fut;
    }

    public static Future<Void> addRLEToReadingList(
        TestContext context,
        WebClient client,
        long userId,
        long listId,
        long[] rleIds,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.post(port, "localhost", "/users/" + userId + "/readingLists/" + listId + "/addReadingListElements")
            .sendJson(rleIds,
            ar -> {
                HttpResponse<Buffer> response = ar.result();

                context.assertEquals(response.statusCode(), expectedStatusCode);
                fut.complete();
            });
        return fut;
    }

    public static Future<Void> removeRLEFromReadingList(
        TestContext context,
        WebClient client,
        long userId,
        long listId,
        long rleId,
        int expectedStatusCode) {
        Future fut = Future.future();

        client.delete(port, "localhost", "/users/" + userId + "/readingLists/" + listId + "/readingListElements/" + rleId)
            .send(ar -> {
                    HttpResponse<Buffer> response = ar.result();

                    context.assertEquals(response.statusCode(), expectedStatusCode);
                    fut.complete();
                });
        return fut;
    }
}
