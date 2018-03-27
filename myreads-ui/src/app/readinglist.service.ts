import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';

import { MessageService } from './message.service';
import { Readinglist } from './readinglist';
import { READINGLISTS } from './mock-lists';

@Injectable()
export class ReadinglistService {

  constructor(private messageService: MessageService) { }

  getLists(): Observable<Readinglist[]> {
      this.messageService.add('ReadinglistService: fetched reading lists');
      return of(READINGLISTS);
  }
}
