import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, tap } from 'rxjs/operators';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';

import { LoggerService } from './logger.service';
import { UserEntity, ReadingListEntity, FollowedListEntity, ReadingListElementEntity, TagEntity, CommentEntity } from './entities';

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
                tap(_ => this.log(`getUser(${userId})`)),
                catchError(this.handleError("getUser", null))
            );
    }
    getReadingLists(userId: number): Observable<ReadingListEntity[]> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists`;
        return this.http.get<ReadingListEntity[]>(url)
            .pipe(
                tap(_ => this.log(`getReadingLists(${userId})`)),
                catchError(this.handleError("getReadingLists", null))
            );
    }
    getReadingList(userId: number, listId: number): Observable<ReadingListEntity> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists/${listId}`;
        return this.http.get<ReadingListEntity>(url)
            .pipe(
                tap(_ => this.log(`getReadingList(${userId}, ${listId})`)),
                catchError(this.handleError("getReadingList", null))
            );
    }
    getReadingListsByTag(userId: number, tagId: number): Observable<ReadingListEntity[]> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListsByTag`;
        return this.http.post<ReadingListEntity[]>(url, tagId)
            .pipe(
                tap(_ => this.log(`getReadingListsByTag(${userId}, ${tagId})`)),
                catchError(this.handleError("getReadingListsByTag", null))
            );
    }
    getReadingListElement(userId: number, rleId: number): Observable<ReadingListElementEntity> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElements/${rleId}`;
        return this.http.get<ReadingListElementEntity>(url)
            .pipe(
                tap(_ => this.log(`getReadingListElement(${userId}, ${rleId})`)),
                catchError(this.handleError("getReadingListElement", null))
            );
    }
    getReadingListElementsByTag(userId: number, tagId: number): Observable<ReadingListElementEntity[]> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElementsByTag`;
        return this.http.post<ReadingListEntity[]>(url, tagId)
            .pipe(
                tap(_ => this.log(`getReadingListElementsByTag(${userId}, ${tagId})`)),
                catchError(this.handleError("getReadingListElementsByTag", null))
            );
    }
    getReadingListElements(userId: number, filter: string): Observable<ReadingListElementEntity[]> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElements/?${filter}`;
        return this.http.get<ReadingListElementEntity>(url)
            .pipe(
                tap(_ => this.log(`getReadingListElements(${userId}, ${filter})`)),
                catchError(this.handleError("getReadingListElements", null))
            );
    }
    getFollowedLists(userId: number): Observable<FollowedListEntity[]> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/followedLists`;
        return this.http.get<FollowedListEntity[]>(url)
            .pipe(
                tap(_ => this.log(`getFollowedLists(${userId})`)),
                catchError(this.handleError("getFollowedLists", null))
            );
    }
    getTags(): Observable<TagEntity[]> {
        var url = `${ServiceApi.baseUrl}/tags`;
        return this.http.get<TagEntity[]>(url)
            .pipe(
                tap(_ => this.log(`getTags()`)),
                catchError(this.handleError("getTags", null))
            );
    }
    getTag(tagId: number): Observable<TagEntity> {
        var url = `${ServiceApi.baseUrl}/tags/${tagId}`;
        return this.http.get<TagEntity>(url)
            .pipe(
                tap(_ => this.log(`getTags(${tagId})`)),
                catchError(this.handleError("getTag", null))
            );
    }
    getTagByName(tagName: string): Observable<TagEntity> {
        var url = `${ServiceApi.baseUrl}/tagByName/${tagName}`;
        return this.http.get<TagEntity>(url)
            .pipe(
                tap(_ => this.log(`getTagByName(${tagName})`)),
                catchError(this.handleError("getTagByName", null))
            );
    }
    getTagsByUser(userId: number): Observable<TagEntity[]> {
        var url = `${ServiceApi.baseUrl}/tagsByUser/${userId}`;
        return this.http.get<TagEntity[]>(url)
            .pipe(
                tap(_ => this.log(`getTagsByUser(${userId})`)),
                catchError(this.handleError("getTagsByUser", null))
            );
    }
    getComment(userId: number, rleId: number, commentId: number): Observable<CommentEntity> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElements/${rleId}/comments/${commentId}`;
        return this.http.get<CommentEntity>(url)
            .pipe(
                tap(_ => this.log(`getComment(${userId}, ${rleId}, ${commentId})`)),
                catchError(this.handleError("getComment", null))
            );
    }

    postFollowedList(followedListEntity: FollowedListEntity): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${followedListEntity.userId}/followedLists`;
        return this.http.post(url, followedListEntity)
            .pipe(
                tap(_ => this.log(`postFollowedList(${followedListEntity.userId}, ${followedListEntity})`)),
                catchError(this.handleError("postFollowedList", null))
            );
    }
    postTag(tagEntity: TagEntity): Observable<any> {
        var url = `${ServiceApi.baseUrl}/tags`;
        return this.http.post(url, tagEntity)
            .pipe(
                tap(_ => this.log(`postTag(${tagEntity})`)),
                catchError(this.handleError("postTag", null))
            );
    }
    postComment(commentEntity: CommentEntity): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${commentEntity.userId}/readingListElements/${commentEntity.readingListElementId}/comments`;
        return this.http.post(url, commentEntity)
            .pipe(
                tap(_ => this.log(`postComment(${commentEntity})`)),
                catchError(this.handleError("postComment", null))
            );
    }
    postReadingList(listEntity: ReadingListEntity): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${listEntity.userId}/readingLists`;
        return this.http.post(url, listEntity)
            .pipe(
                tap(_ => this.log(`postReadingList(${listEntity})`)),
                catchError(this.handleError("postReadingList", null))
            );
    }
    postReadingListElement(rleEntity: ReadingListElementEntity): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${rleEntity.userId}/readingListElements`;
        return this.http.post(url, rleEntity)
            .pipe(
                tap(_ => this.log(`postReadingListElement(${rleEntity})`)),
                catchError(this.handleError("postReadingListElement", null))
            );
    }

    putReadingList(listEntity: ReadingListEntity): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${listEntity.userId}/readingLists/${listEntity.id}`;
        return this.http.put(url, listEntity)
            .pipe(
                tap(_ => this.log(`putReadingList(${listEntity})`)),
                catchError(this.handleError("putReadingList", null))
            );
    }
    putReadingListElement(rleEntity: ReadingListElementEntity): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${rleEntity.userId}/readingListElements/${rleEntity.id}`;
        return this.http.put(url, rleEntity)
            .pipe(
                tap(_ => this.log(`putReadingListElement(${rleEntity})`)),
                catchError(this.handleError("putReadingListElement", null))
            );
    }

    deleteFollowedList(userId: number, followedListId: number): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/followedLists/${followedListId}`;
        return this.http.delete(url)
            .pipe(
                tap(_ => this.log(`deleteFollowedList(${userId}, ${followedListId})`)),
                catchError(this.handleError("deleteFollowedList", null))
            );
    }
    deleteReadingListElement(userId: number, rleId: number): Observable<any>{
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElements/${rleId}`;
        return this.http.delete(url)
            .pipe(
                tap(_ => this.log(`deleteReadingListElement(${userId}, ${rleId})`)),
                catchError(this.handleError("deleteReadingListElement", null))
            );
    }
    deleteReadingList(userId: number, readingListId: number): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists/${readingListId}`;
        return this.http.delete(url)
            .pipe(
                tap(_ => this.log(`deleteReadingList(${userId}, ${readingListId})`)),
                catchError(this.handleError("deleteReadingList", null))
            );
    }
    deleteComment(userId: number, rleId: number, commentId: number): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElements/${rleId}/comments/${commentId}`;
        return this.http.delete(url)
            .pipe(
                tap(_ => this.log(`deleteComment(${userId}, ${rleId}, ${commentId})`)),
                catchError(this.handleError("deleteComment", null))
            );
    }

    addTagToReadingList(userId: number, listId: number, tagIds: number[]): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists/${listId}/addTags`;
        return this.http.post(url, tagIds)
            .pipe(
                tap(_ => this.log(`addTagToReadingList(${userId}, ${listId}, ${tagIds})`)),
                catchError(this.handleError("addTagToReadingList", null))
            );
    }
    removeTagFromReadingList(userId: number, listId: number, tagId: number): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists/${listId}/tags/${tagId}`;
        return this.http.delete(url)
            .pipe(
                tap(_ => this.log(`removeTagFromReadingList(${userId}, ${listId}, ${tagId})`)),
                catchError(this.handleError("removeTagFromReadingList", null))
            );
    }

    addTagToReadingListElement(userId: number, rleId: number, tagIds: number[]): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElements/${rleId}/addTags`;
        return this.http.post(url, tagIds)
            .pipe(
                tap(_ => this.log(`addTagToReadingListElement(${userId}, ${rleId}, ${tagIds})`)),
                catchError(this.handleError("addTagToReadingListElement", null))
            );
    }
    removeTagFromReadingListElement(userId: number, rleId: number, tagId: number): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElements/${rleId}/tags/${tagId}`;
        return this.http.delete(url)
            .pipe(
                tap(_ => this.log(`removeTagFromReadingListElement(${userId}, ${rleId}, ${tagId})`)),
                catchError(this.handleError("removeTagFromReadingListElement", null))
            );
    }

    addReadingListElementToReadingList(userId: number, listId: number, rleIds: number[]): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists/${listId}/addReadingListElements`;
        return this.http.post(url, rleIds)
            .pipe(
                tap(_ => this.log(`Api: addReadingListElementToReadingList(${userId}, ${listId}, ${rleIds})`)),
                catchError(this.handleError("addReadingListElementToReadingList", null))
            );
    }
    removeReadingListElementFromReadingList(userId: number, listId: number, rleId: number): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists/${listId}/readingListElements/${rleId}`;
        return this.http.delete(url)
            .pipe(
                tap(_ => this.log(`removeReadingListElementFromReadingList(${userId}, ${listId}, ${rleId})`)),
                catchError(this.handleError("removeReadingListElementFromReadingList", null))
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
