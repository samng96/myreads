import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/mergeMap';

import { ServiceApi } from '../../utilities/serviceapi.service';
import { UserEntity, ReadingListEntity, FollowedListEntity } from '../../utilities/entities';
import { LoggerService } from '../../utilities/logger.service';
import { LocalStorageObjectService } from '../../utilities/localstorageobject';
import { ExtrasHelpers } from '../../utilities/entityextras';

@Component({
    selector: 'app-users',
    templateUrl: './users.component.html',
})
export class UsersComponent implements OnInit {
    userId: number; // This is the current user we're trying to view.
    ownUser: boolean;

    constructor(
        private lso: LocalStorageObjectService,
        private route: ActivatedRoute,
        private serviceApi: ServiceApi,
        private helper: ExtrasHelpers,
        private router: Router,
        private logger: LoggerService
    ) { }
    private getUser(): UserEntity {
        return this.lso.getUser(this.userId);
    }

    ngOnInit() {
        // When we load up, we need to get the user in the route.
        this.userId = +this.route.snapshot.paramMap.get('userId');
        this.ownUser = this.helper.isViewingCurrentUser(this.userId);

        if (this.getUser() == null) {
            this.serviceApi.getUser(this.userId).subscribe(user => this.helper.loadUser(user));
        }
        else {
            this.helper.loadUser(this.getUser());
        }
    }
    private getReadingListsForCurrentUser(): ReadingListEntity[] {
        var rls = this.lso.getReadingListsByUser(this.userId).sort(
            (a,b) => this.lso.getReadingList(a).name < this.lso.getReadingList(b).name ? -1 : this.lso.getReadingList(a).name > this.lso.getReadingList(b));

        return rls;
    }
    private getFollowedListsForCurrentUser(): ReadingListEntity[] {
        return null;
    }

    private onSelectReadingList(list: ReadingListEntity): void {
        this.router.navigate(['users', list.userId, 'readinglists', list.id]);
    }
    private log(message: string) { this.logger.log(`[Users]: ${message}`); }
}
