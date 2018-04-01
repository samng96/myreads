import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/mergeMap';

import { ServiceApi, TagEntity, UserEntity, ReadingListEntity, FollowedListEntity, ReadingListElementEntity } from '../serviceapi.service';
import { LoggerService } from '../logger.service';
import { LocalStorageObject } from '../localstorageobject';

@Component({
    selector: 'app-readinglistelements',
    templateUrl: './readinglistelements.component.html',
    styleUrls: ['./readinglistelements.component.css']
})
export class ReadingListElementsComponent implements OnInit {
    lso: LocalStorageObject;

    rleId: number;
    userId: number;

    readingListElement: ReadingListElementEntity; // This is for the display.
    tags: TagEntity[]; // This is for the display.

    constructor(
        private route: ActivatedRoute,
        private serviceApi: ServiceApi,
        private router: Router,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        this.lso = LocalStorageObject.load();
        this.rleId = +this.route.snapshot.paramMap.get('elementId');
        this.userId = +this.route.snapshot.paramMap.get('userId');

        this.tags = [];

        this.serviceApi.getReadingListElement(this.userId, this.rleId).subscribe(rle => {
            this.lso.updateReadingListElement(rle);
            this.readingListElement = rle;

            // Now get the tags.
            for (let tagId of this.readingListElement.tagIds) {
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
        });
    }

    private onSelectTag(tag: TagEntity): void {
        this.router.navigate(['tags', tag.id]);
    }

    private isViewingCurrentUser(user: UserEntity): boolean {
        var currentUser = this.lso.users[this.lso.myUserId];
        if (currentUser == null) {
            return false;
        }
        return currentUser.userId == user.userId;
    }
    private log(message: string) { this.logger.log(`[Users]: ${message}`); }
}
