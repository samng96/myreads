import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import me.samng.myreads.api.MainVerticle;
import me.samng.myreads.api.entities.UserEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.chrono.ThaiBuddhistEra;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {
    private Vertx vertx;
    private int port = 8080;

    private Future deleteUser(
        TestContext context,
        WebClient client,
        long userId) {
        final Async async = context.async();
        Future fut = Future.future();

        client.delete(port, "localhost", "/users/" + Long.toString(userId))
            .send(ar -> {
                HttpResponse<Buffer> r = ar.result();

                context.assertEquals(r.statusCode(), 204);
                async.complete();
                fut.complete();
            });

        return fut;
    }

    private Future<Long> postUser(
        TestContext context,
        WebClient client) {
        final Async async = context.async();
        Future<Long> fut = Future.future();

        UserEntity entity = new UserEntity();
        entity.email = "test@test.com";
        entity.name = "testuser";
        entity.userId = "testId";
        client.post(port, "localhost", "/users")
            .sendJson(entity,
                ar -> {
                HttpResponse<Buffer> response = ar.result();

                context.assertEquals(response.statusCode(), 201);
                async.complete();
                fut.complete(Long.decode(response.bodyAsString()));
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
    public void root(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        client.get(port, "localhost", "/")
                .send(ar -> {
                    HttpResponse<Buffer> response = ar.result();

                    context.assertEquals(response.statusCode(), 200);
                    context.assertTrue(response.body().toString().contains("myReads API service"));
                    async.complete();
                });
    }

    @Test
    public void getAllUsers(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        client.get(port, "localhost", "/users")
                .send(ar -> {
                    HttpResponse<Buffer> response = ar.result();

                    context.assertEquals(response.statusCode(), 200);
                    async.complete();
                });
    }

    @Test
    public void postUser(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        Future<Long> fut = postUser(context, client);

        fut.setHandler(userId -> {
            Future deleteFut = deleteUser(context, client, userId.result());
            deleteFut.setHandler(x -> { async.complete(); });
        });
    }

    @Test
    public void getUser(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        Future<Long> fut = postUser(context, client);
        client.get(port, "localhost", "/users/12345")
                .send(ar -> {
                    HttpResponse<Buffer> response = ar.result();

                    context.assertEquals(response.statusCode(), 200);
                    context.assertTrue(response.body().toString().contains("getUser"));
                    async.complete();
                });
    }

    @Test
    public void putUser(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        client.put(port, "localhost", "/users/12345")
                .send(ar -> {
                    HttpResponse<Buffer> response = ar.result();

                    context.assertTrue(response.statusCode() == 201 || response.statusCode() == 200);
                    context.assertTrue(response.body().toString().contains("putUser"));
                    async.complete();
                });
    }

    @Test
    public void deleteUser(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        client.delete(port, "localhost", "/users/12345")
                .send(ar -> {
                    HttpResponse<Buffer> response = ar.result();

                    context.assertEquals(response.statusCode(), 204);
                    async.complete();
                });
    }
}
