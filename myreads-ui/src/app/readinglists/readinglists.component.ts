import { Component, OnInit } from '@angular/core';
import { ReadingList } from '../readinglist';

@Component({
  selector: 'app-readinglists',
  templateUrl: './readinglists.component.html',
  styleUrls: ['./readinglists.component.css']
})
export class ReadinglistsComponent implements OnInit {

    listData: ReadingList = {
        id: 1,
        name: "Test Reading List",
        description: "This is my test reading list - we'll dynamically load stuff here later",
        userId: 96,
        tagIds: [1, 2, 3],
        readingListElementIds: [10, 11, 12]
    };

  constructor() { }

  ngOnInit() {
  }

}
