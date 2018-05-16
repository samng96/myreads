import { Component, OnInit } from '@angular/core';
import { NgZone } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

import { ServiceApi } from '../../utilities/serviceapi.service';
import { LoggerService } from '../../utilities/logger.service';
import { LocalStorageObjectService } from '../../utilities/localstorageobject';

declare var gapi: any;

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit {
    username: string;
    password: string;

    hardcodedUserId: number = 5732452450435072;

    constructor(
        private ngZone: NgZone,
        private lso: LocalStorageObjectService,
        private router: Router,
        private route: ActivatedRoute,
        private serviceApi: ServiceApi,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        this.checkLogin();
    }

    private checkLogin(): void {
        if (this.lso.isLoggedIn()) {
            this.log(`checkLogin is logged in`);

            this.ngZone.run(() => {
                this.router.navigate(['users', this.lso.getMyUserId()]);
            });
        }
    }

    public onSignIn(googleUser) {
        var profile = googleUser.getBasicProfile();

        // TODO: We need to make a call to the API to get the user associated with
        // TODO: the currently logged in user instead of getting the hard coded user.
        this.serviceApi.getUser(this.hardcodedUserId).subscribe(user =>
            {
                this.lso.setMyLoginInfo(
                    "1",
                    this.hardcodedUserId,
                    profile.getImageUrl(),
                    profile.getGivenName()
                );

                this.log(`successful login for user ${user.name}`)
                this.checkLogin();
            });
    };

    ngAfterViewInit() {
        gapi.signin2.render('my-signin2', {
            'scope': 'profile email',
            'width': 150,
            'height': 50,
            'longtitle': false,
            'theme': 'light',
            'onsuccess': param => this.onSignIn(param)
        });
    }

    private log(message: string) { this.logger.log(`[Login]: ${message}`); }
}
