import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ServiceApi, UserEntity, ReadingListEntity, FollowedListEntity, TagEntity } from '../serviceapi.service';
import { LoggerService } from '../logger.service';
import { LocalStorageObjectService } from '../LocalStorageObject';

@Component({
    selector: 'app-addlist',
    templateUrl: './addlist.component.html',
})
export class AddListComponent implements OnInit {
    // Bound properties.
    name: string;
    tags: string;
    description: string;

    private static maxTags: number = 20;

    constructor(
        private serviceApi: ServiceApi,
        private lso: LocalStorageObjectService,
        private router: Router,
        private logger: LoggerService) { }

    ngOnInit() {
    }

    private addList() {
        var splitTags = this.tags.split(";", AddListComponent.maxTags); // Max it out so we don't overflow.

        var listEntity = new ReadingListEntity();
        listEntity.name = this.name;
        listEntity.description = this.description;
        listEntity.userId = this.lso.getMyUserId();

        // Now get the tagIds.
        let requests = splitTags.map(item => {
            return new Promise(resolve => {
                var tagEntity = this.lso.getTagsByName()[item];
                if (tagEntity == null) {
                    tagEntity = new TagEntity();
                    tagEntity.tagName = item;
                    tagEntity.id = this.serviceApi.postTag(tagEntity);
                    this.lso.updateTag(tagEntity);
                }
                listEntity.tagIds.push(tagEntity.id);
            });
        });

        Promise.all(requests).then(() => {
            this.serviceApi.postReadingList(listEntity).subscribe(newList => {
                listEntity.id = newList.id;
                this.lso.updateReadingList(listEntity);

                // After adding the list, route us to
                this.router.navigate(['/users', this.lso.getMyUserId(), 'readinglists', listEntity.id]);
            });
        });
    }
}
