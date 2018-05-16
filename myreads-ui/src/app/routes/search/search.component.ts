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

    constructor(
        private lso: LocalStorageObjectService,
        private route: ActivatedRoute,
        private serviceApi: ServiceApi,
        private listOfElements: ListOfElementsCommunicationObject,
        private router: Router,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        this.route.params.subscribe(params => {
            this.onInitializeComponent();
        });
    }

    private onInitializeComponent(): void {
        // First clear out the comms object.
        this.listOfElements.listOfElements = null;
        this.listOfElements.listOfElementIds = null;

        var path = this.route.snapshot.routeConfig.path;
        if (path == "unread") {
            this.searchTitle = "Displaying unread items";
            this.loadUnreadElements();
        }
        else if (path == "favorites") {
            this.searchTitle = "Displaying favorite items";
            this.loadFavoriteElements();
        }
    }

    private loadUnreadElements(): void {
        this.serviceApi.getReadingListElements(this.lso.getMyUserId(), "unread=true").subscribe(rles => {
            this.listOfElements.displayById = false;
            this.listOfElements.listOfElements = rles;
        });
    }

    private loadFavoriteElements(): void {
        this.serviceApi.getReadingListElements(this.lso.getMyUserId(), "favorite=true").subscribe(rles => {
            this.listOfElements.displayById = false;
            this.listOfElements.listOfElements = rles;
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
