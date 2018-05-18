import { Injectable, EventEmitter, Output } from '@angular/core';
import { UserEntity, ReadingListEntity, FollowedListEntity, ReadingListElementEntity, TagEntity, CommentEntity } from './entities';
import { ReadingListElementExtras } from './entityextras';

export class LocalStorageObject {
    public myUserId: number; // The current user's Id
    public myLoginToken: string = null;
    public myLoginImage: string = null;
    public myUserName: string = null;

    // Globally cached stuff.
    public users: Map<number, UserEntity>;
    public readingLists: Map<number, ReadingListEntity>;
    public followedLists: Map<number, FollowedListEntity>;
    public readingListElements: Map<number, ReadingListElementEntity>;
    public comments: Map<number, CommentEntity>;
    public tags: Map<number, TagEntity>;
    public tagsByName: Map<string, TagEntity>;
    public readingListsByUser: Map<number, number[]>;
    public followedListsByUser: Map<number, number[]>;

    // This stuff is cached for dispay, not just from the API.
    public rleExtras: Map<number, ReadingListElementExtras>;

    constructor() {
        var loadedObject = JSON.parse(localStorage.getItem("LocalStorageObject"));
        if (loadedObject != null) {
            this.myUserId = loadedObject.myUserId;
            this.myLoginToken = loadedObject.myLoginToken;
            this.myLoginImage = loadedObject.myLoginImage;
            this.myUserName = loadedObject.myUserName;

            this.users = this.makeMap(loadedObject.users);
            this.readingLists = this.makeMap(loadedObject.readingLists);
            this.followedLists = this.makeMap(loadedObject.followedLists);
            this.readingListElements = this.makeMap(loadedObject.readingListElements);
            this.comments = this.makeMap(loadedObject.comments);
            this.tags = this.makeMap(loadedObject.tags);
            this.tagsByName = this.makeMap(loadedObject.tagsByName);
            this.readingListsByUser = this.makeMap(loadedObject.readingListsByUser);
            this.followedListsByUser = this.makeMap(loadedObject.followedListsByUser);

            this.rleExtras = this.makeMap(loadedObject.rleExtras);
        }
        else {
            this.myUserId = -1;
            this.myLoginToken = null;
            this.myLoginImage = null;
            this.myUserName = null;

            this.users = new Map<number, UserEntity>();
            this.readingLists = new Map<number, ReadingListEntity>();
            this.followedLists = new Map<number, FollowedListEntity>();
            this.readingListElements = new Map<number, ReadingListElementEntity>();
            this.comments = new Map<number, CommentEntity>();
            this.tags = new Map<number, TagEntity>();
            this.tagsByName = new Map<string, TagEntity>();
            this.readingListsByUser = new Map<number, number[]>();
            this.followedListsByUser = new Map<number, number[]>();

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

@Injectable()
export class LocalStorageObjectService {
    @Output() changeLogin: EventEmitter<number> = new EventEmitter();
    @Output() changeListAdd: EventEmitter<ReadingListEntity> = new EventEmitter();
    lso: LocalStorageObject;

    constructor() {
        this.lso = new LocalStorageObject();
    }

    public setMyLoginInfo(loginToken: string, userId: number, loginImage: string, userName: string): void {
        this.lso.myUserId = userId;
        this.lso.myLoginToken = loginToken;
        this.lso.myLoginImage = loginImage;
        this.lso.myUserName = userName;
        this.lso.save();

        this.changeLogin.emit(userId);
    }
    public setLoggedOut(): void {
        this.lso.myUserId = -1;
        this.lso.myLoginToken = null;
        this.lso.myLoginImage = null;
        this.lso.myUserName = null;
        this.lso.save();

        this.changeLogin.emit(-1);
    }
    public getMyLoginToken(): any { return this.lso.myLoginToken; }
    public getMyUserId(): number { return this.lso.myUserId; }
    public getMyUserName(): string { return this.lso.myUserName; }
    public getMyLoginImage(): string { return this.lso.myLoginImage; }
    public isLoggedIn(): boolean { return this.lso.myLoginToken != null; }

    public getReadingList(listId: number): ReadingListEntity { return this.lso.readingLists[listId]; }
    public getReadingListsByUser(userId: number): number[] { return this.lso.readingListsByUser[userId]; }
    public getReadingLists(): Map<number, ReadingListEntity> { return this.lso.readingLists; }
    public getReadingListElements(): Map<number, ReadingListElementEntity> { return this.lso.readingListElements; }
    public getReadingListElement(rleId: number): ReadingListElementEntity { return this.lso.readingListElements[rleId]; }
    public getUsers(): Map<number, UserEntity> { return this.lso.users; }
    public getUser(userId: number): UserEntity { return this.lso.users[userId]; }
    public getTags(): Map<number, TagEntity> { return this.lso.tags; }
    public getTagsByName(): Map<string, TagEntity> { return this.lso.tagsByName; }
    public getComments(): Map<number, CommentEntity> { return this.lso.comments; }
    public getFollowedList(flId: number): FollowedListEntity { return this.lso.followedLists[flId]; }
    public getFollowedLists(): Map<number, FollowedListEntity> { return this.lso.followedLists; }
    public getFollowedListsByUser(userId: number): number[] { return this.lso.followedListsByUser[userId]; }

    public updateTags(tags: TagEntity[]): void {
        if (tags == null) { return; }

        tags.forEach(tag => this.updateTag(tag));
    }
    public updateTag(tag: TagEntity): void {
        if (tag == null) { return; }

        this.lso.tags[tag.id] = tag;
        this.lso.tagsByName[tag.tagName] = tag;
        this.lso.save();
    }
    public updateUser(userEntity: UserEntity): void {
        if (userEntity == null) { return; }

        this.lso.users[userEntity.id] = userEntity;
        this.lso.save();
    }
    public updateReadingList(listEntity: ReadingListEntity): void {
        if (listEntity == null) { return; }

        this.lso.readingLists[listEntity.id] = listEntity;
        var lists = this.lso.readingListsByUser[listEntity.userId];
        var userId = listEntity.userId;
        if (lists == null) {
            this.lso.readingListsByUser[userId] = [listEntity.id];
        }
        else {
            var index = this.lso.readingListsByUser[userId].indexOf(listEntity.id, 0);
            if (index == -1) {
                this.lso.readingListsByUser[userId].push(listEntity.id);
            }
        }
        this.lso.save();
    }
    public updateFollowedList(listEntity: FollowedListEntity): void {
        if (listEntity == null) { return; }

        this.lso.followedLists[listEntity.id] = listEntity;
        var lists = this.lso.followedListsByUser[listEntity.userId];
        var userId = listEntity.userId;
        if (lists == null) {
            this.lso.followedListsByUser[userId] = [listEntity.id];
        }
        else {
            var index = this.lso.followedListsByUser[userId].indexOf(listEntity.id, 0);
            if (index == -1) {
                this.lso.followedListsByUser[userId].push(listEntity.id);
            }
        }
        this.lso.save();
    }
    public updateReadingListElement(rle: ReadingListElementEntity): void {
        if (rle == null) { return; }

        this.lso.readingListElements[rle.id] = rle;
        this.lso.save();
    }
    public updateReadingListElements(rles: ReadingListElementEntity[]): void {
        if (rles == null) { return; }

        rles.forEach(rle => this.updateReadingListElement(rle));
    }

    public addReadingList(listEntity: ReadingListEntity): void {
        if (listEntity == null) { return; }

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
    public deleteReadingList(listId: number): void {
        var rl = this.lso.readingLists[listId];
        if (rl == null) { return; }

        this.lso.readingLists.delete(listId);

        var index = this.lso.readingListsByUser[rl.userId].indexOf(listId, 0);
        this.lso.readingListsByUser[rl.userId].splice(index, 1);
        this.lso.save();
    }
    public deleteFollowedList(flId: number): void {
        var fl = this.lso.followedLists[flId];
        if (fl == null) { return; }

        this.lso.followedLists.delete(flId);
        var index = this.lso.followedListsByUser[fl.userId].indexOf(flId, 0);
        this.lso.followedListsByUser[fl.userId].splice(index, 1);
        this.lso.save();
    }

    public updateRleExtras(rleId: number, rleExtra: ReadingListElementExtras): void {
        this.lso.rleExtras[rleId] = rleExtra;
        this.lso.save();
    }
    public getRleExtras(): Map<number, ReadingListElementExtras> { return this.lso.rleExtras; }
}
