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

        Future<Long> postFut = TestHelper.postUser(context, client, entity, 201);
        Future<FollowedListEntity[]> getFut = postFut.compose(userId -> {
                this.userId = userId;

                return TestHelper.getFollowedLists(context, client, userId, 200);
            });
        getFut.compose(x -> {
            return TestHelper.deleteUser(context, client, this.userId, 204);
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

        Future<Long> postFut = TestHelper.postUser(context, client, entity, 201);
        Future<Long> postOwnerFut = postFut.compose(userId -> {
                this.userId = userId;

                return TestHelper.postUser(context, client, entity, 201);
            });
        Future<Long> postListFut = postOwnerFut.compose(ownerId -> {
            ReadingListEntity listEntity = new ReadingListEntity();
            listEntity.userId = userId;
            listEntity.description = "postFollowedListDescription";
            listEntity.name = "postFollowedListTestList";

            this.ownerId = ownerId;

            return TestHelper.postReadingList(context, client, listEntity, ownerId, 201); });
        Future<Long> postFollowedListFut = postListFut.compose(listId -> {
            FollowedListEntity followedListEntity = new FollowedListEntity();
            followedListEntity.listId = listId;
            followedListEntity.ownerId = this.ownerId;

            this.listId = listId;

            return TestHelper.postFollowedList(context, client, followedListEntity,  userId, 201);
            });
        Future<FollowedListEntity[]> getFollowedListFut = postFollowedListFut.compose(listId -> {
            return TestHelper.getFollowedLists(context, client, this.userId, 200);
            });
        Future<Long> deleteFollowedListFut = getFollowedListFut.compose(e -> {
            context.assertEquals(e[0].listId, this.listId);
            context.assertEquals(e[0].ownerId, this.ownerId);
            context.assertEquals(e[0].userId, this.userId);

            return TestHelper.deleteFollowedList(context, client, this.userId, e[0].id(), 204).map(e[0].id);
        });
        Future<Void> deleteListFut = deleteFollowedListFut.compose(e -> {
            return TestHelper.deleteReadingList(context, client, this.ownerId, this.listId, 204);
        });
        Future<Void> deleteUserFut = deleteListFut.compose(x -> {
            return TestHelper.deleteUser(context, client, this.userId, 204);
        });
        deleteUserFut.compose(x -> {
            return TestHelper.deleteUser(context, client, this.ownerId, 204);
        })
            .setHandler(x -> { async.complete(); });
    }
}
