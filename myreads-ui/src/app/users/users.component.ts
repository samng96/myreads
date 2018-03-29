import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/mergeMap';

import { ServiceApi, UserEntity, ReadingListEntity, FollowedListEntity } from '../serviceapi.service';
import { LoggerService } from '../logger.service';
import { LocalStorageObject } from '../localstorageobject';

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
    lso: LocalStorageObject;

    constructor(
        private route: ActivatedRoute,
        private serviceApi: ServiceApi,
        private router: Router,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        // When we load up, we need to get the user in the route.
        this.lso = LocalStorageObject.load();
        this.userId = +this.route.snapshot.paramMap.get('userId');

        var getUserMerge = this.serviceApi.getUser(this.userId);

        // TODO: Need to add error handling. Since we have some stuff stored locally,
        // TODO: we should be able to work in offline mode as needed.
        // Two paths here - one is to get the reading lists for the user, and the other is to get the followed lists.
        var getReadingListsMerge = getUserMerge.mergeMap(user =>
            {
                this.lso.updateUser(user);
                this.userEntity = user;
                this.canEdit = this.isViewingCurrentUser(this.lso.users[user.id]);
                this.log(`got user ${user.name}, editing permissions ${this.canEdit}`);

                // Get the user's reading lists.
                return this.serviceApi.getReadingLists(user.id);
            });
        getReadingListsMerge.subscribe(readingLists =>
            {
                this.log(`got ${readingLists.length} lists for user ${this.lso.users[this.userId].name}`);
                this.readingLists = readingLists.sort((a, b) => +(a.name > b.name));

                // Save them to local storage object for later.
                for (let list of readingLists) {
                    this.lso.updateReadingList(list);
                }
            });

        var getFollowedListsMerge = getUserMerge.mergeMap(user => this.serviceApi.getFollowedLists(user.id));
        var resolveFollowedListsMerge = getFollowedListsMerge.subscribe(followedLists =>
            {
                this.log(`got ${followedLists.length} followed lists for user ${this.lso.users[this.userId].name}`);
                this.followedLists = [];

                // For each followed list, we need to resolve the list, then resolve the user.
                for (let followedList of followedLists) {
                    this.lso.updateFollowedList(followedList);
                    if (this.lso.readingLists[followedList.id] == null) {
                        var getReadingListForFollowedListMerge = this.serviceApi.getReadingList(followedList.ownerId, followedList.listId);
                        getReadingListForFollowedListMerge.subscribe(readingList =>
                        {
                            this.log(`got list ${readingList.name} for followed user Id ${followedList.ownerId}`);
                            this.lso.updateReadingList(readingList);

                            this.followedLists.push(readingList);
                            this.followedLists = this.followedLists.sort((a,b) => +(a.name > b.name));
                            if (this.lso.users[followedList.ownerId] == null) {
                                this.serviceApi.getUser(followedList.ownerId).subscribe(user => {
                                    this.log(`got user ${user.name}`);
                                    this.lso.updateUser(user);
                                })
                            }
                        });
                    }
                    else {
                        this.followedLists.push(this.lso.readingLists[followedList.id]);
                        this.followedLists = this.followedLists.sort((a,b) => +(a.name > b.name));
                    }
                }
            });
    }

    private onSelectFollowedList(list: ReadingListEntity): void {
        this.router.navigate(['users', this.userId, 'followedlists', list.id]);
    }

    private onSelectReadingList(list: ReadingListEntity): void {
        this.router.navigate(['users', this.userId, 'readinglists', list.id]);
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
