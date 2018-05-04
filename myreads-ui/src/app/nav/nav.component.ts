import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ServiceApi } from '../serviceapi.service';
import { UserEntity, ReadingListEntity, FollowedListEntity } from '../entities';
import { LoggerService } from '../logger.service';
import { LocalStorageObjectService } from '../LocalStorageObject';

@Component({
    selector: 'app-nav',
    templateUrl: './nav.component.html',
})
export class NavComponent implements OnInit {
    public isVisible: boolean;

    // Display binding variables.
    public userEntity: UserEntity;
    public readingLists: ReadingListEntity[]; // The reading lists to present on this user.
    public toggleRls: boolean;
    public toggleFls: boolean;

    constructor(
        private serviceApi: ServiceApi,
        private lso: LocalStorageObjectService,
        private router: Router,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        // TODO: Need to move this to lso so that we get updates.
        this.readingLists = [];
        this.toggleRls = true;
        this.toggleFls = true;

        // Check if the nav should be visible.
        this.isVisible = (this.lso.getMyLoginToken() != null);
        if (this.isVisible) {
            this.loadUser();
        }

        this.lso.changeLogin.subscribe(myLoginToken => {
            this.isVisible = (myLoginToken != null);

            if (this.isVisible) {
                this.loadUser();
            }
        });

        this.lso.changeListDelete.subscribe(list => {
            for (let rl of this.readingLists) {
                if (rl.id == list.id) {
                    this.readingLists.splice(this.readingLists.indexOf(rl, 0), 1);
                }
            }
        });
        this.lso.changeListAdd.subscribe(list => {
            this.readingLists.push(list);
        });
    }

    private loadUser() {
        // Load up everything we know about the user.
        this.userEntity = this.lso.getUsers()[this.lso.getMyUserId()];
        this.serviceApi.getReadingLists(this.userEntity.id).subscribe(readingLists => {
            if (readingLists == null) { return; }

            this.readingLists = readingLists.sort((a, b) => +(a.name > b.name));
            for (let list of readingLists) {
                this.lso.updateReadingList(list);
            }
        });

        this.serviceApi.getFollowedLists(this.userEntity.id).subscribe(followedLists => {
            if (followedLists == null) { return; }

            for (let fl of followedLists) {
                this.lso.updateFollowedList(fl);
                this.lso.updateMyFollowedLists(fl.listId);
                if (this.lso.getReadingLists()[fl.listId] == null) {
                    this.serviceApi.getReadingList(fl.ownerId, fl.listId).subscribe(readingList => {
                        this.log(`got list ${readingList.name} for followed user Id ${fl.ownerId}`);
                        this.lso.updateReadingList(readingList);

                        if (this.lso.getUsers()[fl.ownerId] == null) {
                            this.serviceApi.getUser(fl.ownerId).subscribe(user => {
                                this.log(`got user ${user.name}`);
                                this.lso.updateUser(user);
                            })
                        }
                    });
                }
            }
        });
    }

    private onAddList(): void {
        this.router.navigate(['addlist']);
    }
    private onAddRLE(): void {
        this.router.navigate(['addlistelement']);
    }
    private onToggleRls(): void {
        this.toggleRls = !this.toggleRls;
    }
    private onToggleFls(): void {
        this.toggleFls = !this.toggleFls;
    }
    private onSelectReadingList(list: ReadingListEntity): void {
        this.router.navigate(['users', list.userId, 'readinglists', list.id]);
    }
    private log(message: string) { this.logger.log(`[Nav]: ${message}`); }
}
