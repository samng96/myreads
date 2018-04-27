import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/mergeMap';

import { ServiceApi } from '../serviceapi.service';
import { TagEntity, UserEntity, ReadingListEntity, FollowedListEntity, ReadingListElementEntity } from '../entities';
import { LoggerService } from '../logger.service';
import { LocalStorageObjectService } from '../LocalStorageObject';

@Component({
    selector: 'app-tags',
    templateUrl: './tags.component.html',
})
export class TagsComponent implements OnInit {
    tag: TagEntity; // This is for displaying a single tag.
    tags: TagEntity[]; // This is for the display.

    constructor(
        private lso: LocalStorageObjectService,
        private route: ActivatedRoute,
        private serviceApi: ServiceApi,
        private router: Router,
        private logger: LoggerService
    ) { }

    onSelectTag(tag: TagEntity) {
        this.router.navigate(['tags', tag.id]);
    }

    ngOnInit() {
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

                // TODO: We might want to list all things associated with the tag here. We'll want to
                // TODO: collate of course, since it might be super long. Maybe show the first x?
                // TODO: In general, we'll need to eventually handle uber huge lists of things, but for now,
                // TODO: we can just give it a pass.
            })
        }
    }
}
