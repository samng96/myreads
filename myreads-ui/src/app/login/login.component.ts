import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

    id = 1;

    constructor(private location: Location) { }

    ngOnInit() {
        const url = '/users/' + this.id;

        //if ($cookies.get('loggedIn') == true) {
        window.location = url;
        //}
    }
}
