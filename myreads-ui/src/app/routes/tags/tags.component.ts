import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/mergeMap';

import { ServiceApi } from '../../utilities/serviceapi.service';
import { TagEntity, UserEntity, ReadingListEntity, FollowedListEntity, ReadingListElementEntity } from '../../utilities/entities';
import { LoggerService } from '../../utilities/logger.service';
import { LocalStorageObjectService } from '../../utilities/localstorageobject';
import { ExtrasHelpers } from '../../utilities/entityextras';

@Component({
    selector: 'app-tags',
    templateUrl: './tags.component.html',
})
export class TagsComponent implements OnInit {
    tag: TagEntity = null; // This is for displaying a single tag.
    readingLists: ReadingListEntity[] = null;
    readingListElements: ReadingListElementEntity[] = null;
    tags: TagEntity[] = null; // This is for the display.

    constructor(
        private lso: LocalStorageObjectService,
        private route: ActivatedRoute,
        private serviceApi: ServiceApi,
        private helper: ExtrasHelpers,
        private router: Router,
        private logger: LoggerService
    ) { }

    onSelectTag(tag: TagEntity): void {
        this.router.navigate(['tags', tag.id]);
    }
    onSelectRl(rl: ReadingListEntity): void {
        this.router.navigate(['users', rl.userId, 'readinglists', rl.id])
    }
    onSelectRle(rle: ReadingListElementEntity): void {
        this.router.navigate(['users', rle.userId, 'readinglistelements', rle.id])
    }

    ngOnInit() {
        var tagId = +this.route.snapshot.paramMap.get('tagId');
        if (tagId == 0) {
            // No Id means we just load up all tags and display em.
            this.serviceApi.getTagsByUser(this.lso.getMyUserId()).subscribe(tags => {
                if (tags == null) { return; }

                this.tags = tags.sort((a, b) => +(a.tagName > b.tagName));
            });
        }
        else {
            this.serviceApi.getTag(tagId).subscribe(tag => {
                if (tag == null) { return; }

                this.tag = tag;

                var promises = [];
                promises.push(new Promise(resolve => {
                    this.serviceApi.getReadingListsByTag(this.lso.getMyUserId(), tagId).subscribe(rls => {
                        this.readingLists = rls;
                        resolve();
                    });
                }));
                promises.push(new Promise(resolve => {
                    this.serviceApi.getReadingListElementsByTag(this.lso.getMyUserId(), tagId).subscribe(rles => {
                        this.readingListElements = rles;
                        resolve();
                    });
                }));
            })
        }
    }
}
