import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { ServiceApi } from '../serviceapi.component';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
    username: string;
    password: string;

    constructor(private location: Location) { }

    ngOnInit() {
        this.checkLogin();
    }

    checkLogin(): void {
        // If we've logged in, we'll have a loginToken. Eventually, we'll
        // want to use that token with auth to our API, but for now we'll just
        // set it to 1 when we've logged in.
        var loginToken = sessionStorage.getItem('loginToken');
        if (loginToken != null) {
            const url = '/users/' + sessionStorage.getItem('userId');
            window.location = url;
        }
    }

    login(): void {
        // For now we don't have a login API, so just assume it all works out and
        // hard code the login tokens.
        sessionStorage.setItem('userId', 96);
        sessionStorage.setItem('loginToken', 1);

        this.checkLogin();
    }
}
