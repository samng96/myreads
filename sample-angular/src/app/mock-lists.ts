
import { Readinglist } from './readingList';

export const READINGLISTS: Readinglist[] = [
    {
        id: 1,
        name: "Test Reading List",
        description: "This is my test reading list - we'll dynamically load stuff here later",
        userId: 96,
        tagIds: [1, 2, 3],
        readingListElementIds: [10, 11, 12]
    },
    {
        id: 2,
        name: "Second Reading List",
        description: "Description",
        userId: 96,
        tagIds: [1, 2],
        readingListElementIds: [11, 12]
    },
    {
        id: 3,
        name: "Third Reading List",
        description: "Description",
        userId: 96,
        tagIds: [2, 3],
        readingListElementIds: [11, 12]
    },
    {
        id: 4,
        name: "First Reading List for user 2",
        description: "Description",
        userId: 89,
        tagIds: [2],
        readingListElementIds: [21, 22]
    },
];
