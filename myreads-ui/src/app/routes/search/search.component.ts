import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';

import { ServiceApi } from '../../utilities/serviceapi.service';
import { TagEntity, UserEntity, ReadingListEntity, FollowedListEntity, ReadingListElementEntity } from '../../utilities/entities';
import { LoggerService } from '../../utilities/logger.service';
import { LocalStorageObjectService } from '../../utilities/localstorageobject';

import { ListOfElementsComponent, ListOfElementsCommunicationObject } from '../../components/listofelements.component';

@Component({
    selector: 'app-search',
    templateUrl: './search.component.html',
})
export class SearchComponent implements OnInit {
    public searchTitle: string;
    public isDisplayingList: boolean;

    public searchTerm: string;
    public isDisplayingSearch: boolean;
    public isSearchDone: boolean;
    public users: UserEntity[];

    constructor(
        private lso: LocalStorageObjectService,
        private route: ActivatedRoute,
        private serviceApi: ServiceApi,
        private listOfElements: ListOfElementsCommunicationObject,
        private router: Router,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        this.route.params.subscribe(() => {
            this.onInitializeComponent();
        });
    }

    private onInitializeComponent(): void {
        // First clear out the comms object.
        this.listOfElements.listOfElements = null;
        this.listOfElements.listOfElementIds = null;
        this.isDisplayingList = false;
        this.isSearchDone = false;
        this.users = null;

        var path = this.route.snapshot.routeConfig.path;
        if (path == "unread") {
            this.searchTitle = "Displaying unread items";
            this.loadUnreadElements();
        }
        else if (path == "favorites") {
            this.searchTitle = "Displaying favorite items";
            this.loadFavoriteElements();
        }
        else if (path == "search") {
            this.searchTitle = "Search";
            this.isDisplayingSearch = true;

            this.route.queryParams.subscribe(params => {
                this.searchTerm = params["searchTerm"];
                this.performSearch(this.searchTerm.toLowerCase());
            });
        }
    }

    private onSelectUser(user: UserEntity): void {
        this.router.navigate(['users', user.id]);
    }

    private performSearch(searchTerm: string) {
        // Right now, all we search through is users, so load up all the users
        // and see if any names match the search term.
        this.serviceApi.getUsers().subscribe(users => {
            this.users = [];
            for (let user of users) {
                if (user.name.toLowerCase().includes(searchTerm) || user.userId.includes(searchTerm)) {
                    this.users.push(user);
                }
            }
            this.isSearchDone = true;
        });
    }

    private loadUnreadElements(): void {
        this.serviceApi.getReadingListElements(this.lso.getMyUserId(), "unread=true").subscribe(rles => {
            if (rles == null) { return; }

            this.listOfElements.displayById = false;
            this.listOfElements.listOfElements = rles;

            this.isDisplayingList = true;
        });
    }

    private loadFavoriteElements(): void {
        this.serviceApi.getReadingListElements(this.lso.getMyUserId(), "favorite=true").subscribe(rles => {
            if (rles == null) { return; }

            this.listOfElements.displayById = false;
            this.listOfElements.listOfElements = rles;

            this.isDisplayingList = true;
        });
    }

    private log(message: string) { this.logger.log(`[Users]: ${message}`); }
    private handleError<T>(operation: string, result?:T) {
        return (error: any): Observable<T> => {
            this.log(`${operation} failed: ${error.message}`);
            return of(result as T);
        }
    }
}
