import { jQuery } from 'jquery';
import { Component, OnInit } from '@angular/core';
import { NgZone } from '@angular/core';
import { Router } from '@angular/router';

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
    profile: any;
    isSignup: boolean = false;

    constructor(
        private ngZone: NgZone,
        private lso: LocalStorageObjectService,
        private router: Router,
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
                this.log(`user ${this.profile.getId()} not found`)
                this.doSignUp();
            }
            else {
                this.log(`found user ${user.id}, logged in`)
                this.setLoggedIn(user.id);
            }
        });
    };

    private doSignUp() {
        var user = new UserEntity();
        user.email = this.profile.getEmail();
        user.name = this.profile.getGivenName();
        user.userId = this.profile.getName();
        user.externalId = this.profile.getId();
        this.serviceApi.postUser(user).subscribe(userId => {
            this.setLoggedIn(userId);
        });
    }

    public onSignUp() {
        this.isSignup = true;
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
            'width': 200,
            'height': 40,
            'longtitle': true,
            'theme': 'light',
            'onsuccess': param => this.onSignIn(param)
        });
    }

    private log(message: string) { this.logger.log(`[Login]: ${message}`); }
}
