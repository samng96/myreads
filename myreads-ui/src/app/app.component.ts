import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LocalStorageObject } from './localstorageobject';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    title = 'MyReads';
    lso: LocalStorageObject;

    constructor(
        private router: Router
    ) {}

    ngOnInit(): void {
        this.lso = LocalStorageObject.load();

        if (this.lso.myLoginToken == null) {
            this.router.navigate(['/login']);
        }
    }
}
