import { Component, OnInit } from '@angular/core';
import { LoggerService } from '../logger.service';

@Component({
  selector: 'app-debug',
  templateUrl: './debug.component.html',
})
export class DebugComponent implements OnInit {

  constructor(public logger: LoggerService) { }

  ngOnInit() {
  }

}
