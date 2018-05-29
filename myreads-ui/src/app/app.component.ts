import { Component} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LocalStorageObjectService } from './utilities/localstorageobject';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    isLoggedIn: boolean;

    constructor(
        private lso: LocalStorageObjectService,
        private route: ActivatedRoute,
        private router: Router
    ) {}

    ngOnInit(): void {
        var path = window.location.pathname;
        if (!this.lso.isLoggedIn() && !(path == "/login")) {
            this.router.navigate(['/login']);
        }

        this.router.onSameUrlNavigation = "reload";
    }
}
