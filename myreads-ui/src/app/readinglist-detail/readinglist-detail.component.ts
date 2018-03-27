import { Component, OnInit, Input } from '@angular/core';
import { Readinglist } from '../readinglist';

@Component({
  selector: 'app-readinglist-detail',
  templateUrl: './readinglist-detail.component.html',
  styleUrls: ['./readinglist-detail.component.css']
})
export class ReadinglistDetailComponent implements OnInit {

    @Input() list: Readinglist

    constructor() { }

    ngOnInit() { }
}
