import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/mergeMap';

import { ServiceApi } from '../serviceapi.service';
import { TagEntity, UserEntity, ReadingListEntity, FollowedListEntity, ReadingListElementEntity } from '../entities';
import { LoggerService } from '../logger.service';
import { LocalStorageObjectService } from '../LocalStorageObject';

@Component({
    selector: 'app-readinglists',
    templateUrl: './readinglists.component.html',
    styleUrls: ['./readinglists.component.css']
})
export class ReadingListsComponent implements OnInit {
    userId: number; // This is the current user we're trying to view.
    listId: number; // This is the current list we're trying to view.

    ownList: boolean;
    followingList: boolean;
    isGridView: boolean = true;
    addTagName: string; // Bound to the form.
    readingList: ReadingListEntity; // This is for the display.
    readingListElements: ReadingListElementEntity[]; // This is for the display.
    tags: TagEntity[]; // This is for the display.

    constructor(
        private lso: LocalStorageObjectService,
        private route: ActivatedRoute,
        private serviceApi: ServiceApi,
        private router: Router,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        this.route.params.subscribe(params => {
            this.onInitializeComponent();
        });
    }

    private onInitializeComponent(): void {
        // When we load up, we need to get the user and the list in the route.
        this.userId = +this.route.snapshot.paramMap.get('userId');
        this.listId = +this.route.snapshot.paramMap.get('listId');

        this.readingListElements = [];
        this.tags = [];

        var tagIds = [];
        this.serviceApi.getReadingList(this.userId, this.listId).subscribe(readingList =>
        {
            this.lso.updateReadingList(readingList);
            this.ownList = this.isViewingCurrentUser(readingList.userId);
            this.readingList = readingList;

            // Now load up all the RLEs for the list.
            var promises = [];
            for (let rleId of readingList.readingListElementIds) {
                var promise = new Promise(resolve => {
                    this.serviceApi.getReadingListElement(this.userId, rleId).subscribe(rle => {
                        this.lso.updateReadingListElement(rle);
                        this.readingListElements.push(rle);

                        // Ensure that every RLE's tags are loaded.
                        for (let tagId of rle.tagIds) {
                            tagIds.push(tagId);
                            resolve();
                        }
                    });
                });
                promises.push(promise);
            }

            // Now get the tags.
            Promise.all(promises).then(() => {
                for (let tagId of readingList.tagIds) {
                    tagIds.push(tagId);
                }
                for (let tagId of tagIds) {
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
            });

            // Now get all the lists that the user is following.
            this.serviceApi.getFollowedLists(this.lso.getMyUserId()).subscribe(fles => {
                for (let fle of fles) {
                    this.lso.updateFollowedList(fle);
                    this.lso.updateMyFollowedLists(fle.listId, fle.id);
                }
                this.followingList = this.isFollowingList(readingList.id);
            });
        });
    }

    private onFollowList(): void {
        var fle = new FollowedListEntity();
        fle.userId = this.lso.getMyUserId();
        fle.ownerId = this.readingList.userId;
        fle.listId = this.readingList.id;
        this.serviceApi.postFollowedList(fle).subscribe(fleId => {
            if (fleId != null) {
                fle.id = fleId;
                this.lso.updateFollowedList(fle);
                this.lso.updateMyFollowedLists(fle.listId, fle.id);
                this.followingList = true;
            }
        });
    }
    private onUnfollowList(): void {
        var fleId = this.lso.getMyFollowedLists()[this.listId];
        this.serviceApi.deleteFollowedList(this.lso.getMyUserId(), fleId).subscribe(x => {
            this.lso.deleteMyFollowedList(this.listId);
            this.followingList = false;
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
                        this.serviceApi.addTagToReadingList(this.userId, this.listId, tagIds).subscribe(x => {
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
                    this.serviceApi.addTagToReadingList(this.userId, this.listId, tagIds).subscribe(x => {
                        this.tags.push(tagEntity);
                    });
                }
            });
        }
    }
    private onRemoveTag(tag: TagEntity): void {
        this.serviceApi.removeTagFromReadingList(this.userId, this.listId, tag.id).subscribe(x => {
            var index = this.tags.indexOf(tag, 0);
            this.tags.splice(index, 1);
        })
    }

    private onSelectTag(tag: TagEntity): void {
        this.router.navigate(['tags', tag.id]);
    }
    private onSelectReadingListElement(rle: ReadingListElementEntity): void {
        this.router.navigate(['users', rle.userId, 'readinglistelements', rle.id]);
    }

    private onDeleteList(): void {
        this.serviceApi.deleteReadingList(this.lso.getMyUserId(), this.listId).subscribe(() => {
            this.lso.deleteReadingList(this.readingList);
            this.router.navigate(['/users', this.lso.getMyUserId()]);
        });
    }
    private onEditList(): void {

    }
    private onToggleView(): void {
        this.isGridView = !this.isGridView;
    }

    private isFollowingList(listId: number): boolean {
        return (this.lso.getMyFollowedLists()[listId] != undefined);
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
