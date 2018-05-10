import { Injectable, EventEmitter, Output } from '@angular/core';
import { UserEntity, ReadingListEntity, FollowedListEntity, ReadingListElementEntity, TagEntity, CommentEntity } from './entities';
import { ReadingListElementExtras } from './entityextras';

export class LocalStorageObject {
    public myUserId: number; // The current user's Id
    public myLoginToken: string; // TODO: This will eventually do some auth thing.

    public myFollowedLists: number[];
    public myReadingLists: number[];

    // Globally cached stuff.
    public users: Map<number, UserEntity>;
    public readingLists: Map<number, ReadingListEntity>;
    public followedLists: Map<number, FollowedListEntity>;
    public readingListElements: Map<number, ReadingListElementEntity>;
    public comments: Map<number, CommentEntity>;
    public tags: Map<number, TagEntity>;
    public tagsByName: Map<string, TagEntity>;

    // This stuff is cached for dispay, not just from the API.
    public rleExtras: Map<number, ReadingListElementExtras>;

    constructor() {
        var loadedObject = JSON.parse(localStorage.getItem("LocalStorageObject"));
        if (loadedObject != null) {
            this.myUserId = loadedObject.myUserId;
            this.myLoginToken = loadedObject.myLoginToken;
            this.myFollowedLists = loadedObject.myFollowedLists;
            this.myReadingLists = loadedObject.myReadingLists;

            this.users = this.makeMap(loadedObject.users);
            this.readingLists = this.makeMap(loadedObject.readingLists);
            this.followedLists = this.makeMap(loadedObject.followedLists);
            this.readingListElements = this.makeMap(loadedObject.readingListElements);
            this.comments = this.makeMap(loadedObject.comments);
            this.tags = this.makeMap(loadedObject.tags);
            this.tagsByName = this.makeMap(loadedObject.tagsByName);

            this.rleExtras = this.makeMap(loadedObject.rleExtras);
        }
        else {
            this.myUserId = -1;
            this.myLoginToken = null;
            this.myFollowedLists = [];
            this.myReadingLists = [];

            this.users = new Map<number, UserEntity>();
            this.readingLists = new Map<number, ReadingListEntity>();
            this.followedLists = new Map<number, FollowedListEntity>();
            this.readingListElements = new Map<number, ReadingListElementEntity>();
            this.comments = new Map<number, CommentEntity>();
            this.tags = new Map<number, TagEntity>();
            this.tagsByName = new Map<string, TagEntity>();

            this.rleExtras = new Map<number, ReadingListElementExtras>();
        }
    }

    private makeMap(obj: any): Map<any, any> {
        var map = new Map();
        Object.keys(obj).forEach(key => {
            map[key] = obj[key];
        });
        return map;
    }

    public save(): void {
        localStorage.setItem("LocalStorageObject", JSON.stringify(this));
    }
}

// TODO: Ensure uniqueness in each of these lists.
@Injectable()
export class LocalStorageObjectService {
    @Output() changeLogin: EventEmitter<string> = new EventEmitter();
    @Output() changeListAdd: EventEmitter<ReadingListEntity> = new EventEmitter();
    lso: LocalStorageObject;

    constructor() {
        this.lso = new LocalStorageObject();
    }

    public setMyUserId(userId: number): void {
        this.lso.myUserId = userId;
        this.lso.save();
    }
    public setMyLoginToken(myLoginToken: string): void {
        this.lso.myLoginToken = myLoginToken;
        this.lso.save();

        this.changeLogin.emit(this.lso.myLoginToken);
    }

    public getMyUserId(): number { return this.lso.myUserId; }
    public getMyLoginToken(): string { return this.lso.myLoginToken; }
    public getMyFollowedLists(): number[] { return this.lso.myFollowedLists; }
    public getMyReadingLists(): number[] { return this.lso.myReadingLists; }

    public getReadingLists(): Map<number, ReadingListEntity> { return this.lso.readingLists; }
    public getReadingListElements(): Map<number, ReadingListElementEntity> { return this.lso.readingListElements; }
    public getUsers(): Map<number, UserEntity> { return this.lso.users; }
    public getTags(): Map<number, TagEntity> { return this.lso.tags; }
    public getTagsByName(): Map<string, TagEntity> { return this.lso.tagsByName; }
    public getComments(): Map<number, CommentEntity> { return this.lso.comments; }
    public getFollowedLists(): Map<number, FollowedListEntity> { return this.lso.followedLists; }

    public updateMyFollowedLists(listId: number): void {
        if (this.lso.myFollowedLists.indexOf(listId, 0) == -1) {
            this.lso.myFollowedLists.push(listId);
            this.lso.save();
        }
    }
    public deleteMyFollowedList(listId: number): void {
        var index = this.lso.myFollowedLists.indexOf(listId, 0);
        if (index != -1) {
            this.lso.myFollowedLists.splice(index, 1);
            this.lso.save();
        }
    }

    public updateTags(tags: TagEntity[]): void {
        for (let tag of tags) {
            this.updateTag(tag);
        }
        this.lso.save();
    }
    public updateTag(tag: TagEntity): void {
        this.lso.tags[tag.id] = tag;
        this.lso.tagsByName[tag.tagName] = tag;
        this.lso.save();
    }
    public updateUser(userEntity: UserEntity): void {
        this.lso.users[userEntity.id] = userEntity;
        this.lso.save();
    }
    public updateReadingList(listEntity: ReadingListEntity): void {
        this.lso.readingLists[listEntity.id] = listEntity;

        if (listEntity.userId == this.lso.myUserId) {
            var index = this.lso.myReadingLists.indexOf(listEntity.id, 0);
            if (index == -1) {
                this.lso.myReadingLists.push(listEntity.id);
            }
        }
        this.lso.save();
    }
    public updateFollowedList(listEntity: FollowedListEntity): void {
        this.lso.followedLists[listEntity.id] = listEntity;
        this.lso.save();
    }
    public updateReadingListElement(rle: ReadingListElementEntity): void {
        this.lso.readingListElements[rle.id] = rle;
        this.lso.save();
    }

    public addReadingList(listEntity: ReadingListEntity): void {
        this.lso.readingLists[listEntity.id] = listEntity;
        this.lso.save();

        this.changeListAdd.emit(listEntity);
    }

    public deleteReadingListElement(rleId: number): void {
        this.lso.readingListElements.delete(rleId);
        this.lso.save();
    }
    public deleteComment(commentId: number): void {
        this.lso.comments.delete(commentId);
        this.lso.save();
    }
    public deleteReadingList(list: ReadingListEntity): void {
        this.lso.readingLists.delete(list.id);

        var index = this.lso.myReadingLists.indexOf(list.id, 0);
        if (index != -1) {
            this.lso.myReadingLists.splice(index, 1);
        }
        this.lso.save();
    }

    public updateRleExtras(rleId: number, rleExtra: ReadingListElementExtras): void {
        this.lso.rleExtras[rleId] = rleExtra;
        this.lso.save();
    }
    public getRleExtras(): Map<number, ReadingListElementExtras> { return this.lso.rleExtras; }
}
