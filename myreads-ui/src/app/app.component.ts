import { Component} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LocalStorageObjectService } from './localstorageobject';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    title = 'MyReads';
    isLoggedIn: boolean;

    constructor(
        private lso: LocalStorageObjectService,
        private router: Router
    ) {}

    ngOnInit(): void {
        this.isLoggedIn = (this.lso.getMyLoginToken() != null);
        if (!this.isLoggedIn) {
            this.router.navigate(['/login']);
        }

        this.router.onSameUrlNavigation = "reload";
    }
}
