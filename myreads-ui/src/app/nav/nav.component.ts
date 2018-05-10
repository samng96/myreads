import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ServiceApi } from '../serviceapi.service';
import { UserEntity, ReadingListEntity, FollowedListEntity } from '../entities';
import { LoggerService } from '../logger.service';
import { LocalStorageObjectService } from '../localstorageobject';

@Component({
    selector: 'app-nav',
    templateUrl: './nav.component.html',
})
export class NavComponent implements OnInit {
    public isVisible: boolean;

    // Display binding variables.
    public userEntity: UserEntity;
    public toggleRls: boolean;
    public toggleFls: boolean;
    public toggleAddList: boolean;
    public addListName: string;

    constructor(
        private serviceApi: ServiceApi,
        private lso: LocalStorageObjectService,
        private router: Router,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        this.toggleRls = true;
        this.toggleFls = true;
        this.toggleAddList = false;

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
    }

    private loadUser() {
        // Load up everything we know about the user.
        this.userEntity = this.lso.getUsers()[this.lso.getMyUserId()];
        this.serviceApi.getReadingLists(this.userEntity.id).subscribe(readingLists => {
            if (readingLists == null) { return; }

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

    private onToggleAddList(): void {
        this.toggleAddList = !this.toggleAddList;
    }
    private onAddList(): void {
        if (this.addListName == null || this.addListName == "") {
            return;
        }

        var rl = new ReadingListEntity();
        rl.userId = this.lso.getMyUserId();
        rl.name = this.addListName;
        rl.description = "empty description";
        rl.tagIds = [];
        rl.readingListElementIds = [];
        this.serviceApi.postReadingList(rl).subscribe(addedListId => {
            rl.id = addedListId;
            this.lso.updateReadingList(rl);

            this.addListName = null;
        });
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
