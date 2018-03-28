import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { ServiceApi } from '../serviceapi.service';
import { LoggerService } from '../logger.service';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
    username: string;
    password: string;

    hardcodedUserId: number = 5732452450435072;

    constructor(
        private router: Router,
        private serviceApi: ServiceApi,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        this.checkLogin();
    }

    checkLogin(): void {
        // If we've logged in, we'll have a loginToken. Eventually, we'll
        // want to use that token with auth to our API, but for now we'll just
        // set it to 1 when we've logged in.
        var loginToken = localStorage.getItem('loginToken');
        this.log(`checkLogin got token ${loginToken}`);
        if (loginToken != null) {
            this.router.navigate(['users', localStorage.getItem('userId')]);
        }
    }

    login(): void {
        // For now we don't have a login API, so just assume it all works out and
        // hard code the login tokens, then get the user object.
        localStorage.setItem('userId', this.hardcodedUserId.toString());
        localStorage.setItem('loginToken', "1");

        this.serviceApi.getUser(this.hardcodedUserId).subscribe(user =>
            {
                localStorage.setItem('userEntity', JSON.stringify(user));
                this.log(`successful login for user ${user.name}`)
                this.checkLogin();
            });
    }

    private log(message: string) { this.logger.log(message); }
}
