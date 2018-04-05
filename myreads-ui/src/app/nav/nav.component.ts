import { Component, OnInit, Input } from '@angular/core';

import { ServiceApi } from '../serviceapi.service';
import { LoggerService } from '../logger.service';
import { LocalStorageObjectService } from '../LocalStorageObject';

@Component({
    selector: 'app-nav',
    templateUrl: './nav.component.html',
    styleUrls: ['./nav.component.css']
})
export class NavComponent implements OnInit {
    @Input()
    public isVisible: boolean;

    constructor(
        private serviceApi: ServiceApi,
        private lso: LocalStorageObjectService,
        private logger: LoggerService
    ) { }

    ngOnInit() {
        this.lso.change.subscribe(myLoginToken => {
            this.isVisible = (myLoginToken != null);
        });
    }

    private log(message: string) { this.logger.log(`[Nav]: ${message}`); }
}
