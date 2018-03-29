import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs/Observable';

import { LoggerService } from './logger.service';

export class UserEntity {
    id: number;
    email: string;
    name: string;
    userId: string;
}

export class ReadingListEntity {
    id: number;
    userId: number;
    name: string;
    description: string;
    tagIds: number[];
    readingListElementIds: number[];
}

export class FollowedListEntity {
    id: number;
    ownerId: number;
    listId: number;
    userId: number;
    orphaned: boolean;
}

@Injectable()
export class ServiceApi {
    public static baseUrl = "http://localhost:8080"

    constructor(
        private http: HttpClient,
        private logger: LoggerService
    ) { }

    getUser(userId: number): Observable<UserEntity> {
        var url = `${ServiceApi.baseUrl}/users/${userId}`;
        return this.http.get<UserEntity>(url)
            .pipe(
                tap(_ => this.log(`Api: getUser(${userId})`))
            );
    }

    getReadingLists(userId: number): Observable<ReadingListEntity[]> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists`;
        return this.http.get<ReadingListEntity[]>(url)
            .pipe(
                tap(_ => this.log(`Api: getReadingLists(${userId})`))
            );
    }

    getReadingList(userId: number, listId: number): Observable<ReadingListEntity> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists/${listId}`;
        return this.http.get<ReadingListEntity>(url)
            .pipe(
                tap(_ => this.log(`Api: getReadingList(${userId}, ${listId})`))
            );
    }

    getFollowedLists(userId: number): Observable<FollowedListEntity[]> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/followedLists`;
        return this.http.get<FollowedListEntity[]>(url)
            .pipe(
                tap(_ => this.log(`Api: getFollowedLists(${userId})`))
            );
    }

    private log(message: string) { this.logger.log(`[ServiceApi]: ${message}`); }
}
