import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import me.samng.myreads.api.MainVerticle;
import me.samng.myreads.api.entities.ReadingListEntity;
import me.samng.myreads.api.entities.UserEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(VertxUnitRunner.class)
public class ReadingListRouteTest {
    private Vertx vertx;
    private int port = 8080;
    private long userId = -1;

    private Future<Void> deleteUser(
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

    private Future<Long> postUser(
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

    private Future<Void> deleteReadingList(
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

    private Future<Long> postReadingList(
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

    private Future<Void> putReadingList(
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

    private Future<ReadingListEntity> getReadingList(
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

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(MainVerticle.class.getName(),
            context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void getAllLists(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        UserEntity entity = new UserEntity();
        entity.email = "listtest@test.com";
        entity.name = "testuser";
        entity.userId = "testId";

        Future<Long> postFut = postUser(context, client, entity, 201);
        postFut.setHandler(userId -> {
            client.get(port, "localhost", "/users/" + userId.result() + "/readingLists")
                .send(ar -> {
                    HttpResponse<Buffer> response = ar.result();

                    context.assertEquals(response.statusCode(), 200);
                    deleteUser(context, client, userId.result(), 204).setHandler(x -> { async.complete(); });
                });
        });
    }

    @Test
    public void postList(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        UserEntity entity = new UserEntity();
        entity.email = "PostUserTest@test.com";
        entity.name = "testuser";
        entity.userId = "testId";

        Future<Long> postFut = postUser(context, client, entity, 201);
        Future<Long> postListFut = postFut.compose(userId -> {
            ReadingListEntity listEntity = new ReadingListEntity();
            listEntity.userId = userId;
            listEntity.description = "description";
            listEntity.name = "listName";

            this.userId = userId;

            return postReadingList(context, client, listEntity, userId, 201); });
        Future<ReadingListEntity> getFut = postListFut.compose(listId -> {
            return getReadingList(context, client, this.userId, listId, 200);
        });
        Future<Long> deleteListFut = getFut.compose(e -> {
            context.assertEquals("description", e.description);
            context.assertEquals("listName", e.name);
            context.assertEquals(this.userId, e.userId);

            return deleteReadingList(context, client, this.userId, e.id, 204).map(e.id);
        });
        Future<ReadingListEntity> failGetFut = deleteListFut.compose(listId -> {
            return getReadingList(context, client, this.userId, listId, 404);
        });
        failGetFut.compose(x -> {
            return deleteUser(context, client, this.userId, 204);
        })
            .setHandler(x -> { async.complete(); });
    }

    @Test
    public void putList(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        UserEntity entity = new UserEntity();
        entity.email = "putUserTest@test.com";
        entity.name = "testuser";
        entity.userId = "testId";

        Future<Long> postFut = postUser(context, client, entity, 201);
        Future<Long> postListFut = postFut.compose(userId -> {
            ReadingListEntity listEntity = new ReadingListEntity();
            listEntity.userId = userId;
            listEntity.description = "description";
            listEntity.name = "listName";

            this.userId = userId;

            return postReadingList(context, client, listEntity, userId, 201); });
        Future<Long> putFut = postListFut.compose(listId -> {
            ReadingListEntity putEntity = new ReadingListEntity();
            putEntity.id = listId;
            putEntity.userId = this.userId;
            putEntity.description = "newdescription";
            putEntity.name = "newlistName";

            return putReadingList(context, client, putEntity, this.userId,204).map(listId);
        });
        Future<ReadingListEntity> getFut = putFut.compose(listId -> { return getReadingList(context, client, userId, listId, 200); });
        Future<Void> deleteFut = getFut.compose(e -> {
            context.assertEquals("newdescription", e.description);
            context.assertEquals("newlistName", e.name);

            return deleteReadingList(context, client, this.userId, e.id, 204);
        });
        deleteFut.compose(x -> {
            return deleteUser(context, client, this.userId, 204);
        })
            .setHandler(x -> { async.complete(); });
    }
}
