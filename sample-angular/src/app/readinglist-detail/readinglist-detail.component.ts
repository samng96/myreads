import { Component, OnInit, Input } from '@angular/core';
import { Readinglist } from '../readinglist';
import { ReadinglistService } from '../readinglist.service';
import { Location } from '@angular/common';

@Component({
  selector: 'app-readinglist-detail',
  templateUrl: './readinglist-detail.component.html',
  styleUrls: ['./readinglist-detail.component.css']
})
export class ReadinglistDetailComponent implements OnInit {

    @Input() list: Readinglist

    constructor(
        private location: Location,
        private readingListService: ReadinglistService) { }

    ngOnInit() { }

    goBack(): void {
        this.location.back();
    }

    save(): void {
        this.readingListService.updateReadingList(this.list)
            .subscribe(() => this.goBack());
    }
}
