import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';

import { ServiceApi } from '../../utilities/serviceapi.service';
import { TagEntity, UserEntity, ReadingListEntity, FollowedListEntity, ReadingListElementEntity } from '../../utilities/entities';
import { LoggerService } from '../../utilities/logger.service';
import { LocalStorageObjectService } from '../../utilities/localstorageobject';
import { ExtrasHelpers, ReadingListElementExtras, LinkPreviewResultObject } from '../../utilities/entityextras';

import { ListOfElementsComponent, ListOfElementsCommunicationObject } from '../../components/listofelements.component';

@Component({
    selector: 'app-readinglists',
    templateUrl: './readinglists.component.html',
})
export class ReadingListsComponent implements OnInit {
    userId: number; // This is the current user we're trying to view.
    listId: number; // This is the current list we're trying to view.

    ownList: boolean;
    followingList: boolean;

    isAddTag: boolean = false;
    addTagName: string; // Bound to the form.

    addRleLink: string; // Bound to the form.

    isEdit: boolean = false;
    isEditDescription: boolean = false;
    editListName: string;
    editListDescription: string;

    constructor(
        private lso: LocalStorageObjectService,
        private helper: ExtrasHelpers,
        private route: ActivatedRoute,
        private serviceApi: ServiceApi,
        private listOfElements: ListOfElementsCommunicationObject,
        private router: Router,
        private logger: LoggerService
    ) { }

    private getRl(): ReadingListEntity {
        return this.lso.getReadingList(this.listId);
    }

    ngOnInit() {
        this.route.params.subscribe(params => {
            this.onInitializeComponent();
        });
    }

    private onInitializeComponent(): void {
        // When we load up, we need to get the user and the list in the route.
        this.userId = +this.route.snapshot.paramMap.get('userId');
        this.listId = +this.route.snapshot.paramMap.get('listId');

        var tagIds = [];
        this.serviceApi.getReadingList(this.userId, this.listId).subscribe(readingList =>
        {
            if (readingList == null) { return; }

            this.ownList = this.helper.isViewingCurrentUser(readingList.userId);
            this.editListName = readingList.name;
            this.editListDescription = readingList.description;

            this.updateChildListOfElements();

            // Now load up all the RLEs for the list.
            var promises = [];
            for (let rleId of readingList.readingListElementIds) {
                var promise = new Promise(resolve => {
                    this.serviceApi.getReadingListElement(this.userId, rleId).subscribe(rle => {
                        if (rle == null) { return; }

                        // Ensure that every RLE's tags are loaded.
                        for (let tagId of rle.tagIds) {
                            tagIds.push(tagId);
                        }
                        resolve();

                        // Now asynchronously load up the link previews if we've never loaded them
                        // before. We check this by checking the description - if there is one, we must
                        // have loaded it from the link preview, so don't bother reloading. If there isn't
                        // one, try to load up the link preview and update it.
                        if (this.lso.getRleExtras()[rle.id] == null && rle.description == "") {
                            this.helper.getLinkPreview(rle.link).subscribe(lp => {
                                if (lp != null) {
                                    var rlee = new ReadingListElementExtras();
                                    rlee.image = lp.image;
                                    rlee.title = this.cleanTitle(lp.title);
                                    rlee.url = lp.url;
                                    rlee.description = lp.description;

                                    rle.name = lp.title;
                                    rle.description = lp.description;
                                    this.lso.updateRleExtras(rle.id, rlee);
                                    this.serviceApi.putReadingListElement(rle);
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
                        this.serviceApi.getTag(tagId);
                    }
                }
            });

            // Now get all the lists that the user is following.
            this.serviceApi.getFollowedLists(this.lso.getMyUserId()).subscribe(fles => {
                if (fles == null) { return; }

                this.followingList = this.isFollowingList(readingList.id);
            });
        });
    }

    private updateChildListOfElements(): void {
        this.listOfElements.displayById = true;
        this.listOfElements.listOfElementIds = this.getRl().readingListElementIds;
        this.listOfElements.listOfElements = null;
    }

    private onFollowList(): void {
        this.followingList = true;

        var fle = new FollowedListEntity();
        fle.userId = this.lso.getMyUserId();
        fle.ownerId = this.getRl().userId;
        fle.listId = this.getRl().id;
        this.serviceApi.postFollowedList(fle);
    }
    private onUnfollowList(): void {
        this.followingList = false;

        var fl = this.lso.getFollowedList(this.listId);
        this.serviceApi.deleteFollowedList(this.lso.getMyUserId(), fl.Id);
    }

    private onAddTag(): void {
        if (this.addTagName != undefined) {
            // First check if the tag exists.
            this.serviceApi.getTagByName(this.addTagName).subscribe(tag => {
                if (tag == null) {
                    var tagEntity = new TagEntity();
                    tagEntity.tagName = this.addTagName;
                    this.serviceApi.postTag(tagEntity).subscribe(tagId => {
                        if (tagId == -1) { return; }

                        let tagIds: number[] = [tagId];
                        this.serviceApi.addTagToReadingList(this.userId, this.listId, tagIds);
                    });
                }
                else {
                    let tagIds: number[] = [tag.id];

                    // Make sure our tag isn't already added.
                    if (this.getRl().tagIds.indexOf(tag.id) != -1) {
                        return;
                    }
                    this.serviceApi.addTagToReadingList(this.userId, this.listId, tagIds);
                }
            });
        }
    }
    private onRemoveTag(tag: TagEntity): void {
        this.serviceApi.removeTagFromReadingList(this.userId, this.listId, tag.id);
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
                rlee.title = this.cleanTitle(lp.title);
                rlee.url = lp.url;
                rlee.description = lp.description;

                rle.name = lp.title;
                rle.description = lp.description;
            }
            else {
                rle.name = this.addRleLink;
                rle.description = "";
            }

            rle.listIds = [this.listId];
            rle.userId = this.userId;
            rle.link = this.addRleLink;

            this.serviceApi.postReadingListElement(rle).subscribe(rleId => {
                if (lp != null) {
                    this.lso.updateRleExtras(rleId, rlee);
                }

                var rleIds = [rleId];
                this.serviceApi.addReadingListElementToReadingList(this.userId, this.listId, rleIds).subscribe(() => {
                    this.updateChildListOfElements();
                });
            });
        });
    }
    private onDeleteList(): void {
        this.serviceApi.deleteReadingList(this.lso.getMyUserId(), this.listId).subscribe(() => {
            this.router.navigate(['/users', this.lso.getMyUserId()]);
        });
    }
    private onEditList(): void {
        var rl = this.getRl();
        rl.name = this.editListName;
        rl.description = this.editListDescription;
        this.serviceApi.putReadingList(rl).subscribe(() => {
            this.isEdit = false;
        });
    }
    private onToggleEdit(): void {
        this.isEdit = !this.isEdit;
    }
    private onToggleAddTag(): void {
        this.isAddTag = !this.isAddTag;
    }
    private onTogglePublic(): void {
        var rl = this.getRl();
        rl.visible = !rl.visible;
        this.serviceApi.putReadingList(rl);
    }

    private cleanTitle(title: string): string {
        var index = title.indexOf("Amazon.com: ", 0);
        if (index == 0) {
            return title.slice(12);
        }
        else if (index != -1) {
            return title.slice(0, index);
        }
        return title;
    }
    private isFollowingList(listId: number): boolean {
        return (this.lso.getFollowedListsByUser(this.lso.getMyUserId()).indexOf(listId) != -1);
    }
    private log(message: string) { this.logger.log(`[Users]: ${message}`); }
}
