import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { ServiceApi } from '../serviceapi.service';
import { LoggerService } from '../logger.service';
import { LocalStorageObject } from '../localstorageobject';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
    username: string;
    password: string;
    lso: LocalStorageObject;

    hardcodedUserId: number = 5732452450435072;

    constructor(
        private router: Router,
        private serviceApi: ServiceApi,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        this.lso = LocalStorageObject.load();
        this.checkLogin();
    }

    checkLogin(): void {
        // If we've logged in, we'll have a loginToken. Eventually, we'll
        // want to use that token with auth to our API, but for now we'll just
        // set it to 1 when we've logged in.
        var loginToken = this.lso.loginToken;
        this.log(`checkLogin got token ${loginToken}`);
        if (loginToken != null) {
            this.router.navigate(['users', this.lso.loggedInUserId]);
        }
    }

    login(): void {
        // For now we don't have a login API, so just assume it all works out and
        // hard code the login tokens, then get the user object.
        this.lso.setCurrentUserId(this.hardcodedUserId);
        this.lso.setCurrentLoginToken("1");

        this.serviceApi.getUser(this.hardcodedUserId).subscribe(user =>
            {
                this.lso.updateUser(user);
                this.log(`successful login for user ${user.name}`)
                this.checkLogin();
            });
    }

    private log(message: string) { this.logger.log(`[Login]: ${message}`); }
}
