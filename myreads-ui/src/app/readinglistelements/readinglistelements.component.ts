import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/mergeMap';

import { ServiceApi, TagEntity, UserEntity, ReadingListEntity, CommentEntity, FollowedListEntity, ReadingListElementEntity } from '../serviceapi.service';
import { LoggerService } from '../logger.service';
import { LocalStorageObjectService } from '../LocalStorageObject';

@Component({
    selector: 'app-readinglistelements',
    templateUrl: './readinglistelements.component.html',
    styleUrls: ['./readinglistelements.component.css']
})
export class ReadingListElementsComponent implements OnInit {
    rleId: number;
    userId: number;

    ownRle: boolean;
    addTagName: string; // Bound to the form.
    addComment: string; // Bound to the form.
    readingListElement: ReadingListElementEntity; // This is for the display.
    comments: CommentEntity[]; // This is for the display.
    tags: TagEntity[]; // This is for the display.

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

        this.tags = [];
        this.comments = [];

        this.serviceApi.getReadingListElement(this.userId, this.rleId).subscribe(rle => {
            this.lso.updateReadingListElement(rle);
            this.readingListElement = rle;

            // Now get the tags.
            for (let tagId of this.readingListElement.tagIds) {
                if (this.lso.getTags()[tagId] != null) {
                    this.tags.push(this.lso.getTags()[tagId]);
                }
                else {
                    this.serviceApi.getTag(tagId).subscribe(tag => {
                        this.lso.updateTag(tag);
                        this.tags.push(tag);
                    })
                }
            }

            for (let commentId of this.readingListElement.commentIds) {
                this.serviceApi.getComment(this.userId, this.rleId, commentId).subscribe(comment => {
                    this.comments.push(comment);
                });
            }
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
                            this.tags.push(tagEntity);
                        });
                    });
                }
                else {
                    var tagEntity = new TagEntity();
                    tagEntity.tagName = this.addTagName;
                    let tagIds: number[] = [tag.id];

                    // Make sure our tag isn't already added.
                    for (let currentTag of this.tags) {
                        if (currentTag.tagName == this.addTagName) {
                            return;
                        }
                    }
                    this.serviceApi.addTagToReadingListElement(this.userId, this.rleId, tagIds).subscribe(x => {
                        this.tags.push(tagEntity);
                    });
                }
            });
        }
    }
    private onRemoveTag(tag: TagEntity): void {
        this.serviceApi.removeTagFromReadingListElement(this.userId, this.rleId, tag.id).subscribe(x => {
            var index = this.tags.indexOf(tag, 0);
            this.tags.splice(index, 1);
        })
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
