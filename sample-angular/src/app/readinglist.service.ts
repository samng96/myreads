import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';

import { MessageService } from './message.service';
import { Readinglist } from './readinglist';
import { READINGLISTS } from './mock-lists';

@Injectable()
export class ReadinglistService {
    private readinglistUrl = 'api/readingList';

    constructor(
        private http: HttpClient,
        private messageService: MessageService) { }

    getLists(): Observable<Readinglist[]> {
        this.messageService.add('ReadinglistService: fetched reading lists');
        return of(READINGLISTS);
    }

    updateReadingList(list: Readinglist): Observable<any> {
        // TODO: update the reading list.
        return of(1);
    }

    private log(message: string) {
        this.messageService.add('Readinglist: ' + message);
    }

    private handleError<T> (operation = 'operation', result?: T) {
        return (error: any): Observable<T> => {
            // TODO: send the error to remote logging infrastructure
            console.error(error); // log to console instead

            // TODO: better job of transforming error for user consumption
            this.log(`${operation} failed: ${error.message}`);

            // Let the app keep running by returning an empty result.
            return of(result as T);
        };
    }
}
