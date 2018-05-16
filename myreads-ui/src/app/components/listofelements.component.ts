import { Component, Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { ServiceApi } from '../utilities/serviceapi.service';
import { TagEntity, ReadingListElementEntity } from '../utilities/entities';
import { LocalStorageObjectService } from '../utilities/localstorageobject';
import { ExtrasHelpers } from '../utilities/entityextras';

@Injectable()
export class ListOfElementsCommunicationObject {
    public displayById: boolean;
    public listOfElementIds: number[];
    public listOfElements: ReadingListElementEntity[];
}

@Component({
    selector: 'app-listofelements',
    templateUrl: './listofelements.component.html',
    styleUrls: ['./listofelements.component.css']
})
export class ListOfElementsComponent {
    public isGridView: boolean = false;

    constructor(
        private lso: LocalStorageObjectService,
        private elements: ListOfElementsCommunicationObject,
        private serviceApi: ServiceApi,
        private helper: ExtrasHelpers,
        private router: Router
    ) { }

    private listToDisplay(): ReadingListElementEntity[] {
        if (this.elements.displayById && (this.elements.listOfElementIds != null)) {
            var rles = [];
            this.elements.listOfElementIds.forEach(rleId => rles.push(this.lso.getReadingListElement(rleId)));
            return rles;
        }
        if (!this.elements.displayById && (this.elements.listOfElements != null)) {
            return this.elements.listOfElements;
        }

        return null;
    }
    private isDisplaying(): boolean {
        if (this.elements.displayById && (this.elements.listOfElementIds != null)) {
            return true;
        }
        if (!this.elements.displayById && (this.elements.listOfElements != null)) {
            return true;
        }

        return false;
    }
    private ownRle(rle: ReadingListElementEntity): boolean {
        return rle != null && rle.userId == this.lso.getMyUserId();
    }
    private onToggleFavorite(rle: ReadingListElementEntity): void {
        rle.favorite = !rle.favorite;
        this.serviceApi.putReadingListElement(rle);
    }
    private onToggleView(): void {
        this.isGridView = !this.isGridView;
    }
    private onSelectTag(tag: TagEntity): void {
        this.router.navigate(['tags', tag.id]);
    }
    private onSelectReadingListElement(rle: ReadingListElementEntity): void {
        this.router.navigate(['users', rle.userId, 'readinglistelements', rle.id]);
    }
}
