import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/mergeMap';

import { ServiceApi } from '../serviceapi.service';
import { TagEntity, UserEntity, ReadingListEntity, CommentEntity, FollowedListEntity, ReadingListElementEntity } from '../entities';
import { LoggerService } from '../logger.service';
import { LocalStorageObjectService } from '../LocalStorageObject';

@Component({
    selector: 'app-readinglistelements',
    templateUrl: './readinglistelements.component.html',
})
export class ReadingListElementsComponent implements OnInit {
    rleId: number;
    userId: number;

    ownRle: boolean;
    addTagName: string; // Bound to the form.
    addComment: string; // Bound to the form.
    addRleToListId: string; // Bound to the form.
    readingListElement: ReadingListElementEntity; // This is for the display.
    comments: CommentEntity[]; // This is for the display.
    lists: ReadingListEntity[];

    private numTagStyles: number = 6;

    constructor(
        private lso: LocalStorageObjectService,
        private route: ActivatedRoute,
        private serviceApi: ServiceApi,
        private router: Router,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        this.rleId = +this.route.snapshot.paramMap.get('elementId');
        this.userId = +this.route.snapshot.paramMap.get('userId');
        this.ownRle = this.isViewingCurrentUser(this.userId);

        this.comments = [];
        this.lists = [];

        this.serviceApi.getReadingListElement(this.userId, this.rleId).subscribe(rle => {
            this.lso.updateReadingListElement(rle);
            this.readingListElement = rle;

            // Now get the tags.
            for (let tagId of this.readingListElement.tagIds) {
                if (this.lso.getTags()[tagId] == null) {
                    this.serviceApi.getTag(tagId).subscribe(tag => {
                        this.lso.updateTag(tag);
                    });
                }
            }

            // Get all the comments
            for (let commentId of this.readingListElement.commentIds) {
                this.serviceApi.getComment(this.userId, this.rleId, commentId).subscribe(comment => {
                    this.comments.push(comment);
                });
            }

            // Get all the lists that we're a part of
            for (let listId of this.readingListElement.listIds) {
                if (this.lso.getReadingLists()[listId] != null) {
                    this.lists.push(this.lso.getReadingLists()[listId])
                }
                else {
                    this.serviceApi.getReadingList(this.userId, listId).subscribe(list => {
                        this.lists.push(list);
                    });
                }
            }
        });
    }

    private onAddRleToList(): void {
        var rleIds = [];
        rleIds.push(this.rleId);

        this.serviceApi.addReadingListElementToReadingList(
            this.userId, +this.addRleToListId, rleIds).subscribe(() => {
                this.lists.push(this.lso.getReadingLists()[+this.addRleToListId]);
                this.addRleToListId = "added";
            });
    }

    private onRemoveRleFromList(list: ReadingListEntity): void {
        this.serviceApi.removeReadingListElementFromReadingList(
            this.userId, list.id, this.rleId).subscribe(removedListId => {
                var index = this.lists.indexOf(removedListId, 0);
                this.lists.splice(index, 1);
            });
    }

    private onDeleteReadingListElement(): void {
        // TODO: Should verify the user meant to do this... ;)
        this.serviceApi.deleteReadingListElement(this.userId, this.rleId).subscribe(() => {
            this.lso.deleteReadingListElement(this.rleId);
            for (let commentId of this.readingListElement.commentIds) {
                this.lso.deleteComment(commentId);
            }
            for (let listId of this.readingListElement.listIds) {
                var arr = this.lso.getReadingLists()[listId].readingListElementIds;
                var index = arr.indexOf(this.rleId, 0);
                arr.splice(index, 1);
            }

            this.router.navigate(['users', this.userId]);
        });
    }

    private onAddTag(): void {
        if (this.addTagName != undefined) {
            // First check if the tag exists.
            this.serviceApi.getTagByName(this.addTagName).subscribe(tag => {
                if (tag == null) {
                    var tagEntity = new TagEntity();
                    tagEntity.tagName = this.addTagName;
                    this.serviceApi.postTag(tagEntity).subscribe(tagId => {
                        tagEntity.id = tagId;
                        this.lso.updateTag(tagEntity);

                        let tagIds: number[] = [tagId];
                        this.serviceApi.addTagToReadingListElement(this.userId, this.rleId, tagIds).subscribe(x => {
                            this.readingListElement.tagIds.push(tagEntity.id);
                            this.lso.updateReadingListElement(this.readingListElement);
                        });
                    });
                }
                else {
                    var tagEntity = new TagEntity();
                    tagEntity.tagName = this.addTagName;
                    let tagIds: number[] = [tag.id];

                    // Make sure our tag isn't already added.
                    if (this.readingListElement.tagIds.indexOf(tag.id) != -1) {
                        return;
                    }
                    this.serviceApi.addTagToReadingListElement(this.userId, this.rleId, tagIds).subscribe(x => {
                        this.readingListElement.tagIds.push(tagEntity.id);
                        this.lso.updateReadingListElement(this.readingListElement);
                    });
                }
            });
        }
    }
    private onRemoveTag(tag: TagEntity): void {
        this.serviceApi.removeTagFromReadingListElement(this.userId, this.rleId, tag.id).subscribe(x => {
            var index = this.readingListElement.tagIds.indexOf(tag.id, 0);
            this.readingListElement.tagIds.splice(index, 1);
            this.lso.updateReadingListElement(this.readingListElement);
        })
    }
    private getTagStyle(tagId: number): string {
        return `tagcolor${tagId % this.numTagStyles}`
    }
    private onSelectTag(tag: TagEntity): void {
        this.router.navigate(['tags', tag.id]);
    }

    private onAddComment(): void {
        if (this.addComment != undefined) {
            var ce = new CommentEntity();
            ce.userId = this.userId;
            ce.readingListElementId = this.rleId;
            ce.commentText = this.addComment;
            this.serviceApi.postComment(ce).subscribe(comment => {
                ce.id = comment.id;
                this.comments.push(ce);
            });
        }
    }

    private isViewingCurrentUser(userId: number): boolean {
        var currentUser = this.lso.getUsers()[this.lso.getMyUserId()];
        if (currentUser == null) {
            return false;
        }
        var targetUser = this.lso.getUsers()[userId];
        if (targetUser == null) {
            return false;
        }
        return currentUser.userId == targetUser.userId;
    }
    private log(message: string) { this.logger.log(`[Users]: ${message}`); }
}
