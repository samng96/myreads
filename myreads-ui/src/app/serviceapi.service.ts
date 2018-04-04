import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, tap } from 'rxjs/operators';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';

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

// TODO: Need to handle errors gracefully.
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
                tap(_ => this.log(`Api: getUser(${userId})`)),
                catchError(this.handleError("getUser", null))
            );
    }
    getReadingLists(userId: number): Observable<ReadingListEntity[]> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists`;
        return this.http.get<ReadingListEntity[]>(url)
            .pipe(
                tap(_ => this.log(`Api: getReadingLists(${userId})`)),
                catchError(this.handleError("getReadingLists", null))
            );
    }
    getReadingList(userId: number, listId: number): Observable<ReadingListEntity> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists/${listId}`;
        return this.http.get<ReadingListEntity>(url)
            .pipe(
                tap(_ => this.log(`Api: getReadingList(${userId}, ${listId})`)),
                catchError(this.handleError("getReadingList", null))
            );
    }
    getReadingListElement(userId: number, rleId: number): Observable<ReadingListElementEntity> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElements/${rleId}`;
        return this.http.get<ReadingListElementEntity>(url)
            .pipe(
                tap(_ => this.log(`Api: getReadingListElement(${userId}, ${rleId})`)),
                catchError(this.handleError("getReadingListElement", null))
            );
    }
    getFollowedLists(userId: number): Observable<FollowedListEntity[]> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/followedLists`;
        return this.http.get<FollowedListEntity[]>(url)
            .pipe(
                tap(_ => this.log(`Api: getFollowedLists(${userId})`)),
                catchError(this.handleError("getFollowedLists", null))
            );
    }
    getTags(): Observable<TagEntity[]> {
        var url = `${ServiceApi.baseUrl}/tags`;
        return this.http.get<TagEntity[]>(url)
            .pipe(
                tap(_ => this.log(`Api: getTags()`)),
                catchError(this.handleError("getTags", null))
            );
    }
    getTag(tagId: number): Observable<TagEntity> {
        var url = `${ServiceApi.baseUrl}/tags/${tagId}`;
        return this.http.get<TagEntity>(url)
            .pipe(
                tap(_ => this.log(`Api: getTags(${tagId})`)),
                catchError(this.handleError("getTag", null))
            );
    }
    getTagByName(tagName: string): Observable<TagEntity> {
        var url = `${ServiceApi.baseUrl}/tagByName/${tagName}`;
        return this.http.get<TagEntity>(url)
            .pipe(
                tap(_ => this.log(`Api: getTagByName(${tagName})`)),
                catchError(this.handleError("getTagByName", null))
            );
    }

    postFollowedList(userId: number, followedListEntity: FollowedListEntity): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/followedLists`;
        return this.http.post(url, followedListEntity)
            .pipe(
                tap(_ => this.log(`Api: postFollowedList(${userId}, ${followedListEntity})`)),
                catchError(this.handleError("postFollowedList", null))
            );
    }
    postTag(tagEntity: TagEntity): Observable<any> {
        var url = `${ServiceApi.baseUrl}/tags`;
        return this.http.post(url, tagEntity)
            .pipe(
                tap(_ => this.log(`Api: postTag(${tagEntity})`)),
                catchError(this.handleError("postTag", null))
            );
    }

    deleteFollowedList(userId: number, followedListId: number): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/followedLists/${followedListId}`;
        return this.http.delete(url)
            .pipe(
                tap(_ => this.log(`Api: deleteFollowedList(${userId}, ${followedListId})`)),
                catchError(this.handleError("deleteFollowedList", null))
            );
    }

    addTagToReadingList(userId: number, listId: number, tagIds: number[]): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists/${listId}/addTags`;
        return this.http.post(url, tagIds)
            .pipe(
                tap(_ => this.log(`Api: addTagToReadingList(${userId}, ${tagIds})`)),
                catchError(this.handleError("addTagToReadingList", null))
            );
    }
    removeTagFromReadingList(userId: number, listId: number, tagId: number): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists/${listId}/tags/${tagId}`;
        return this.http.delete(url)
            .pipe(
                tap(_ => this.log(`Api: removeTagFromReadingList(${userId}, ${listId}, ${tagId})`)),
                catchError(this.handleError("removeTagFromReadingList", null))
            );
    }

    private log(message: string) { this.logger.log(`[ServiceApi]: ${message}`); }
    private handleError<T>(operation: string, result?:T) {
        return (error: any): Observable<T> => {
            this.log(`${operation} failed: ${error.message}`);
            return of(result as T);
        }
    }
}
