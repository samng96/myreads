import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import me.samng.myreads.api.MainVerticle;
import me.samng.myreads.api.entities.CommentEntity;
import me.samng.myreads.api.entities.ReadingListElementEntity;
import me.samng.myreads.api.entities.UserEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class CommentRouteTest {
    private Vertx vertx;
    private long userId = -1;
    private long rleId = -1;
    private long commentId = -1;

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
    public void getAllComments(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        UserEntity entity = new UserEntity();
        entity.email = "commentstest@test.com";
        entity.name = "commentsuser";
        entity.userId = "commentsuserId";

        Future<Long> postFut = TestHelper.postUser(context, client, entity, HttpResponseStatus.CREATED.code());
        Future<Long> postRLEFut = postFut.compose(userId -> {
                ReadingListElementEntity rle = new ReadingListElementEntity();
                rle.userId = userId;
                rle.description = "commentDescription";
                rle.name = "commentRleName";
                rle.amazonLink = "some amazon link";

                this.userId = userId;

                return TestHelper.postReadingListElement(context, client, rle, userId, HttpResponseStatus.CREATED.code());
            });
        Future<Long> createCommentEntity = postRLEFut.compose(rleId -> {
            this.rleId = rleId;

            return TestHelper.getAllComments(context, client, this.userId, this.rleId, HttpResponseStatus.OK.code()).map(this.userId);
        });
        Future<Void> deleteRLEFut = createCommentEntity.compose(userId -> {
            return TestHelper.deleteReadingListElement(context, client, this.userId, this.rleId, HttpResponseStatus.NO_CONTENT.code());
        });
        deleteRLEFut.compose(x -> {
            return TestHelper.deleteUser(context, client, this.userId, HttpResponseStatus.NO_CONTENT.code());
        })
            .setHandler(x -> { async.complete(); });
    }

    @Test
    public void postComment(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        UserEntity entity = new UserEntity();
        entity.email = "commentstest@test.com";
        entity.name = "commentsuser";
        entity.userId = "commentsuserId";

        Future<Long> postFut = TestHelper.postUser(context, client, entity, HttpResponseStatus.CREATED.code());
        Future<Long> postRLEFut = postFut.compose(userId -> {
            ReadingListElementEntity rle = new ReadingListElementEntity();
            rle.userId = userId;
            rle.description = "commentDescription";
            rle.name = "commentRleName";
            rle.amazonLink = "some amazon link";

            this.userId = userId;

            return TestHelper.postReadingListElement(context, client, rle, userId, HttpResponseStatus.CREATED.code());
        });
        Future<Long> createCommentEntity = postRLEFut.compose(rleId -> {
            CommentEntity commentEntity = new CommentEntity();
            commentEntity.readingListElementId = rleId;
            commentEntity.userId = this.userId;
            commentEntity.commentText = "commentTest";

            this.rleId = rleId;

            return TestHelper.postComment(context, client, userId, rleId, commentEntity, HttpResponseStatus.CREATED.code());
        });
        Future<CommentEntity> getCommentFut = createCommentEntity.compose(commentId -> {
            this.commentId = commentId;

            return TestHelper.getComment(context, client, this.userId, this.rleId, commentId, HttpResponseStatus.OK.code());
        });
        Future<ReadingListElementEntity> getRleFut = getCommentFut.compose(commentEntity -> {
            context.assertEquals(commentEntity.readingListElementId, this.rleId);
            context.assertEquals(commentEntity.commentText, "commentTest");
            context.assertEquals(commentEntity.userId, this.userId);

            return TestHelper.getReadingListElement(context, client, this.userId, this.rleId, HttpResponseStatus.OK.code());
        });
        Future<Void> deleteCommentFut = getRleFut.compose(rleEntity -> {
            context.assertTrue(rleEntity.commentIds.contains(this.commentId));

            return TestHelper.deleteComment(context, client, this.userId, this.rleId, this.commentId, HttpResponseStatus.NO_CONTENT.code());
        });
        Future<Void> deleteRLEFut = deleteCommentFut.compose(x -> {
            return TestHelper.deleteReadingListElement(context, client, this.userId, this.rleId, HttpResponseStatus.NO_CONTENT.code());
        });
        deleteRLEFut.compose(x -> {
            return TestHelper.deleteUser(context, client, this.userId, HttpResponseStatus.NO_CONTENT.code());
        })
            .setHandler(x -> { async.complete(); });
    }

    @Test
    public void putComment(TestContext context) {
        final Async async = context.async();

        WebClient client = WebClient.create(vertx);

        UserEntity entity = new UserEntity();
        entity.email = "commentstest@test.com";
        entity.name = "commentsuser";
        entity.userId = "commentsuserId";

        Future<Long> postFut = TestHelper.postUser(context, client, entity, HttpResponseStatus.CREATED.code());
        Future<Long> postRLEFut = postFut.compose(userId -> {
            ReadingListElementEntity rle = new ReadingListElementEntity();
            rle.userId = userId;
            rle.description = "commentDescription";
            rle.name = "commentRleName";
            rle.amazonLink = "some amazon link";

            this.userId = userId;

            return TestHelper.postReadingListElement(context, client, rle, userId, HttpResponseStatus.CREATED.code());
        });
        Future<Long> createCommentEntity = postRLEFut.compose(rleId -> {
            CommentEntity commentEntity = new CommentEntity();
            commentEntity.readingListElementId = rleId;
            commentEntity.userId = this.userId;
            commentEntity.commentText = "commentTest";

            this.rleId = rleId;

            return TestHelper.postComment(context, client, userId, rleId, commentEntity, HttpResponseStatus.CREATED.code());
        });
        Future<CommentEntity> getCommentFut = createCommentEntity.compose(commentId -> {
            return TestHelper.getComment(context, client, this.userId, this.rleId, commentId, HttpResponseStatus.OK.code());
        });
        Future<Long> putCommentFut = getCommentFut.compose(commentEntity -> {
            context.assertEquals(commentEntity.readingListElementId, this.rleId);
            context.assertEquals(commentEntity.commentText, "commentTest");
            context.assertEquals(commentEntity.userId, this.userId);

            CommentEntity newEntity = new CommentEntity();
            newEntity.id = commentEntity.id;
            newEntity.userId = commentEntity.userId;
            newEntity.readingListElementId = commentEntity.readingListElementId;
            newEntity.commentText = "updatedCommentText";

            return TestHelper.putComment(context, client, this.userId, this.rleId, newEntity, HttpResponseStatus.NO_CONTENT.code()).map(newEntity.id);
        });
        Future<CommentEntity> getCommentAgainFut = putCommentFut.compose(commentId -> {
            return TestHelper.getComment(context, client, this.userId, this.rleId, commentId, HttpResponseStatus.OK.code());
        });
        Future<Void> deleteCommentFut = getCommentAgainFut.compose(commentEntity -> {
            context.assertEquals(commentEntity.readingListElementId, this.rleId);
            context.assertEquals(commentEntity.commentText, "updatedCommentText");
            context.assertEquals(commentEntity.userId, this.userId);

            return TestHelper.deleteComment(context, client, this.userId, this.rleId, commentEntity.id, HttpResponseStatus.NO_CONTENT.code());
        });
        Future<Void> deleteRLEFut = deleteCommentFut.compose(x -> {
            return TestHelper.deleteReadingListElement(context, client, this.userId, this.rleId, HttpResponseStatus.NO_CONTENT.code());
        });
        deleteRLEFut.compose(x -> {
            return TestHelper.deleteUser(context, client, this.userId, HttpResponseStatus.NO_CONTENT.code());
        })
            .setHandler(x -> { async.complete(); });
    }
}
