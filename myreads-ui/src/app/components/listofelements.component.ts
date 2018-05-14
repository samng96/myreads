import { Component, Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { TagEntity, ReadingListElementEntity } from '../utilities/entities';
import { LocalStorageObjectService } from '../utilities/localstorageobject';
import { ExtrasHelpers } from '../utilities/entityextras';

@Injectable()
export class ListOfElementsCommunicationObject {
    public listOfElementIds: number[];
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
        private helper: ExtrasHelpers,
        private router: Router
    ) { }

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
