import { Component, OnInit } from '@angular/core';
import { LoggerService } from '../logger.service';

@Component({
    selector: 'app-addlistelement',
    templateUrl: './addlistelement.component.html',
})
export class AddListElementComponent implements OnInit {
    constructor(public logger: LoggerService) { }

    ngOnInit() {
    }
}
