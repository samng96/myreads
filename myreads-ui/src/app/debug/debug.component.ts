import { Component, OnInit } from '@angular/core';
import { LoggerService } from '../utilities/logger.service';

@Component({
    selector: 'app-debug',
    template: `<div *ngIf="logger.messages.length">
        <h2>Debug</h2>
        <button class="clear"
            (click)="logger.clear()">clear</button>
        <div *ngFor='let message of logger.messages'> {{message}} </div>
    </div>`
})
export class DebugComponent implements OnInit {

    constructor(public logger: LoggerService) { }
    ngOnInit() {
    }
}
