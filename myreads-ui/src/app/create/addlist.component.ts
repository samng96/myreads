import { Component, OnInit } from '@angular/core';
import { LoggerService } from '../logger.service';

@Component({
  selector: 'app-addlist',
  templateUrl: './addlist.component.html',
})
export class AddListComponent implements OnInit {

  constructor(public logger: LoggerService) { }

  ngOnInit() {
  }

}
