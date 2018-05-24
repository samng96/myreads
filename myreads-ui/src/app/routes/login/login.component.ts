import { jQuery } from 'jquery';
import { Component, OnInit } from '@angular/core';
import { NgZone } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

import { UserEntity } from '../../utilities/entities';
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
    profile: any;

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
        this.profile = googleUser.getBasicProfile();

        this.serviceApi.getUserByAuthToken(this.profile.getId()).subscribe(user => {
            if (user == null) {
                // No user here, move us to sign up.
                jQuery("#signupModal").modal("show");
            }
            else {
                this.setLoggedIn(user.id);
            }
        });
    };

    public onSignUp() {
        var user = new UserEntity();
        user.email = this.profile.getEmail();
        user.name = this.profile.getGivenName();
        user.userId = this.profile.getName();
        user.externalId = this.profile.getId();
        this.serviceApi.postUser(user).subscribe(userId => {
            this.setLoggedIn(userId);
        });
    }

    private setLoggedIn(userId: number) {
        this.lso.setMyLoginInfo(
            this.profile.getId(),
            userId,
            this.profile.getImageUrl(),
            this.profile.getGivenName()
        );

        this.log(`successful login for user ${this.profile.getGivenName()}`)
        this.checkLogin();
    }

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
