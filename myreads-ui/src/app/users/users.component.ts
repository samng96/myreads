import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/mergeMap';

import { ServiceApi, UserEntity, ReadingListEntity, FollowedListEntity } from '../serviceapi.service';
import { LoggerService } from '../logger.service';
import { LocalStorageObjectService } from '../LocalStorageObject';

@Component({
    selector: 'app-users',
    templateUrl: './users.component.html',
    styleUrls: ['./users.component.css']
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
            this.lso.updateUser(user);
            this.userEntity = user;
            this.canEdit = this.isViewingCurrentUser(user.id);
            this.log(`got user ${user.name}, editing permissions ${this.canEdit}`);

            this.serviceApi.getReadingLists(this.userEntity.id).subscribe(readingLists => {
                if (readingLists == null) { return; }

                this.log(`got ${readingLists.length} lists for user ${this.userEntity.name}`);
                this.readingLists = readingLists.sort((a, b) => +(a.name > b.name));
                for (let list of readingLists) {
                    this.lso.updateReadingList(list);
                }
            });

            this.serviceApi.getFollowedLists(this.userEntity.id).subscribe(followedLists => {
                if (followedLists == null) { return; }

                this.log(`got ${followedLists.length} followed lists for user ${this.userEntity.name}`);
                for (let fl of followedLists) {
                    this.lso.updateFollowedList(fl);
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
