import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ServiceApi } from '../utilities/serviceapi.service';
import { UserEntity, ReadingListEntity, FollowedListEntity } from '../utilities/entities';
import { LoggerService } from '../utilities/logger.service';
import { LocalStorageObjectService } from '../utilities/localstorageobject';
import { ExtrasHelpers } from '../utilities/entityextras';

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
        private helper: ExtrasHelpers,
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

    private loadUser(): void {
        this.userEntity = this.lso.getUser(this.lso.getMyUserId());
        if (this.userEntity == null) {
            this.serviceApi.getUser(this.lso.getMyUserId()).subscribe(user => {
                this.userEntity = user;
                this.helper.loadUser(this.userEntity);
            });
        }
        else {
            this.helper.loadUser(this.userEntity);
        }
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
