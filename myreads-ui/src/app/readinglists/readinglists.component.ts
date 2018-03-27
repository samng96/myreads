import { Component, OnInit } from '@angular/core';
import { Readinglist } from '../readinglist';
import { ReadinglistService } from '../readinglist.service';

@Component({
  selector: 'app-readinglists',
  templateUrl: './readinglists.component.html',
  styleUrls: ['./readinglists.component.css']
})
export class ReadinglistsComponent implements OnInit {
    readingLists: Readinglist[];
    selectedList: Readinglist;

    constructor(private readingListService: ReadinglistService) { }

    ngOnInit() {
        this.getLists();
    }

    onSelect(list : Readinglist): void {
        this.selectedList = list;
    }

    getLists(): void {
        this.readingListService.getLists().subscribe(lists => this.readingLists = lists);
    }
}
