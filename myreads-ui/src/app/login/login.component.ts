import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { ServiceApi } from '../serviceapi.service';
import { LoggerService } from '../logger.service';
import { LocalStorageObjectService } from '../LocalStorageObject';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit {
    username: string;
    password: string;

    hardcodedUserId: number = 5732452450435072;

    constructor(
        private lso: LocalStorageObjectService,
        private router: Router,
        private serviceApi: ServiceApi,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        this.checkLogin();
    }

    checkLogin(): void {
        // If we've logged in, we'll have a myLoginToken. Eventually, we'll
        // want to use that token with auth to our API, but for now we'll just
        // set it to 1 when we've logged in.
        var myLoginToken = this.lso.getMyLoginToken();
        this.log(`checkLogin got token ${myLoginToken}`);
        if (myLoginToken != null) {
            this.router.navigate(['users', this.lso.getMyUserId()]);
        }
    }

    login(): void {
        this.serviceApi.getUser(this.hardcodedUserId).subscribe(user =>
            {
                this.lso.updateUser(user);

                // For now we don't have a login API, so just assume it all works out and
                // hard code the login tokens, then get the user object.
                this.lso.setMyUserId(this.hardcodedUserId);
                this.lso.setMyLoginToken("1");

                this.log(`successful login for user ${user.name}`)
                this.checkLogin();
            });
    }

    private log(message: string) { this.logger.log(`[Login]: ${message}`); }
}
