import { Component, OnInit, Input } from '@angular/core';
import { NgZone } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ServiceApi } from '../utilities/serviceapi.service';
import { UserEntity, ReadingListEntity, FollowedListEntity } from '../utilities/entities';
import { LocalStorageObjectService } from '../utilities/localstorageobject';

declare var gapi: any;
@Component({
    selector: 'app-toolbar',
    templateUrl: './toolbar.component.html',
})
export class ToolbarComponent implements OnInit {
    public isVisible: boolean;

    constructor(
        private ngZone: NgZone,
        private serviceApi: ServiceApi,
        private lso: LocalStorageObjectService,
        private router: Router
    ) { }

    ngOnInit() {
        this.isVisible = this.lso.isLoggedIn();

        this.lso.changeLogin.subscribe(loginToken => {
            this.isVisible = this.lso.isLoggedIn();
        });
    }

    onSignOut(): void {
        var auth2 = gapi.auth2.getAuthInstance();
        auth2.signOut().then(() => {
            this.lso.setLoggedOut();
            this.ngZone.run(() => {
                this.router.navigate(['login']);
            });
        });
    }
}
