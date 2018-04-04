import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/mergeMap';

import { ServiceApi, TagEntity, UserEntity, ReadingListEntity, FollowedListEntity, ReadingListElementEntity } from '../serviceapi.service';
import { LoggerService } from '../logger.service';
import { LocalStorageObject } from '../localstorageobject';

@Component({
    selector: 'app-readinglists',
    templateUrl: './readinglists.component.html',
    styleUrls: ['./readinglists.component.css']
})
export class ReadingListsComponent implements OnInit {
    lso: LocalStorageObject;

    userId: number; // This is the current user we're trying to view.
    listId: number; // This is the current list we're trying to view.

    ownList: boolean;
    followingList: boolean;
    addTagName: string; // Bound to the form.
    readingList: ReadingListEntity; // This is for the display.
    readingListElements: ReadingListElementEntity[]; // This is for the display.
    tags: TagEntity[]; // This is for the display.

    constructor(
        private route: ActivatedRoute,
        private serviceApi: ServiceApi,
        private router: Router,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        // TODO: Every page needs to ensure that we're logged in.
        // When we load up, we need to get the user and the list in the route.
        this.lso = LocalStorageObject.load();
        this.userId = +this.route.snapshot.paramMap.get('userId');
        this.listId = +this.route.snapshot.paramMap.get('listId');

        this.readingListElements = [];
        this.tags = [];

        this.serviceApi.getReadingList(this.userId, this.listId).subscribe(readingList =>
        {
            this.lso.updateReadingList(readingList);
            this.ownList = this.isViewingCurrentUser(readingList.userId);
            this.readingList = readingList;

            // Now load up all the RLEs for the list.
            for (let rleId of readingList.readingListElementIds) {
                this.serviceApi.getReadingListElement(this.userId, rleId).subscribe(rle => {
                    this.lso.updateReadingListElement(rle);
                    this.readingListElements.push(rle);
                });
            }

            // Now get the tags.
            for (let tagId of readingList.tagIds) {
                if (this.lso.tags[tagId] != null) {
                    this.tags.push(this.lso.tags[tagId]);
                }
                else {
                    this.serviceApi.getTag(tagId).subscribe(tag => {
                        this.lso.updateTag(tag);
                        this.tags.push(tag);
                    })
                }
            }

            // Now get all the lists that the user is following.
            this.serviceApi.getFollowedLists(this.lso.myUserId).subscribe(fles => {
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
        fle.userId = this.lso.myUserId;
        fle.ownerId = this.readingList.userId;
        fle.listId = this.readingList.id;
        this.serviceApi.postFollowedList(this.lso.myUserId, fle).subscribe(fleId => {
            if (fleId != null) {
                fle.id = fleId;
                this.lso.updateFollowedList(fle);
                this.lso.updateMyFollowedLists(fle.listId, fle.id);
                this.followingList = true;
            }
        });
    }
    private onUnfollowList(): void {
        var fleId = this.lso.myFollowedLists[this.listId];
        this.serviceApi.deleteFollowedList(this.lso.myUserId, fleId).subscribe(x => {
            this.followingList = false;
        });
    }

    private onAddTag(): void {
        if (this.addTagName != undefined) {
            // First check if the tag exists.
            this.serviceApi.getTagByName(this.addTagName).subscribe(tag => {
                if (tag == null) {
                    // TODO: Add a tag, and then add it to the list
                    var tagEntity = new TagEntity();
                    tagEntity.tagName = this.addTagName;
                    this.serviceApi.postTag(tagEntity).subscribe(tagId => {
                        tagEntity.id = tagId;
                        this.lso.updateTag(tagEntity);

                        // TODO: Might want to fix - this takes an array, but really doesn't need to?
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
    private isFollowingList(listId: number): boolean {
        return (this.lso.myFollowedLists[listId] != undefined);
    }
    private isViewingCurrentUser(userId: number): boolean {
        var currentUser = this.lso.users[this.lso.myUserId];
        if (currentUser == null) {
            return false;
        }
        var targetUser = this.lso.users[userId];
        if (targetUser == null) {
            return false;
        }
        return currentUser.userId == targetUser.userId;
    }
    private log(message: string) { this.logger.log(`[Users]: ${message}`); }
}
