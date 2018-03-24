import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
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
    private long userId = -1;
    private long ownerId = -1;
    private long listId = -1;

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

        Future<Long> postFut = TestHelper.postUser(context, client, entity, HttpResponseStatus.CREATED.code());
        Future<FollowedListEntity[]> getFut = postFut.compose(userId -> {
                this.userId = userId;

                return TestHelper.getFollowedLists(context, client, userId, HttpResponseStatus.OK.code());
            });
        getFut.compose(x -> {
            return TestHelper.deleteUser(context, client, this.userId, HttpResponseStatus.NO_CONTENT.code());
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

        Future<Long> postFut = TestHelper.postUser(context, client, entity, HttpResponseStatus.CREATED.code());
        Future<Long> postOwnerFut = postFut.compose(userId -> {
                this.userId = userId;

                return TestHelper.postUser(context, client, entity, HttpResponseStatus.CREATED.code());
            });
        Future<Long> postListFut = postOwnerFut.compose(ownerId -> {
            ReadingListEntity listEntity = new ReadingListEntity();
            listEntity.userId = userId;
            listEntity.description = "postFollowedListDescription";
            listEntity.name = "postFollowedListTestList";

            this.ownerId = ownerId;

            return TestHelper.postReadingList(context, client, listEntity, ownerId, HttpResponseStatus.CREATED.code()); });
        Future<Long> postFollowedListFut = postListFut.compose(listId -> {
            FollowedListEntity followedListEntity = new FollowedListEntity();
            followedListEntity.listId = listId;
            followedListEntity.ownerId = this.ownerId;

            this.listId = listId;

            return TestHelper.postFollowedList(context, client, followedListEntity,  userId, HttpResponseStatus.CREATED.code());
        });
        Future<FollowedListEntity[]> getFollowedListFut = postFollowedListFut.compose(listId -> {
            return TestHelper.getFollowedLists(context, client, this.userId, HttpResponseStatus.OK.code());
        });
        Future<Void> deleteListFut = getFollowedListFut.compose(e -> {
            context.assertEquals(e[0].listId, this.listId);
            context.assertEquals(e[0].ownerId, this.ownerId);
            context.assertEquals(e[0].userId, this.userId);

            return TestHelper.deleteReadingList(context, client, this.ownerId, this.listId, HttpResponseStatus.NO_CONTENT.code());
        });
        Future<FollowedListEntity[]> getFollowedListAgainFut = deleteListFut.compose(x -> {
            return TestHelper.getFollowedLists(context, client, this.userId, HttpResponseStatus.OK.code());
        });
        Future<Void> deleteListFollowedFut = getFollowedListAgainFut.compose(e -> {
            context.assertEquals(e[0].orphaned, true);

            return TestHelper.deleteFollowedList(context, client, this.userId, e[0].id(), HttpResponseStatus.NO_CONTENT.code());
        });
        Future<Void> deleteUserFut = deleteListFollowedFut.compose(x -> {
            return TestHelper.deleteUser(context, client, this.userId, HttpResponseStatus.NO_CONTENT.code());
        });
        deleteUserFut.compose(x -> {
            return TestHelper.deleteUser(context, client, this.ownerId, HttpResponseStatus.NO_CONTENT.code());
        })
            .setHandler(x -> { async.complete(); });
    }
}
