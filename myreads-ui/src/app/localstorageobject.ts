import { UserEntity, ReadingListEntity, FollowedListEntity, ReadingListElementEntity, TagEntity, CommentEntity } from './serviceapi.service';

export class LocalStorageObject {
    public myUserId: number; // The current user's Id
    public myLoginToken: string; // TODO: This will eventually do some auth thing.

    public myFollowedLists: number[];
    public myReadingLists: number[];
    public myReadingListElements: number[];
    public myComments: number[];

    // Globally cached stuff.
    public users: Map<number, UserEntity>;
    public readingLists: Map<number, ReadingListEntity>;
    public followedLists: Map<number, FollowedListEntity>;
    public readingListElements: Map<number, ReadingListElementEntity>;
    public comments: Map<number, CommentEntity>;
    public tags: Map<number, TagEntity>;

    constructor() {
        this.myUserId = -1;
        this.myLoginToken = null;
        this.myReadingLists = [];
        this.myFollowedLists = [];
        this.myReadingListElements = [];
        this.myComments = [];

        this.users = new Map<number, UserEntity>();
        this.readingLists = new Map<number, ReadingListEntity>();
        this.followedLists = new Map<number, FollowedListEntity>();
        this.readingListElements = new Map<number, ReadingListElementEntity>();
        this.comments = new Map<number, CommentEntity>();
        this.tags = new Map<number, TagEntity>();
    }

    public static load(): LocalStorageObject {
        var o = new LocalStorageObject();
        var loadedObject = JSON.parse(localStorage.getItem("localStorageObject"));
        if (loadedObject != null) {
            o.myUserId = loadedObject.myUserId;
            o.myLoginToken = loadedObject.myLoginToken;
            o.myReadingLists = loadedObject.myReadingLists;
            o.myFollowedLists = loadedObject.myFollowedLists;
            o.myReadingListElements = loadedObject.myReadingListElements;
            o.myComments = loadedObject.myComments;

            o.users = loadedObject.users;
            o.readingLists = loadedObject.readingLists;
            o.followedLists = loadedObject.followedLists;
            o.readingListElements = loadedObject.readingListElements;
            o.comments = loadedObject.comments;
            o.tags = loadedObject.tags;
        }
        return o;
    }

    private save(): void {
        localStorage.setItem("localStorageObject", JSON.stringify(this));
    }

    public setMyUserId(userId: number): void {
        this.myUserId = userId;
        this.save();
    }
    public setMyLoginToken(myLoginToken: string): void {
        this.myLoginToken = myLoginToken;
        this.save();
    }

    public updateMyReadingLists(listId: number): void {
        this.myReadingLists.push(listId);
        this.save();
    }
    public updateMyFollowedLists(listId: number): void {
        this.myFollowedLists.push(listId);
        this.save();
    }
    public updateMyReadingListElements(rleId: number): void {
        this.myReadingListElements.push(rleId);
        this.save();
    }

    public updateUser(userEntity: UserEntity): void {
        this.users[userEntity.id] = userEntity;
        this.save();
    }
    public updateReadingList(listEntity: ReadingListEntity): void {
        this.readingLists[listEntity.id] = listEntity;
        this.save();
    }
    public updateFollowedList(listEntity: FollowedListEntity): void {
        this.followedLists[listEntity.id] = listEntity;
        this.save();
    }
    public updateReadingListElement(rle: ReadingListElementEntity): void {
        this.readingListElements[rle.id] = rle;
        this.save();
    }
}
