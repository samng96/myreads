import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/mergeMap';

import { ServiceApi, TagEntity, UserEntity, ReadingListEntity, FollowedListEntity, ReadingListElementEntity } from '../serviceapi.service';
import { LoggerService } from '../logger.service';
import { LocalStorageObject } from '../localstorageobject';

@Component({
    selector: 'app-tags',
    templateUrl: './tags.component.html',
    styleUrls: ['./tags.component.css']
})
export class TagsComponent implements OnInit {
    lso: LocalStorageObject;

    tag: TagEntity; // This is for displaying a single tag.
    tags: TagEntity[]; // This is for the display.

    constructor(
        private route: ActivatedRoute,
        private serviceApi: ServiceApi,
        private router: Router,
        private logger: LoggerService
    ) { }

    onSelectTag(tag: TagEntity) {
        this.router.navigate(['tags', tag.id]);
    }

    ngOnInit() {
        this.lso = LocalStorageObject.load();
        var tagId = +this.route.snapshot.paramMap.get('tagId');
        if (tagId == 0) {
            // No Id means we just load up all tags and display em.
            this.serviceApi.getTags().subscribe(tags => {
                this.lso.updateTags(tags);
                this.tags = tags;
            });
        }
        else {
            this.serviceApi.getTag(tagId).subscribe(tag => {
                this.lso.updateTag(tag);
                this.tag = tag;

                // TODO: We might want to list all things associated with the tag here.
            })
        }
    }
}
