import { UserEntity, ReadingListEntity, FollowedListEntity } from './serviceapi.service';

export class LocalStorageObject {
    public loggedInUserId: number; // The current user's Id
    public loginToken: string; // TODO: This will eventually do some auth thing.
    public users: Map<number, UserEntity>;
    public readingLists: Map<number, ReadingListEntity>;
    public followedLists: Map<number, FollowedListEntity>;

    constructor() {
        this.loggedInUserId = -1;
        this.loginToken = null;
        this.users = new Map<number, UserEntity>();
        this.readingLists = new Map<number, ReadingListEntity>();
        this.followedLists = new Map<number, FollowedListEntity>();
    }

    public static load(): LocalStorageObject {
        var o = new LocalStorageObject();
        var loadedObject = JSON.parse(localStorage.getItem("localStorageObject"));
        if (loadedObject != null) {
            o.loggedInUserId = loadedObject.loggedInUserId;
            o.loginToken = loadedObject.loginToken;
            o.users = loadedObject.users;
            o.readingLists = loadedObject.readingLists;
            o.followedLists = loadedObject.followedLists;
        }
        return o;
    }

    private save(): void {
        localStorage.setItem("localStorageObject", JSON.stringify(this));
    }

    public setCurrentUserId(userId: number): void {
        this.loggedInUserId = userId;
        this.save();
    }

    public setCurrentLoginToken(loginToken: string): void {
        this.loginToken = loginToken;
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
