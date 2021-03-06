import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import me.samng.myreads.api.MainVerticle;
import me.samng.myreads.api.entities.UserEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class UserRouteTest {
    private Vertx vertx;

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
    public void getAllUsers(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        TestHelper.getAllUsers(context, client, HttpResponseStatus.OK.code()).setHandler(x -> { async.complete(); });
    }

    @Test
    public void postUser(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        UserEntity entity = new UserEntity();
        entity.email = "PostUserTest@test.com";
        entity.name = "testuser";
        entity.userId = "testId";

        Future<Long> postFut = TestHelper.postUser(context, client, entity, HttpResponseStatus.CREATED.code());
        Future<UserEntity> getFut = postFut.compose(userId -> { return TestHelper.getUser(context, client, userId, HttpResponseStatus.OK.code()); });
        getFut.compose(e -> {
            context.assertEquals(entity.email, e.email);
            context.assertEquals(entity.name, e.name);
            context.assertEquals(entity.userId, e.userId);

            return TestHelper.deleteUser(context, client, e.id, HttpResponseStatus.NO_CONTENT.code());
        })
            .setHandler(x -> { async.complete(); });
    }

    @Test
    public void putUser(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        UserEntity entity = new UserEntity();
        entity.email = "putUserTest@test.com";
        entity.name = "testuser";
        entity.userId = "testId";
        entity.externalId = "putUserIdTest";

        Future<Long> postFut = TestHelper.postUser(context, client, entity, HttpResponseStatus.CREATED.code());
        Future<Long> putFut = postFut.compose(userId -> {
            UserEntity putEntity = new UserEntity();
            putEntity.id = userId;
            putEntity.email = "changePutUserTest@test.com";
            putEntity.name = "changeduser";
            putEntity.userId = "changeid";
            putEntity.externalId = "putUserIdTest";

            return TestHelper.putUser(context, client, putEntity, HttpResponseStatus.NO_CONTENT.code()).map(userId);
        });
        Future<UserEntity> getFut = putFut.compose(userId -> { return TestHelper.getUser(context, client, userId, HttpResponseStatus.OK.code()); });
        getFut.compose(e -> {
            context.assertEquals("changePutUserTest@test.com", e.email);
            context.assertEquals("changeduser", e.name);
            context.assertEquals("changeid", e.userId);

            return TestHelper.deleteUser(context, client, e.id, HttpResponseStatus.NO_CONTENT.code());
        })
            .setHandler(x -> { async.complete(); });
    }

    @Test
    public void deleteUser(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);
        UserEntity entity = new UserEntity();
        entity.email = "deleteUserTest@test.com";
        entity.name = "testuser";
        entity.userId = "testId";

        Future<Long> postFut = TestHelper.postUser(context, client, entity, HttpResponseStatus.CREATED.code());
        Future<Long> deleteFut = postFut.compose(userId -> {
            return TestHelper.deleteUser(context, client, userId, HttpResponseStatus.NO_CONTENT.code()).map(userId);
        });
        deleteFut.compose(userId -> { return TestHelper.getUser(context, client, userId, HttpResponseStatus.NOT_FOUND.code()); })
            .setHandler(x -> { async.complete(); });
    }
}
