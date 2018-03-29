import { UserEntity, ReadingListEntity, FollowedListEntity } from './serviceapi.service';

export class LocalStorageObject {
    public myUserId: number; // The current user's Id
    public myLoginToken: string; // TODO: This will eventually do some auth thing.

    public myFollowedLists: number[];
    public myReadingLists: number[];

    // Globally cached stuff.
    public users: Map<number, UserEntity>;
    public readingLists: Map<number, ReadingListEntity>;
    public followedLists: Map<number, FollowedListEntity>;

    constructor() {
        this.myUserId = -1;
        this.myLoginToken = null;
        this.users = new Map<number, UserEntity>();
        this.readingLists = new Map<number, ReadingListEntity>();
        this.followedLists = new Map<number, FollowedListEntity>();

        this.myReadingLists = [];
        this.myFollowedLists = [];
    }

    public static load(): LocalStorageObject {
        var o = new LocalStorageObject();
        var loadedObject = JSON.parse(localStorage.getItem("localStorageObject"));
        if (loadedObject != null) {
            o.myUserId = loadedObject.myUserId;
            o.myLoginToken = loadedObject.myLoginToken;
            o.users = loadedObject.users;
            o.readingLists = loadedObject.readingLists;
            o.followedLists = loadedObject.followedLists;
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
}
