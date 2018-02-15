import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import me.samng.myreads.api.MainVerticle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {
    private Vertx vertx;
    private int port = 8080;

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
                    context.assertTrue(response.body().toString().contains("getAllUsers"));
                    async.complete();
                });
    }

    @Test
    public void postUser(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        client.post(port, "localhost", "/users")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", "1")
                .send(ar -> {
                    HttpResponse<Buffer> response = ar.result();

                    context.assertEquals(response.statusCode(), 201);
                    async.complete();
                });
    }

    @Test
    public void getUser(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

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
