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

export class ReadingListElementEntity {
    id: number;
    userId: number;
    listIds: number[];
    name: string;
    description: string;
    amazonLink: string;
    tagIds: number[];
    commentIds: number[];
}

export class TagEntity {
    id: number;
    tagName: string;
}

export class CommentEntity {
    id: number;
    userId: number;
    readingListElementId: number;
    commentText: string;
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

    getReadingListElement(userId: number, rleId: number): Observable<ReadingListElementEntity> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElements/${rleId}`;
        return this.http.get<ReadingListElementEntity>(url)
            .pipe(
                tap(_ => this.log(`Api: getReadingListElement(${userId}, ${rleId})`))
            );
    }

    getFollowedLists(userId: number): Observable<FollowedListEntity[]> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/followedLists`;
        return this.http.get<FollowedListEntity[]>(url)
            .pipe(
                tap(_ => this.log(`Api: getFollowedLists(${userId})`))
            );
    }

    getTags(): Observable<TagEntity[]> {
        var url = `${ServiceApi.baseUrl}/tags`;
        return this.http.get<TagEntity[]>(url)
            .pipe(
                tap(_ => this.log(`Api: getTags()`))
            );
    }

    getTag(tagId: number): Observable<TagEntity> {
        var url = `${ServiceApi.baseUrl}/tags/${tagId}`;
        return this.http.get<TagEntity>(url)
            .pipe(
                tap(_ => this.log(`Api: getTags(${tagId})`))
            );
    }

    private log(message: string) { this.logger.log(`[ServiceApi]: ${message}`); }
}
