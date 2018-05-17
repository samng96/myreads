import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, tap } from 'rxjs/operators';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { Subject } from 'rxjs/subject';

import { LoggerService } from './logger.service';
import { UserEntity, ReadingListEntity, FollowedListEntity, ReadingListElementEntity, TagEntity, CommentEntity } from './entities';
import { LocalStorageObjectService } from './localstorageobject';

@Injectable()
export class ServiceApi {
    public static baseUrl = "http://localhost:8080"

    constructor(
        private lso: LocalStorageObjectService,
        private http: HttpClient,
        private logger: LoggerService
    ) { }

    getUser(userId: number): Subject<UserEntity> {
        var url = `${ServiceApi.baseUrl}/users/${userId}`;
        var result = this.http.get<UserEntity>(url)
            .pipe(
                tap(_ => this.log(`getUser(${userId})`)),
                catchError(this.handleError("getUser", null))
            );

        var subject = new Subject<UserEntity>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(user => this.lso.updateUser(user));
        return subject;
    }
    getReadingLists(userId: number): Subject<ReadingListEntity[]> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists`;
        var result = this.http.get<ReadingListEntity[]>(url)
            .pipe(
                tap(_ => this.log(`getReadingLists(${userId})`)),
                catchError(this.handleError("getReadingLists", null))
            );

        var subject = new Subject<ReadingListEntity[]>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(rls => {
            for (let rl of rls) {
                this.lso.updateReadingList(rl);
            }
        });
        return subject;
    }
    getReadingList(userId: number, listId: number): Subject<ReadingListEntity> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists/${listId}`;
        var result = this.http.get<ReadingListEntity>(url)
            .pipe(
                tap(_ => this.log(`getReadingList(${userId}, ${listId})`)),
                catchError(this.handleError("getReadingList", null))
            );

        var subject = new Subject<ReadingListEntity>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(rl => this.lso.updateReadingList(rl));
        return subject;
    }
    getReadingListsByTag(userId: number, tagId: number): Subject<ReadingListEntity[]> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListsByTag`;
        var result = this.http.post<ReadingListEntity[]>(url, tagId)
            .pipe(
                tap(_ => this.log(`getReadingListsByTag(${userId}, ${tagId})`)),
                catchError(this.handleError("getReadingListsByTag", null))
            );

        var subject = new Subject<ReadingListEntity[]>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(rls => {
            for (let rl of rls) {
                this.lso.updateReadingList(rl);
            }
        });
        return subject;
    }
    getReadingListElement(userId: number, rleId: number): Subject<ReadingListElementEntity> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElements/${rleId}`;
        var result = this.http.get<ReadingListElementEntity>(url)
            .pipe(
                tap(_ => this.log(`getReadingListElement(${userId}, ${rleId})`)),
                catchError(this.handleError("getReadingListElement", null))
            );

        var subject = new Subject<ReadingListElementEntity>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(rle => this.lso.updateReadingListElement(rle));
        return subject;
    }
    getReadingListElementsByTag(userId: number, tagId: number): Subject<ReadingListElementEntity[]> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElementsByTag`;
        var result = this.http.post<ReadingListEntity[]>(url, tagId)
            .pipe(
                tap(_ => this.log(`getReadingListElementsByTag(${userId}, ${tagId})`)),
                catchError(this.handleError("getReadingListElementsByTag", null))
            );

        var subject = new Subject<ReadingListElementEntity[]>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(rles => {
            for (let rle of rles) {
                this.lso.updateReadingListElement(rle);
            }
        });
        return subject;
    }
    getReadingListElements(userId: number, filter: string): Subject<ReadingListElementEntity[]> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElements/?${filter}`;
        var result = this.http.get<ReadingListElementEntity>(url)
            .pipe(
                tap(_ => this.log(`getReadingListElements(${userId}, ${filter})`)),
                catchError(this.handleError("getReadingListElements", null))
            );

        var subject = new Subject<ReadingListElementEntity[]>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(rles => {
            for (let rle of rles) {
                this.lso.updateReadingListElement(rle);
            }
        });
        return subject;
    }
    getFollowedLists(userId: number): Subject<FollowedListEntity[]> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/followedLists`;
        var result = this.http.get<FollowedListEntity[]>(url)
            .pipe(
                tap(_ => this.log(`getFollowedLists(${userId})`)),
                catchError(this.handleError("getFollowedLists", null))
            );

        var subject = new Subject<FollowedListEntity[]>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(fls => {
            for (let fl of fls) {
                this.lso.updateFollowedList(fl);
            }
        });
        return subject;
    }
    getTags(): Subject<TagEntity[]> {
        var url = `${ServiceApi.baseUrl}/tags`;
        var result = this.http.get<TagEntity[]>(url)
            .pipe(
                tap(_ => this.log(`getTags()`)),
                catchError(this.handleError("getTags", null))
            );

        var subject = new Subject<TagEntity[]>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(tags => {
            for (let tag of tags) {
                this.lso.updateTag(tag);
            }
        });
        return subject;
    }
    getTag(tagId: number): Subject<TagEntity> {
        var url = `${ServiceApi.baseUrl}/tags/${tagId}`;
        var result = this.http.get<TagEntity>(url)
            .pipe(
                tap(_ => this.log(`getTags(${tagId})`)),
                catchError(this.handleError("getTag", null))
            );

        var subject = new Subject<TagEntity>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(tag => this.lso.updateTag(tag));
        return subject;
    }
    getTagByName(tagName: string): Subject<TagEntity> {
        var url = `${ServiceApi.baseUrl}/tagByName/${tagName}`;
        var result = this.http.get<TagEntity>(url)
            .pipe(
                tap(_ => this.log(`getTagByName(${tagName})`)),
                catchError(this.handleError("getTagByName", null))
            );

        var subject = new Subject<TagEntity>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(tag => this.lso.updateTag(tag));
        return subject;
    }
    getTagsByUser(userId: number): Subject<TagEntity[]> {
        var url = `${ServiceApi.baseUrl}/tagsByUser/${userId}`;
        var result = this.http.get<TagEntity[]>(url)
            .pipe(
                tap(_ => this.log(`getTagsByUser(${userId})`)),
                catchError(this.handleError("getTagsByUser", null))
            );

        var subject = new Subject<TagEntity[]>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(tags => {
            for (let tag of tags) {
                this.lso.updateTag(tag);
            }
        });
        return subject;
    }
    getComment(userId: number, rleId: number, commentId: number): Observable<CommentEntity> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElements/${rleId}/comments/${commentId}`;
        return this.http.get<CommentEntity>(url)
            .pipe(
                tap(_ => this.log(`getComment(${userId}, ${rleId}, ${commentId})`)),
                catchError(this.handleError("getComment", null))
            );
    }

    postFollowedList(followedListEntity: FollowedListEntity): Subject<number> {
        var url = `${ServiceApi.baseUrl}/users/${followedListEntity.userId}/followedLists`;
        var result = this.http.post(url, followedListEntity)
            .pipe(
                tap(_ => this.log(`postFollowedList(${followedListEntity.userId}, ${followedListEntity})`)),
                catchError(this.handleError("postFollowedList", null))
            );

        var subject = new Subject<number>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(flId => {
            followedListEntity.id = flId;
            this.lso.updateFollowedList(followedListEntity);
        });
        return subject;
    }
    postTag(tagEntity: TagEntity): Subject<number> {
        var url = `${ServiceApi.baseUrl}/tags`;
        var result = this.http.post(url, tagEntity)
            .pipe(
                tap(_ => this.log(`postTag(${tagEntity})`)),
                catchError(this.handleError("postTag", null))
            );

        var subject = new Subject<number>();
        result.subscribe(tagId => subject.next(tagId));

        subject.subscribe(tagId => {
            tagEntity.id = tagId;
            this.lso.updateTag(tagEntity);
        });
        return subject;
    }
    postComment(commentEntity: CommentEntity): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${commentEntity.userId}/readingListElements/${commentEntity.readingListElementId}/comments`;
        return this.http.post(url, commentEntity)
            .pipe(
                tap(_ => this.log(`postComment(${commentEntity})`)),
                catchError(this.handleError("postComment", null))
            );
    }
    postReadingList(listEntity: ReadingListEntity): Subject<number> {
        var url = `${ServiceApi.baseUrl}/users/${listEntity.userId}/readingLists`;
        var result = this.http.post(url, listEntity)
            .pipe(
                tap(_ => this.log(`postReadingList(${listEntity})`)),
                catchError(this.handleError("postReadingList", null))
            );

        var subject = new Subject<number>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(rlId => {
            listEntity.id = rlId;
            this.lso.updateReadingList(listEntity);
        });
        return subject;
    }
    postReadingListElement(rleEntity: ReadingListElementEntity): Subject<number> {
        var url = `${ServiceApi.baseUrl}/users/${rleEntity.userId}/readingListElements`;
        var result = this.http.post(url, rleEntity)
            .pipe(
                tap(_ => this.log(`postReadingListElement(${rleEntity})`)),
                catchError(this.handleError("postReadingListElement", null))
            );

        var subject = new Subject<number>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(rleId => {
            rleEntity.id = rleId;
            this.lso.updateReadingListElement(rleEntity);
        });
        return subject;
    }

    putReadingList(listEntity: ReadingListEntity): Subject<any> {
        var url = `${ServiceApi.baseUrl}/users/${listEntity.userId}/readingLists/${listEntity.id}`;
        var result = this.http.put(url, listEntity)
            .pipe(
                tap(_ => this.log(`putReadingList(${listEntity})`)),
                catchError(this.handleError("putReadingList", null))
            );

        var subject = new Subject<any>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(() => this.lso.updateReadingList(listEntity));
        return subject;
    }
    putReadingListElement(rleEntity: ReadingListElementEntity): Subject<any> {
        var url = `${ServiceApi.baseUrl}/users/${rleEntity.userId}/readingListElements/${rleEntity.id}`;
        var result = this.http.put(url, rleEntity)
            .pipe(
                tap(_ => this.log(`putReadingListElement(${rleEntity})`)),
                catchError(this.handleError("putReadingListElement", null))
            );

        var subject = new Subject<any>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(() => this.lso.updateReadingListElement(rleEntity));
        return subject;
    }

    deleteFollowedList(userId: number, followedListId: number): Subject<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/followedLists/${followedListId}`;
        var result = this.http.delete(url)
            .pipe(
                tap(_ => this.log(`deleteFollowedList(${userId}, ${followedListId})`)),
                catchError(this.handleError("deleteFollowedList", null))
            );

        var subject = new Subject<any>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(() => this.lso.deleteMyFollowedList(followedListId));
        return subject;
    }
    deleteReadingListElement(userId: number, rleId: number): Subject<any>{
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElements/${rleId}`;
        var result = this.http.delete(url)
            .pipe(
                tap(_ => this.log(`deleteReadingListElement(${userId}, ${rleId})`)),
                catchError(this.handleError("deleteReadingListElement", null))
            );

        var subject = new Subject<any>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(() => {
            var rle = this.lso.getReadingListElements()[rleId];
            this.lso.deleteReadingListElement(rleId);
            for (let commentId of rle.commentIds) {
                this.lso.deleteComment(commentId);
            }
            for (let listId of rle.listIds) {
                var arr = this.lso.getReadingLists()[listId].readingListElementIds;
                var index = arr.indexOf(rleId, 0);
                arr.splice(index, 1);

                this.lso.updateReadingList(this.lso.getReadingLists()[listId]);
            }
        });
        return subject;
    }
    deleteReadingList(userId: number, readingListId: number): Subject<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists/${readingListId}`;
        var result = this.http.delete(url)
            .pipe(
                tap(_ => this.log(`deleteReadingList(${userId}, ${readingListId})`)),
                catchError(this.handleError("deleteReadingList", null))
            );

        var subject = new Subject<any>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(() => this.lso.deleteReadingList(readingListId));
        return subject;
    }
    deleteComment(userId: number, rleId: number, commentId: number): Observable<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElements/${rleId}/comments/${commentId}`;
        return this.http.delete(url)
            .pipe(
                tap(_ => this.log(`deleteComment(${userId}, ${rleId}, ${commentId})`)),
                catchError(this.handleError("deleteComment", null))
            );
    }

    addTagToReadingList(userId: number, listId: number, tagIds: number[]): Subject<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists/${listId}/addTags`;
        var result = this.http.post(url, tagIds)
            .pipe(
                tap(_ => this.log(`addTagToReadingList(${userId}, ${listId}, ${tagIds})`)),
                catchError(this.handleError("addTagToReadingList", null))
            );

        var subject = new Subject<any>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(() => {
            this.lso.getReadingLists()[listId].tagIds.push(tagIds[0]);
            this.lso.updateReadingList(this.lso.getReadingLists()[listId]);
        });
        return subject;
    }
    removeTagFromReadingList(userId: number, listId: number, tagId: number): Subject<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists/${listId}/tags/${tagId}`;
        var result = this.http.delete(url)
            .pipe(
                tap(_ => this.log(`removeTagFromReadingList(${userId}, ${listId}, ${tagId})`)),
                catchError(this.handleError("removeTagFromReadingList", null))
            );

        var subject = new Subject<any>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(() => {
            var index = this.lso.getReadingLists()[listId].tagIds.indexOf(tagId, 0);
            this.lso.getReadingLists()[listId].tagIds.splice(index, 1);
            this.lso.updateReadingList(this.lso.getReadingLists()[listId]);
        });
        return subject;
    }

    addTagToReadingListElement(userId: number, rleId: number, tagIds: number[]): Subject<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElements/${rleId}/addTags`;
        var result = this.http.post(url, tagIds)
            .pipe(
                tap(_ => this.log(`addTagToReadingListElement(${userId}, ${rleId}, ${tagIds})`)),
                catchError(this.handleError("addTagToReadingListElement", null))
            );

        var subject = new Subject<any>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(() => {
            this.lso.getReadingListElements()[rleId].tagIds.push(tagIds[0]);
            this.lso.updateReadingListElement(this.lso.getReadingListElements()[rleId]);
        });
        return subject;
    }
    removeTagFromReadingListElement(userId: number, rleId: number, tagId: number): Subject<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingListElements/${rleId}/tags/${tagId}`;
        var result = this.http.delete(url)
            .pipe(
                tap(_ => this.log(`removeTagFromReadingListElement(${userId}, ${rleId}, ${tagId})`)),
                catchError(this.handleError("removeTagFromReadingListElement", null))
            );

        var subject = new Subject<any>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(() => {
            var index = this.lso.getReadingListElements()[rleId].tagIds.indexOf(tagId, 0);
            this.lso.getReadingListElements()[rleId].tagIds.splice(index, 1);
            this.lso.updateReadingListElement(this.lso.getReadingListElements()[rleId]);
        });
        return subject;
    }

    addReadingListElementToReadingList(userId: number, listId: number, rleIds: number[]): Subject<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists/${listId}/addReadingListElements`;
        var result = this.http.post(url, rleIds)
            .pipe(
                tap(_ => this.log(`Api: addReadingListElementToReadingList(${userId}, ${listId}, ${rleIds})`)),
                catchError(this.handleError("addReadingListElementToReadingList", null))
            );

        var subject = new Subject<any>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(() => {
            var rle = this.lso.getReadingListElements()[rleIds[0]];
            var rl = this.lso.getReadingLists()[listId];

            rle.listIds.push(listId);
            rl.readingListElementIds.push(rleIds[0]);

            this.lso.updateReadingListElement(rle);
            this.lso.updateReadingList(rl);
        });
        return subject;
    }
    removeReadingListElementFromReadingList(userId: number, listId: number, rleId: number): Subject<any> {
        var url = `${ServiceApi.baseUrl}/users/${userId}/readingLists/${listId}/readingListElements/${rleId}`;
        var result = this.http.delete(url)
            .pipe(
                tap(_ => this.log(`removeReadingListElementFromReadingList(${userId}, ${listId}, ${rleId})`)),
                catchError(this.handleError("removeReadingListElementFromReadingList", null))
            );

        var subject = new Subject<any>();
        result.subscribe(x => subject.next(x));
        subject.subscribe(() => {
            var rle = this.lso.getReadingListElements()[rleId];
            var rl = this.lso.getReadingLists()[listId];

            var index = rle.listIds.indexOf(listId, 0);
            rle.listIds.splice(index, 1);

            index = rl.readingListElementIds.indexOf(rleId, 0);
            rl.readingListElementIds.splice(index, 1);

            this.lso.updateReadingList(rl);
            this.lso.updateReadingListElement(rle);
        });
        return subject;
    }

    private log(message: string) { this.logger.log(`[ServiceApi]: ${message}`); }
    private handleError<T>(operation: string, result?:T) {
        return (error: any): Observable<T> => {
            this.log(`${operation} failed: ${error.message}`);
            return of(result as T);
        }
    }
}
