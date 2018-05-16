import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ServiceApi } from '../utilities/serviceapi.service';
import { UserEntity, ReadingListEntity, FollowedListEntity } from '../utilities/entities';
import { LoggerService } from '../utilities/logger.service';
import { LocalStorageObjectService } from '../utilities/localstorageobject';

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

        // Check if the nav should be visible.
        this.isVisible = this.lso.isLoggedIn();
        if (this.isVisible) {
            this.loadUser();
        }

        this.lso.changeLogin.subscribe(userId => {
            this.isVisible = this.lso.isLoggedIn();

            if (this.isVisible) {
                this.loadUser();
            }
        });
    }

    private loadUser() {
        // Load up everything we know about the user.
        this.userEntity = this.lso.getUsers()[this.lso.getMyUserId()];
        this.serviceApi.getReadingLists(this.userEntity.id);

        this.serviceApi.getFollowedLists(this.userEntity.id).subscribe(followedLists => {
            if (followedLists == null) { return; }

            for (let fl of followedLists) {
                if (this.lso.getReadingLists()[fl.listId] == null) {
                    this.serviceApi.getReadingList(fl.ownerId, fl.listId).subscribe(readingList => {
                        if (this.lso.getUsers()[fl.ownerId] == null) {
                            this.serviceApi.getUser(fl.ownerId);
                        }
                    });
                }
            }
        });
    }

    private goTo(url: string): void {
        this.router.navigate([url]);
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

        this.addListName = null;
        this.serviceApi.postReadingList(rl);
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
