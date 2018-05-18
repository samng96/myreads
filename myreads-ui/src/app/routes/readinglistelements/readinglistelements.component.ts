import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/mergeMap';

import { ServiceApi } from '../../utilities/serviceapi.service';
import { TagEntity, UserEntity, ReadingListEntity, CommentEntity, FollowedListEntity, ReadingListElementEntity } from '../../utilities/entities';
import { LoggerService } from '../../utilities/logger.service';
import { LocalStorageObjectService } from '../../utilities/localstorageobject';
import { ExtrasHelpers, ReadingListElementExtras, LinkPreviewResultObject } from '../../utilities/entityextras';

@Component({
    selector: 'app-readinglistelements',
    templateUrl: './readinglistelements.component.html',
})
export class ReadingListElementsComponent implements OnInit {
    rleId: number;
    userId: number;

    ownRle: boolean;
    isAddTag: boolean = false;
    addTagName: string; // Bound to the form.
    addComment: string; // Bound to the form.
    selectedRlidForAdd: number;
    comments: CommentEntity[]; // This is for the display.

    constructor(
        private lso: LocalStorageObjectService,
        private route: ActivatedRoute,
        private helper: ExtrasHelpers,
        private serviceApi: ServiceApi,
        private router: Router,
        private logger: LoggerService
    ) { }

    private getRle(): ReadingListElementEntity {
        return this.lso.getReadingListElement(this.rleId);
    }

    ngOnInit() {
        this.rleId = +this.route.snapshot.paramMap.get('elementId');
        this.userId = +this.route.snapshot.paramMap.get('userId');
        this.ownRle = this.helper.isViewingCurrentUser(this.userId);

        this.comments = [];

        this.serviceApi.getReadingListElement(this.userId, this.rleId).subscribe(rle => {
            if (rle == null) { return; }

            // Now get the tags.
            for (let tagId of this.getRle().tagIds) {
                if (this.lso.getTags()[tagId] == null) {
                    this.serviceApi.getTag(tagId);
                }
            }

            // Get all the comments
            for (let commentId of this.getRle().commentIds) {
                this.serviceApi.getComment(this.userId, this.rleId, commentId).subscribe(comment => {
                    if (comment == null) { return; }

                    this.comments.push(comment);
                });
            }

            // Get all the lists that we're a part of
            for (let listId of this.getRle().listIds) {
                if (this.lso.getReadingLists()[listId] == null) {
                    this.serviceApi.getReadingList(this.userId, listId);
                }
            }
        });
    }

    private onToggleFavorite(): void {
        this.getRle().favorite = !this.getRle().favorite;
        this.serviceApi.putReadingListElement(this.getRle());
    }
    private onToggleIsRead(): void {
        this.getRle().read = !this.getRle().read;
        this.serviceApi.putReadingListElement(this.getRle());
    }
    private onSelectChange(value): void {
        this.selectedRlidForAdd = value.target.selectedOptions[0].value;
    }
    private onAddRleToList(): void {
        var rleIds = [];
        rleIds.push(this.rleId);

        this.serviceApi.addReadingListElementToReadingList(this.userId, this.selectedRlidForAdd, rleIds);
    }
    private onToggleAddTag(): void {
        this.isAddTag = !this.isAddTag;
    }

    private onRemoveRleFromList(listId: number): void {
        this.serviceApi.removeReadingListElementFromReadingList(this.userId, listId, this.rleId);
    }
    private onDeleteReadingListElement(): void {
        this.serviceApi.deleteReadingListElement(this.userId, this.rleId).subscribe(() => {
            this.router.navigate(['users', this.userId]);
        });
    }
    private onDeleteComment(comment: CommentEntity): void {
        this.serviceApi.deleteComment(comment.userId, comment.readingListElementId, comment.id).subscribe(() => {
            var index = this.comments.indexOf(comment, 0);
            this.comments.splice(index, 1);
            this.lso.deleteComment(comment.id);
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
                        let tagIds: number[] = [tagId];
                        this.serviceApi.addTagToReadingListElement(this.userId, this.rleId, tagIds);
                    });
                }
                else {
                    let tagIds: number[] = [tag.id];

                    // Make sure our tag isn't already added.
                    if (this.getRle().tagIds.indexOf(tag.id) != -1) {
                        return;
                    }
                    this.serviceApi.addTagToReadingListElement(this.userId, this.rleId, tagIds);
                }
            });
        }
    }
    private onRemoveTag(tag: TagEntity): void {
        this.serviceApi.removeTagFromReadingListElement(this.userId, this.rleId, tag.id);
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
            ce.lastModified = new Date();
            this.serviceApi.postComment(ce).subscribe(commentId => {
                if (commentId == null) { return; }

                ce.id = commentId;
                this.comments.push(ce);
            });
        }
    }
    private onSelectList(list: ReadingListEntity): void {
        this.router.navigate(['users', list.userId, 'readinglists', list.id]);
    }

    private getDate(dateInMilli: number): string {
        var options = { year: "numeric", month: "long", day: "numeric", hour: "numeric", minute: "numeric" }
        return new Date(dateInMilli).toLocaleDateString('en-US', options);
    }
    private getListsThatRleIsNotIn(): ReadingListEntity[] {
        var lists = [];
        for (let rlId of this.lso.getReadingListsByUser(this.lso.getMyUserId())) {
            var rl = this.lso.getReadingList(rlId);
            if (rl.readingListElementIds.indexOf(this.rleId, 0) == -1) {
                lists.push(rl);
            }
        }
        return lists;
    }
    private log(message: string) { this.logger.log(`[Users]: ${message}`); }
}
