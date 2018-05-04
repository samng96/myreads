import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { catchError, tap } from 'rxjs/operators';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';

import { ServiceApi } from '../serviceapi.service';
import { TagEntity, UserEntity, ReadingListEntity, FollowedListEntity, ReadingListElementEntity } from '../entities';
import { LoggerService } from '../logger.service';
import { LocalStorageObjectService, ReadingListElementExtras } from '../LocalStorageObject';

class LinkPreviewResultObject {
    title: string;
    description: string;
    image: string;
    url: string;
    error: number;
}

@Component({
    selector: 'app-readinglists',
    templateUrl: './readinglists.component.html',
    styleUrls: ['./readinglists.component.css']
})
export class ReadingListsComponent implements OnInit {
    userId: number; // This is the current user we're trying to view.
    listId: number; // This is the current list we're trying to view.

    private linkPreviewApiKey: string = "5aeaa317b64a2ae9950b87ffc3b372739ad468bb2a676";
    private maxTitleLength: number = 75;
    private maxDescriptionLength: number = 150;
    private numTagStyles: number = 6;

    ownList: boolean;
    followingList: boolean;
    isGridView: boolean = true;
    addTagName: string; // Bound to the form.
    readingList: ReadingListEntity; // This is for the display.
    readingListElements: ReadingListElementEntity[]; // This is for the display.

    constructor(
        private http: HttpClient,
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

                        // Now asynchronously load up the link previews.
                        if (this.lso.getRleExtras()[rle.id] == null) {
                            this.getRleExtra(rle).subscribe(lp => {
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
    private getTagStyle(tagId: number): string {
        return `tagcolor${tagId % this.numTagStyles}`
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
        // TODO:
    }
    private onToggleView(): void {
        this.isGridView = !this.isGridView;
    }

    private getRleExtra(rle: ReadingListElementEntity): Observable<LinkPreviewResultObject> {
        // TODO: Looks like this API throttles - figure out how we can delay load.
        var url = `http://api.linkpreview.net/?key=${this.linkPreviewApiKey}&q=${rle.link}`
        return this.http.get<LinkPreviewResultObject>(url)
            .pipe(
                tap(_ => this.log(`linkPreview(${rle.link})`)),
                catchError(this.handleError("linkPreview", null))
            );
    }
    private pickDescription(rle: ReadingListElementEntity): string {
        var desc;
        if (this.lso.getRleExtras()[rle.id] != null) {
            desc = this.lso.getRleExtras()[rle.id].description;
        }
        else {
            desc = rle.description;
        }
        if (desc.length > this.maxDescriptionLength) {
            return `${desc.substring(0, this.maxDescriptionLength)} ...`;
        }
        return desc;
    }
    private pickTitle(rle: ReadingListElementEntity): string {
        if (this.lso.getRleExtras()[rle.id] != null) {
            var title = this.lso.getRleExtras()[rle.id].title;
            if (title.length > this.maxTitleLength) {
                return `${title.substring(0, this.maxTitleLength)} ...`;
            }
            return title;
        }
        return rle.name;
    }
    private getImageUrl(rle: ReadingListElementEntity): string {
        if (this.extractRootDomain(rle.link) == "amazon.com") {
            var productId = this.extractAmazonProductId(rle.link);

            if (productId != null) {
                // TODO: Get a larger amazon image
                return `http://ws-na.amazon-adsystem.com/widgets/q?ASIN=${productId}&ServiceVersion=20070822&ID=AsinImage&WS=1&Format`;
            }
        }
        if (this.lso.getRleExtras()[rle.id] != null) {
            return this.lso.getRleExtras()[rle.id].image;
        }
        return "";
    }
    private getLink(rle: ReadingListElementEntity): string {
        if (this.extractRootDomain(rle.link) == "amazon.com") {
            var productId = this.extractAmazonProductId(rle.link);

            if (productId != null) {
                return `https://www.amazon.com/gp/product/${productId}/ref=as_li_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=0451495861&linkCode=as2&tag=samng96-20&linkId=0e3bf9c7ea3f23b726971dc8cfd7ba8d`;
            }
        }
        return rle.link;
    }
    private extractHostname(url: string): string {
        var hostname;
        //find & remove protocol (http, ftp, etc.) and get hostname

        if (url.indexOf("://") > -1) {
            hostname = url.split('/')[2];
        }
        else {
            hostname = url.split('/')[0];
        }

        //find & remove port number
        hostname = hostname.split(':')[0];
        //find & remove "?"
        hostname = hostname.split('?')[0];

        return hostname;
    }
    private extractRootDomain(url: string): string {
        var domain = this.extractHostname(url);
        var splitArr = domain.split('.');
        var arrLen = splitArr.length;

        //extracting the root domain here
        //if there is a subdomain
        if (arrLen > 2) {
            domain = splitArr[arrLen - 2] + '.' + splitArr[arrLen - 1];
            //check to see if it's using a Country Code Top Level Domain (ccTLD) (i.e. ".me.uk")
            if (splitArr[arrLen - 2].length == 2 && splitArr[arrLen - 1].length == 2) {
                //this is using a ccTLD
                domain = splitArr[arrLen - 3] + '.' + domain;
            }
        }
        return domain;
    }
    private extractAmazonProductId(url: string): string {
        return url.toLowerCase().split("dp/")[1].split("/")[0];
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
