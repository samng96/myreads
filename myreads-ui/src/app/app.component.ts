import { Component} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LocalStorageObjectService } from './utilities/localstorageobject';

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
        if (!this.lso.isLoggedIn()) {
            this.router.navigate(['/login']);
        }

        this.router.onSameUrlNavigation = "reload";
    }
}
