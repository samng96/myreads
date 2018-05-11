import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ServiceApi } from '../serviceapi.service';
import { UserEntity, ReadingListEntity, FollowedListEntity } from '../entities';
import { LocalStorageObjectService } from '../localstorageobject';

@Component({
    selector: 'app-toolbar',
    templateUrl: './toolbar.component.html',
})
export class ToolbarComponent implements OnInit {
    public isVisible: boolean;

    // Display binding variables.

    constructor(
        private serviceApi: ServiceApi,
        private lso: LocalStorageObjectService,
        private router: Router,
    ) { }

    ngOnInit() {
        this.isVisible = (this.lso.getMyLoginToken() != null);
    }
}
