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

        Future<Long> postFut = TestHelper.postUser(context, client, entity, 201);
        Future<Long> postRLEFut = postFut.compose(userId -> {
                ReadingListElementEntity rle = new ReadingListElementEntity();
                rle.userId = userId;
                rle.description = "commentDescription";
                rle.name = "commentRleName";
                rle.amazonLink = "some amazon link";

                this.userId = userId;

                return TestHelper.postReadingListElement(context, client, rle, userId, 201);
            });
        Future<Long> createCommentEntity = postRLEFut.compose(rleId -> {
            this.rleId = rleId;

            return TestHelper.getAllComments(context, client, this.userId, this.rleId, 200).map(this.userId);
        });
        Future<Void> deleteRLEFut = createCommentEntity.compose(userId -> {
            return TestHelper.deleteReadingListElement(context, client, this.userId, this.rleId, 204);
        });
        deleteRLEFut.compose(x -> {
            return TestHelper.deleteUser(context, client, this.userId, 204);
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

        Future<Long> postFut = TestHelper.postUser(context, client, entity, 201);
        Future<Long> postRLEFut = postFut.compose(userId -> {
            ReadingListElementEntity rle = new ReadingListElementEntity();
            rle.userId = userId;
            rle.description = "commentDescription";
            rle.name = "commentRleName";
            rle.amazonLink = "some amazon link";

            this.userId = userId;

            return TestHelper.postReadingListElement(context, client, rle, userId, 201);
        });
        Future<Long> createCommentEntity = postRLEFut.compose(rleId -> {
            CommentEntity commentEntity = new CommentEntity();
            commentEntity.readingListElementId = rleId;
            commentEntity.userId = this.userId;
            commentEntity.commentText = "commentTest";

            this.rleId = rleId;

            return TestHelper.postComment(context, client, userId, rleId, commentEntity, 201);
        });
        Future<CommentEntity> getCommentFut = createCommentEntity.compose(commentId -> {
            return TestHelper.getComment(context, client, this.userId, this.rleId, commentId, 200);
        });
        Future<Void> deleteCommentFut = getCommentFut.compose(commentEntity -> {
            context.assertEquals(commentEntity.readingListElementId, this.rleId);
            context.assertEquals(commentEntity.commentText, "commentTest");
            context.assertEquals(commentEntity.userId, this.userId);

            return TestHelper.deleteComment(context, client, this.userId, this.rleId, commentEntity.id, 204);
        });
        Future<Void> deleteRLEFut = deleteCommentFut.compose(x -> {
            return TestHelper.deleteReadingListElement(context, client, this.userId, this.rleId, 204);
        });
        deleteRLEFut.compose(x -> {
            return TestHelper.deleteUser(context, client, this.userId, 204);
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

        Future<Long> postFut = TestHelper.postUser(context, client, entity, 201);
        Future<Long> postRLEFut = postFut.compose(userId -> {
            ReadingListElementEntity rle = new ReadingListElementEntity();
            rle.userId = userId;
            rle.description = "commentDescription";
            rle.name = "commentRleName";
            rle.amazonLink = "some amazon link";

            this.userId = userId;

            return TestHelper.postReadingListElement(context, client, rle, userId, 201);
        });
        Future<Long> createCommentEntity = postRLEFut.compose(rleId -> {
            CommentEntity commentEntity = new CommentEntity();
            commentEntity.readingListElementId = rleId;
            commentEntity.userId = this.userId;
            commentEntity.commentText = "commentTest";

            this.rleId = rleId;

            return TestHelper.postComment(context, client, userId, rleId, commentEntity, 201);
        });
        Future<CommentEntity> getCommentFut = createCommentEntity.compose(commentId -> {
            return TestHelper.getComment(context, client, this.userId, this.rleId, commentId, 200);
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

            return TestHelper.putComment(context, client, this.userId, this.rleId, newEntity, 204).map(newEntity.id);
        });
        Future<CommentEntity> getCommentAgainFut = putCommentFut.compose(commentId -> {
            return TestHelper.getComment(context, client, this.userId, this.rleId, commentId, 200);
        });
        Future<Void> deleteCommentFut = getCommentAgainFut.compose(commentEntity -> {
            context.assertEquals(commentEntity.readingListElementId, this.rleId);
            context.assertEquals(commentEntity.commentText, "updatedCommentText");
            context.assertEquals(commentEntity.userId, this.userId);

            return TestHelper.deleteComment(context, client, this.userId, this.rleId, commentEntity.id, 204);
        });
        Future<Void> deleteRLEFut = deleteCommentFut.compose(x -> {
            return TestHelper.deleteReadingListElement(context, client, this.userId, this.rleId, 204);
        });
        deleteRLEFut.compose(x -> {
            return TestHelper.deleteUser(context, client, this.userId, 204);
        })
            .setHandler(x -> { async.complete(); });
    }
}
