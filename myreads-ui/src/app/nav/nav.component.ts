import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ServiceApi, UserEntity, ReadingListEntity, FollowedListEntity } from '../serviceapi.service';
import { LoggerService } from '../logger.service';
import { LocalStorageObjectService } from '../LocalStorageObject';

@Component({
    selector: 'app-nav',
    templateUrl: './nav.component.html',
    styleUrls: ['./nav.component.css']
})
export class NavComponent implements OnInit {
    public isVisible: boolean;

    // Display binding variables.
    public userEntity: UserEntity;
    public readingLists: ReadingListEntity[]; // The reading lists to present on this user.
    public followedLists: ReadingListEntity[]; // The followed lists to present on this user.

    constructor(
        private serviceApi: ServiceApi,
        private lso: LocalStorageObjectService,
        private router: Router,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        this.userEntity = this.lso.getUsers()[this.lso.getMyUserId()];
        this.readingLists = [];
        this.followedLists = [];

        // Check if the nav should be visible.
        this.isVisible = (this.lso.getMyLoginToken() != null);
        this.lso.change.subscribe(myLoginToken => {
            this.isVisible = (myLoginToken != null);
        });

        // Load up everything we know about the user.
        this.serviceApi.getReadingLists(this.userEntity.id).subscribe(readingLists => {
            if (readingLists == null) { return; }

            this.readingLists = readingLists.sort((a, b) => +(a.name > b.name));
            for (let list of readingLists) {
                this.lso.updateReadingList(list);
                this.lso.updateMyReadingLists(list.id);
            }
        });

        this.serviceApi.getFollowedLists(this.userEntity.id).subscribe(followedLists => {
            if (followedLists == null) { return; }

            for (let fl of followedLists) {
                this.lso.updateFollowedList(fl);
                this.lso.updateMyFollowedLists(fl.listId, fl.id)
                if (this.lso.getReadingLists()[fl.listId] == null) {
                    this.serviceApi.getReadingList(fl.ownerId, fl.listId).subscribe(readingList => {
                        this.log(`got list ${readingList.name} for followed user Id ${fl.ownerId}`);
                        this.lso.updateReadingList(readingList);

                        this.followedLists.push(readingList);
                        this.followedLists = this.followedLists.sort((a,b) => +(a.name > b.name));
                        if (this.lso.getUsers()[fl.ownerId] == null) {
                            this.serviceApi.getUser(fl.ownerId).subscribe(user => {
                                this.log(`got user ${user.name}`);
                                this.lso.updateUser(user);
                            })
                        }
                    });
                }
                else {
                    this.followedLists.push(this.lso.getReadingLists()[fl.listId]);
                    this.followedLists = this.followedLists.sort((a,b) => +(a.name > b.name));
                }
            }
        });
    }

    private onSelectReadingList(list: ReadingListEntity): void {
        // TODO: Why is it that clicking on one route works, but multiple stops until you
        // TODO: click on a route from a different component?
        this.router.navigate(['users', list.userId, 'readinglists', list.id]);
    }
    private log(message: string) { this.logger.log(`[Nav]: ${message}`); }
}
