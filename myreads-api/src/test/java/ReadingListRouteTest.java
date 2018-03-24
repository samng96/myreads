import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import me.samng.myreads.api.MainVerticle;
import me.samng.myreads.api.entities.ReadingListElementEntity;
import me.samng.myreads.api.entities.ReadingListEntity;
import me.samng.myreads.api.entities.TagEntity;
import me.samng.myreads.api.entities.UserEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ReadingListRouteTest {
    private Vertx vertx;
    private long userId = -1;
    private long listId = -1;
    private long rleId = -1;
    private long tagId = -1;

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

        Future<Long> postFut = TestHelper.postUser(context, client, entity, HttpResponseStatus.CREATED.code());
        Future<Long> getAllFut = postFut.compose(userId -> {
            return TestHelper.getAllReadingLists(context, client, userId, HttpResponseStatus.OK.code()).map(userId);
        });
        getAllFut.compose(userId -> {
            return TestHelper.deleteUser(context, client, userId, HttpResponseStatus.NO_CONTENT.code());
        })
            .setHandler(x -> { async.complete(); });
    }

    @Test
    public void postList(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        UserEntity entity = new UserEntity();
        entity.email = "PostUserTest@test.com";
        entity.name = "testuser";
        entity.userId = "testId";

        Future<Long> postFut = TestHelper.postUser(context, client, entity, HttpResponseStatus.CREATED.code());
        Future<Long> postListFut = postFut.compose(userId -> {
            ReadingListEntity listEntity = new ReadingListEntity();
            listEntity.userId = userId;
            listEntity.description = "description";
            listEntity.name = "listName";

            this.userId = userId;

            return TestHelper.postReadingList(context, client, listEntity, userId, HttpResponseStatus.CREATED.code()); });
        Future<ReadingListEntity> getFut = postListFut.compose(listId -> {
            return TestHelper.getReadingList(context, client, this.userId, listId, HttpResponseStatus.OK.code());
        });
        Future<Long> deleteListFut = getFut.compose(e -> {
            context.assertEquals("description", e.description);
            context.assertEquals("listName", e.name);
            context.assertEquals(this.userId, e.userId);

            return TestHelper.deleteReadingList(context, client, this.userId, e.id, HttpResponseStatus.NO_CONTENT.code()).map(e.id);
        });
        Future<ReadingListEntity> failGetFut = deleteListFut.compose(listId -> {
            return TestHelper.getReadingList(context, client, this.userId, listId, HttpResponseStatus.NOT_FOUND.code());
        });
        failGetFut.compose(x -> {
            return TestHelper.deleteUser(context, client, this.userId, HttpResponseStatus.NO_CONTENT.code());
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

        Future<Long> postFut = TestHelper.postUser(context, client, entity, HttpResponseStatus.CREATED.code());
        Future<Long> postListFut = postFut.compose(userId -> {
            ReadingListEntity listEntity = new ReadingListEntity();
            listEntity.userId = userId;
            listEntity.description = "description";
            listEntity.name = "listName";

            this.userId = userId;

            return TestHelper.postReadingList(context, client, listEntity, userId, HttpResponseStatus.CREATED.code()); });
        Future<Long> putFut = postListFut.compose(listId -> {
            ReadingListEntity putEntity = new ReadingListEntity();
            putEntity.id = listId;
            putEntity.userId = this.userId;
            putEntity.description = "newdescription";
            putEntity.name = "newlistName";

            return TestHelper.putReadingList(context, client, putEntity, this.userId,HttpResponseStatus.NO_CONTENT.code()).map(listId);
        });
        Future<ReadingListEntity> getFut = putFut.compose(listId -> { return TestHelper.getReadingList(context, client, userId, listId, HttpResponseStatus.OK.code()); });
        Future<Void> deleteFut = getFut.compose(e -> {
            context.assertEquals("newdescription", e.description);
            context.assertEquals("newlistName", e.name);

            return TestHelper.deleteReadingList(context, client, this.userId, e.id, HttpResponseStatus.NO_CONTENT.code());
        });
        deleteFut.compose(x -> {
            return TestHelper.deleteUser(context, client, this.userId, HttpResponseStatus.NO_CONTENT.code());
        })
            .setHandler(x -> { async.complete(); });
    }

    @Test
    public void addElementToList(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        UserEntity entity = new UserEntity();
        entity.email = "putUserTest@test.com";
        entity.name = "testuser";
        entity.userId = "testId";

        Future<Long> postFut = TestHelper.postUser(context, client, entity, HttpResponseStatus.CREATED.code());
        Future<Long> postListFut = postFut.compose(userId -> {
            ReadingListEntity listEntity = new ReadingListEntity();
            listEntity.userId = userId;
            listEntity.description = "description";
            listEntity.name = "listName";

            this.userId = userId;

            return TestHelper.postReadingList(context, client, listEntity, userId, HttpResponseStatus.CREATED.code()); });
        Future<Long> postRLEFut = postListFut.compose(listId -> {
            ReadingListElementEntity rle = new ReadingListElementEntity();
            rle.userId = userId;
            rle.description = "description";
            rle.name = "rleName For addElementToList";
            rle.amazonLink = "some amazon link";

            this.listId = listId;

            return TestHelper.postReadingListElement(context, client, rle, userId, HttpResponseStatus.CREATED.code());
        });
        Future<Void> addToListFut = postRLEFut.compose(rleId -> {
            this.rleId = rleId;

            long[] rleIds = { rleId };
            return TestHelper.addRLEToReadingList(context, client, this.userId, this.listId, rleIds, HttpResponseStatus.OK.code());
        });
        Future<ReadingListEntity> getFut = addToListFut.compose(x -> { return TestHelper.getReadingList(context, client, this.userId, this.listId, HttpResponseStatus.OK.code()); });
        Future<Void> removeFromListFut = getFut.compose(e -> {
            context.assertEquals(e.readingListElementIds.size(), 1);
            context.assertEquals(e.readingListElementIds.get(0), rleId);

            return TestHelper.removeRLEFromReadingList(context, client, this.userId, this.listId, this.rleId, HttpResponseStatus.NO_CONTENT.code());
        });
        Future<ReadingListEntity> getAgainFut = removeFromListFut.compose(x -> { return TestHelper.getReadingList(context, client, this.userId, this.listId, HttpResponseStatus.OK.code()); });
        Future<Void> deleteRleFut = getAgainFut.compose(e -> {
            context.assertEquals(e.readingListElementIds.size(), 0);

            return TestHelper.deleteReadingListElement(context, client, this.userId, this.rleId, HttpResponseStatus.NO_CONTENT.code());
        });
        Future<Void> deleteFut = deleteRleFut.compose(e -> {
            return TestHelper.deleteReadingList(context, client, this.userId, this.listId, HttpResponseStatus.NO_CONTENT.code());
        });
        deleteFut.compose(x -> {
            return TestHelper.deleteUser(context, client, this.userId, HttpResponseStatus.NO_CONTENT.code());
        })
            .setHandler(x -> { async.complete(); });
    }

    @Test
    public void tagTest(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        UserEntity entity = new UserEntity();
        entity.email = "tagReadingList@test.com";
        entity.name = "testuser";
        entity.userId = "testId";

        Future<Long> postFut = TestHelper.postUser(context, client, entity, HttpResponseStatus.CREATED.code());
        Future<Long> postListFut = postFut.compose(userId -> {
            ReadingListEntity listEntity = new ReadingListEntity();
            listEntity.userId = userId;
            listEntity.description = "description";
            listEntity.name = "listName";

            this.userId = userId;

            return TestHelper.postReadingList(context, client, listEntity, userId, HttpResponseStatus.CREATED.code()); });
        Future<Long> postTagFut = postListFut.compose(listId -> {
            TagEntity tagEntity = new TagEntity();
            tagEntity.tagName = "testReadingListTag";

            this.listId = listId;

            return TestHelper.postTag(context, client, tagEntity, HttpResponseStatus.CREATED.code());
        });
        Future<Long> addTagToListFut = postTagFut.compose(tagId -> {
            long[] tagIds = { tagId };

            this.tagId = tagId;

            return TestHelper.addTagToReadingList(context, client, this.userId, this.listId, tagIds, HttpResponseStatus.OK.code()).map(tagId);
        });
        Future<TagEntity[]> getTagFut = addTagToListFut.compose(tagId -> {
            return TestHelper.getTagsForReadingList(context, client, this.userId, this.listId, HttpResponseStatus.OK.code());
        });
        Future<Void> removeTagFut = getTagFut.compose(tagEntities -> {
            context.assertEquals(tagEntities.length, 1);
            context.assertEquals(tagEntities[0].id, tagId);
            context.assertEquals(tagEntities[0].tagName, "testReadingListTag");

            return TestHelper.removeTagFromReadingList(context, client, this.userId, this.listId, this.tagId, HttpResponseStatus.NO_CONTENT.code());
        });
        Future<TagEntity[]> checkTagFut = removeTagFut.compose(tagId -> {
            return TestHelper.getTagsForReadingList(context, client, this.userId, this.listId, HttpResponseStatus.OK.code());
        });
        Future<Void> deleteTagFut = checkTagFut.compose(tagEntities -> {
            context.assertEquals(tagEntities.length, 0);

            return TestHelper.deleteTag(context, client, this.tagId, HttpResponseStatus.NO_CONTENT.code());
        });
        Future<Void> deleteListFut = deleteTagFut.compose(x -> {
            return TestHelper.deleteReadingList(context, client, this.userId, this.listId, HttpResponseStatus.NO_CONTENT.code());
        });
        deleteListFut.compose(x -> {
            return TestHelper.deleteUser(context, client, this.userId, HttpResponseStatus.NO_CONTENT.code());
        })
            .setHandler(x -> { async.complete(); });
    }
}
