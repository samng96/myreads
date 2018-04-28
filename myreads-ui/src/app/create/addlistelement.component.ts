import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ServiceApi } from '../serviceapi.service';
import { UserEntity, ReadingListElementEntity, FollowedListEntity, TagEntity } from '../entities';
import { LoggerService } from '../logger.service';
import { LocalStorageObjectService } from '../LocalStorageObject';

@Component({
    selector: 'app-addlistelement',
    templateUrl: './addlistelement.component.html',
})
export class AddListElementComponent implements OnInit {
    // Bound properties.
    name: string;
    tags: string;
    listIds: string;
    description: string;
    amazonLink: string;

    private static maxTags: number = 20;

    constructor(
        private serviceApi: ServiceApi,
        private lso: LocalStorageObjectService,
        private router: Router,
        private logger: LoggerService) { }

    ngOnInit() {
    }

    private addListElement() {

        var rleEntity = new ReadingListElementEntity();
        rleEntity.name = this.name;
        rleEntity.description = this.description;
        rleEntity.userId = this.lso.getMyUserId();
        rleEntity.amazonLink = this.amazonLink;
        rleEntity.tagIds = [];
        // Get the list ids. This is temporary while we have crappy UI to not allow selection.
        rleEntity.listIds = this.listIds.split(";", AddListElementComponent.maxTags).map(Number);

        // Now get the tagIds.
        var requests;
        if (this.tags != null) {
            var splitTags = this.tags.split(";", AddListElementComponent.maxTags); // Max it out so we don't overflow.
            requests = splitTags.map(item => {
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
                            rleEntity.tagIds.push(tagEntity.id);

                            resolve();
                        });
                    }
                    else {
                        rleEntity.tagIds.push(tagEntity.id);
                        resolve();
                    }
                });
            });
        }
        else {
            requests = Promise.resolve();
        }

        Promise.all(requests).then(() => {
            this.log(`promise complete, updating reading list ${rleEntity.name}`)
            this.serviceApi.postReadingListElement(rleEntity).subscribe(rleId => {
                rleEntity.id = rleId;
                this.lso.updateReadingListElement(rleEntity);

                var rleIds = [];
                rleIds.push(rleId);

                // Now we need to go through each list and add the element to that list, and
                // when we complete all of those adds, reroute us.
                let listUpdates = rleEntity.listIds.map(listId => {
                    return new Promise(resolve => {
                        this.serviceApi.addReadingListElementToReadingList(
                            rleEntity.userId, listId, rleIds).subscribe(() => resolve());
                    });
                });

                Promise.all(listUpdates).then(() => {
                    // After adding the list, route us to the element.
                    this.router.navigate(['/users', this.lso.getMyUserId(), 'readinglistelements', rleEntity.id]);
                });
            });
        });
    }
    private log(message: string) { this.logger.log(`[AddListElement]: ${message}`); }
}
