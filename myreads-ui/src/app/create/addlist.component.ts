import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ServiceApi } from '../serviceapi.service';
import { UserEntity, ReadingListEntity, FollowedListEntity, TagEntity } from '../entities';
import { LoggerService } from '../logger.service';
import { LocalStorageObjectService } from '../localstorageobject';

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

    addList() {
        var splitTags = this.tags.split(";", AddListComponent.maxTags); // Max it out so we don't overflow.

        var listEntity = new ReadingListEntity();
        listEntity.name = this.name;
        listEntity.description = this.description;
        listEntity.userId = this.lso.getMyUserId();
        listEntity.tagIds = [];

        // Now get the tagIds.
        let requests = splitTags.map(item => {
            return new Promise(resolve => {
                var trimmed = item.trim();
                var tagEntity = this.lso.getTagsByName()[trimmed];
                if (tagEntity == null) {
                    this.log(`loading tag ${trimmed}`)
                    tagEntity = new TagEntity();
                    tagEntity.tagName = trimmed;
                    this.serviceApi.postTag(tagEntity).subscribe(tagId => {
                        tagEntity.id = tagId;
                        this.lso.updateTag(tagEntity);
                        listEntity.tagIds.push(tagEntity.id);

                        resolve();
                    });
                }
                else {
                    listEntity.tagIds.push(tagEntity.id);
                    resolve();
                }
            });
        });

        Promise.all(requests).then(() => {
            this.log(`promise complete, updating reading list ${listEntity.name}`)
            this.serviceApi.postReadingList(listEntity).subscribe(listId => {
                listEntity.id = listId;
                this.lso.addReadingList(listEntity);

                // After adding the list, route us to the list.
                this.router.navigate(['/users', this.lso.getMyUserId(), 'readinglists', listEntity.id]);
            });
        });
    }
    private log(message: string) { this.logger.log(`[AddList]: ${message}`); }
}
