import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/mergeMap';

import { ServiceApi } from '../../utilities/serviceapi.service';
import { UserEntity, ReadingListEntity, FollowedListEntity } from '../../utilities/entities';
import { LoggerService } from '../../utilities/logger.service';
import { LocalStorageObjectService } from '../../utilities/localstorageobject';

@Component({
    selector: 'app-users',
    templateUrl: './users.component.html',
})
export class UsersComponent implements OnInit {
    userId: number; // This is the current user we're trying to view.
    userEntity: UserEntity; // This is for presentation.
    readingLists: ReadingListEntity[]; // The reading lists to present on this user.
    followedLists: ReadingListEntity[]; // The followed lists to present on this user.
    canEdit: boolean;

    constructor(
        private lso: LocalStorageObjectService,
        private route: ActivatedRoute,
        private serviceApi: ServiceApi,
        private router: Router,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        // When we load up, we need to get the user in the route.
        this.userId = +this.route.snapshot.paramMap.get('userId');
        this.followedLists = [];
        this.readingLists = [];

        this.serviceApi.getUser(this.userId).subscribe(user => {
            this.userEntity = user;
            this.canEdit = this.isViewingCurrentUser(user.id);

            this.serviceApi.getReadingLists(this.userEntity.id).subscribe(readingLists => {
                if (readingLists == null) { return; }

                this.readingLists = readingLists.sort((a, b) => +(a.name > b.name));
            });

            this.serviceApi.getFollowedLists(this.userEntity.id).subscribe(followedLists => {
                if (followedLists == null) { return; }

                for (let fl of followedLists) {
                    if (this.lso.getReadingLists()[fl.listId] == null) {
                        this.serviceApi.getReadingList(fl.ownerId, fl.listId).subscribe(readingList => {
                            this.followedLists.push(readingList);
                            this.followedLists = this.followedLists.sort((a,b) => +(a.name > b.name));
                            if (this.lso.getUsers()[fl.ownerId] == null) {
                                this.serviceApi.getUser(fl.ownerId);
                            }
                        });
                    }
                    else {
                        this.followedLists.push(this.lso.getReadingLists()[fl.listId]);
                        this.followedLists = this.followedLists.sort((a,b) => +(a.name > b.name));
                    }
                }
            });
        });
    }

    private onSelectReadingList(list: ReadingListEntity): void {
        this.router.navigate(['users', list.userId, 'readinglists', list.id]);
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
