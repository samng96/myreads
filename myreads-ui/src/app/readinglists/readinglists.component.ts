import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/mergeMap';

import { ServiceApi, UserEntity, ReadingListEntity, FollowedListEntity, ReadingListElementEntity } from '../serviceapi.service';
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

    readingList: ReadingListEntity; // This is for the display.
    readingListElements: ReadingListElementEntity[]; // This is for the display.

    constructor(
        private route: ActivatedRoute,
        private serviceApi: ServiceApi,
        private router: Router,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        // When we load up, we need to get the user and the list in the route.
        this.lso = LocalStorageObject.load();
        this.userId = +this.route.snapshot.paramMap.get('userId');
        this.listId = +this.route.snapshot.paramMap.get('listId');

        this.readingListElements = [];

        this.serviceApi.getReadingList(this.userId, this.listId).subscribe(readingList =>
        {
            this.lso.updateReadingList(readingList);
            this.readingList = readingList;

            // Now load up all the RLEs for the list.
            for (let rleId of this.lso.readingLists[this.listId].readingListElementIds) {
                this.serviceApi.getReadingListElement(this.userId, rleId).subscribe(rle => {
                    this.lso.updateReadingListElement(rle);
                    this.readingListElements.push(rle);
                });
            }
        });
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
