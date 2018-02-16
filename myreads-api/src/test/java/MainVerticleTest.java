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
}
