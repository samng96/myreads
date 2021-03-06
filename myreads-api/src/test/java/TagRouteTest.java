import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import me.samng.myreads.api.MainVerticle;
import me.samng.myreads.api.entities.TagEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class TagRouteTest {
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
    public void getAllTags(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        TestHelper.getAllTags(context, client, HttpResponseStatus.OK.code()).setHandler(x -> { async.complete(); });
    }

    @Test
    public void postTag(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        TagEntity entity = new TagEntity();
        entity.tagName = "testTag";

        Future<Long> postFut = TestHelper.postTag(context, client, entity, HttpResponseStatus.CREATED.code());
        Future<TagEntity> getTagFut = postFut.compose(tagId -> {
            return TestHelper.getTag(context, client, tagId, HttpResponseStatus.OK.code());
        }).setHandler(x -> { async.complete(); });
    }
}
