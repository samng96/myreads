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
    read: boolean;
    favorite: boolean;
    description: string;
    link: string;
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
    lastModified: Date;
}
