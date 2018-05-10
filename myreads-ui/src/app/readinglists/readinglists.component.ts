import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { catchError, tap } from 'rxjs/operators';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';

import { ServiceApi } from '../serviceapi.service';
import { TagEntity, UserEntity, ReadingListEntity, FollowedListEntity, ReadingListElementEntity } from '../entities';
import { LoggerService } from '../logger.service';
import { LocalStorageObjectService } from '../localstorageobject';
import { ExtrasHelpers, ReadingListElementExtras, LinkPreviewResultObject } from '../entityextras';

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
    addRleLink: string; // Bound to the form.

    constructor(
        private http: HttpClient,
        private lso: LocalStorageObjectService,
        private helper: ExtrasHelpers,
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
                        }
                        resolve();

                        // Now asynchronously load up the link previews.
                        if (this.lso.getRleExtras()[rle.id] == null) {
                            this.helper.getLinkPreview(rle.link).subscribe(lp => {
                                if (lp != null) {
                                    var rlee = new ReadingListElementExtras();
                                    rlee.image = lp.image;
                                    rlee.title = lp.title;
                                    rlee.url = lp.url;
                                    rlee.description = lp.description;

                                    this.lso.updateRleExtras(rle.id, rlee);
                                }
                            });
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
                    if (this.lso.getTags()[tagId] == null) {
                        this.serviceApi.getTag(tagId).subscribe(tag => {
                            this.lso.updateTag(tag);
                        })
                    }
                }
            });

            // Now get all the lists that the user is following.
            this.serviceApi.getFollowedLists(this.lso.getMyUserId()).subscribe(fles => {
                for (let fle of fles) {
                    this.lso.updateFollowedList(fle);
                    this.lso.updateMyFollowedLists(fle.listId);
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
                this.lso.updateMyFollowedLists(fle.listId);
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
                        this.serviceApi.addTagToReadingList(this.userId, this.listId, tagIds).subscribe(() => {
                            this.readingList.tagIds.push(tagEntity.id);
                            this.lso.updateReadingList(this.readingList);
                        });
                    });
                }
                else {
                    var tagEntity = new TagEntity();
                    tagEntity.tagName = this.addTagName;
                    let tagIds: number[] = [tag.id];

                    // Make sure our tag isn't already added.
                    for (let currentTag of this.readingList.tagIds) {
                        if (currentTag == tag.id) {
                            return;
                        }
                    }
                    this.serviceApi.addTagToReadingList(this.userId, this.listId, tagIds).subscribe(x => {
                        this.readingList.tagIds.push(tag.id);
                        this.lso.updateReadingList(this.readingList);
                    });
                }
            });
        }
    }
    private onRemoveTag(tag: TagEntity): void {
        this.serviceApi.removeTagFromReadingList(this.userId, this.listId, tag.id).subscribe(x => {
            var index = this.readingList.tagIds.indexOf(tag.id, 0);
            this.readingList.tagIds.splice(index, 1);
            this.lso.updateReadingList(this.readingList);
        })
    }
    private onSelectTag(tag: TagEntity): void {
        this.router.navigate(['tags', tag.id]);
    }

    private onSelectReadingListElement(rle: ReadingListElementEntity): void {
        this.router.navigate(['users', rle.userId, 'readinglistelements', rle.id]);
    }
    private onAddRle(): void {
        this.helper.getLinkPreview(this.addRleLink).subscribe(lp => {
            var rle = new ReadingListElementEntity();
            var rlee = new ReadingListElementExtras();
            if (lp != null) {
                rlee.image = lp.image;
                rlee.title = lp.title;
                rlee.url = lp.url;
                rlee.description = lp.description;

                rle.name = lp.title;
                rle.description = lp.description;
                rle.listIds = [this.listId];
            }
            else {
                rle.name = this.addRleLink;
                rle.description = "empty description";
            }

            rle.userId = this.userId;
            rle.link = this.addRleLink;

            this.serviceApi.postReadingListElement(rle).subscribe(rleId => {
                if (lp != null) {
                    this.lso.updateRleExtras(rle.id, rlee);
                }

                rle.id = rleId;
                this.lso.updateReadingListElement(rle);

                var rleIds = [rleId];
                this.serviceApi.addReadingListElementToReadingList(this.userId, this.listId, rleIds).subscribe(() => {
                    this.readingList.readingListElementIds.push(this.listId);
                });
            });
        });
    }

    private onDeleteList(): void {
        this.serviceApi.deleteReadingList(this.lso.getMyUserId(), this.listId).subscribe(() => {
            this.lso.deleteReadingList(this.readingList);
            this.router.navigate(['/users', this.lso.getMyUserId()]);
        });
    }
    private onEditList(): void {
        // TODO: switch the title to put it in an input box for updating the title of the RL.
        // TODO: do reading lists need descriptions?
    }
    private onToggleView(): void {
        this.isGridView = !this.isGridView;
    }

    private isFollowingList(listId: number): boolean {
        return (this.lso.getMyFollowedLists().indexOf(listId) != -1);
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
    private handleError<T>(operation: string, result?:T) {
        return (error: any): Observable<T> => {
            this.log(`${operation} failed: ${error.message}`);
            return of(result as T);
        }
    }
}
