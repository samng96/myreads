import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/mergeMap';

import { ServiceApi, UserEntity, ReadingListEntity, FollowedListEntity } from '../serviceapi.service';
import { LoggerService } from '../logger.service';

@Component({
    selector: 'app-users',
    templateUrl: './users.component.html',
    styleUrls: ['./users.component.css']
})
export class UsersComponent implements OnInit {
    userId: number;
    userEntity: UserEntity;
    readingLists: ReadingListEntity[];
    followedLists: ReadingListEntity[];
    usersCollection: Map<number, UserEntity>;
    canEdit: boolean;

    constructor(
        private route: ActivatedRoute,
        private serviceApi: ServiceApi,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        // When we load up, we need to get the user in the route.
        this.usersCollection = JSON.parse(localStorage.getItem("usersCollection"));
        if (this.usersCollection == null) {
            this.usersCollection = new Map<number, UserEntity>();
        }
        this.userId = +this.route.snapshot.paramMap.get('userId');

        var getUserMerge = this.serviceApi.getUser(this.userId);

        // Two paths here - one is to get the reading lists for the user, and the other is to get the followed lists.
        var getReadingListsMerge = getUserMerge.mergeMap(user =>
            {
                this.userEntity = user;
                this.canEdit = this.isViewingCurrentUser(user);
                this.log(`got user ${user.name}, editing permissions ${this.canEdit}`);

                // Get the user's reading lists.
                return this.serviceApi.getReadingLists(user.id);
            });
        getReadingListsMerge.subscribe(readingLists =>
            {
                this.log(`got ${readingLists.length} lists for user ${this.userEntity.name}`);
                this.readingLists = readingLists.sort((a, b) => +(a.name > b.name));
            });

        var getFollowedListsMerge = getUserMerge.mergeMap(user => this.serviceApi.getFollowedLists(user.id));
        var resolveFollowedListsMerge = getFollowedListsMerge.mergeMap(followedLists =>
            {
                this.log(`got ${followedLists.length} followed lists for user ${user.name}`);
                this.followedLists = [];

                // For each followed list, we need to resolve the list, then resolve the user.
                for (let followedList of followedLists) {
                    var getReadingListForFollowedListMerge = this.serviceApi.getReadingList(followedList.ownerId, followedList.listId);
                    getReadingListForFollowedListMerge.subscribe(readingList =>
                    {
                        this.log(`got list ${readingList.name} for followed user Id ${followedList.ownerId}`);
                        this.followedLists.push(readingList);
                        this.followedLists = this.followedLists.sort((a,b) => +(a.name > b.name));

                        // Resolve the user, then add it to the cache.
                        if (this.usersCollection[followedList.ownerId] == null) {
                            this.serviceApi.getUser(followedList.ownerId).subscribe(user => {
                                this.log(`got user ${user.name}`);
                                this.usersCollection[user.id] = user;
                                localStorage.setItem("usersCollection", JSON.stringify(this.usersCollection));
                            })
                        }
                    });
                }
            });
    }

    private isViewingCurrentUser(user: UserEntity): boolean {
        var currentUser = JSON.parse(localStorage.getItem("loggedInUserEntity"));
        if (currentUser == null) {
            return false;
        }
        return currentUser.userId == user.userId;
    }
    private log(message: string) { this.logger.log(`[Users]: ${message}`); }
}
