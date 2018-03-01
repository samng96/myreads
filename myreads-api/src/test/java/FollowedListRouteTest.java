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
import me.samng.myreads.api.entities.FollowedListEntity;
import me.samng.myreads.api.entities.ReadingListEntity;
import me.samng.myreads.api.entities.UserEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class FollowedListRouteTest {
    private Vertx vertx;
    private int port = 8080;
    private long userId = -1;
    private long ownerId = -1;
    private long listId = -1;

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

    private Future<Long> postFollowedList(
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

    private Future<FollowedListEntity[]> getFollowedLists(
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

    private Future<Void> deleteFollowedList(
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
    public void getAllFollowedLists(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        UserEntity entity = new UserEntity();
        entity.email = "followedlisttest@test.com";
        entity.name = "testuser";
        entity.userId = "testId";

        Future<Long> postFut = postUser(context, client, entity, 201);
        Future<FollowedListEntity[]> getFut = postFut.compose(userId -> {
                this.userId = userId;

                return getFollowedLists(context, client, userId, 200);
            });
        getFut.compose(x -> {
            return deleteUser(context, client, this.userId, 204);
        })
            .setHandler(x -> { async.complete(); });
    }

    @Test
    public void postFollowedList(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        UserEntity entity = new UserEntity();
        entity.email = "PostFollowedListTest@test.com";
        entity.name = "testuser";
        entity.userId = "testId";

        Future<Long> postFut = postUser(context, client, entity, 201);
        Future<Long> postOwnerFut = postFut.compose(userId -> {
                this.userId = userId;

                return postUser(context, client, entity, 201);
            });
        Future<Long> postListFut = postOwnerFut.compose(ownerId -> {
            ReadingListEntity listEntity = new ReadingListEntity();
            listEntity.userId = userId;
            listEntity.description = "postFollowedListDescription";
            listEntity.name = "postFollowedListTestList";

            this.ownerId = ownerId;

            return postReadingList(context, client, listEntity, ownerId, 201); });
        Future<Long> postFollowedListFut = postListFut.compose(listId -> {
            FollowedListEntity followedListEntity = new FollowedListEntity();
            followedListEntity.listId = listId;
            followedListEntity.ownerId = this.ownerId;

            this.listId = listId;

            return postFollowedList(context, client, followedListEntity,  userId, 201);
            });
        Future<FollowedListEntity[]> getFollowedListFut = postFollowedListFut.compose(listId -> {
            return getFollowedLists(context, client, this.userId, 200);
            });
        Future<Long> deleteFollowedListFut = getFollowedListFut.compose(e -> {
            context.assertEquals(e[0].listId, this.listId);
            context.assertEquals(e[0].ownerId, this.ownerId);
            context.assertEquals(e[0].userId, this.userId);

            return deleteFollowedList(context, client, this.userId, e[0].id(), 204).map(e[0].id);
        });
        Future<Void> deleteListFut = deleteFollowedListFut.compose(e -> {
            return deleteReadingList(context, client, this.ownerId, this.listId, 204);
        });
        Future<Void> deleteUserFut = deleteListFut.compose(x -> {
            return deleteUser(context, client, this.userId, 204);
        });
        deleteUserFut.compose(x -> {
            return deleteUser(context, client, this.ownerId, 204);
        })
            .setHandler(x -> { async.complete(); });
    }
}
