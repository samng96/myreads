import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, tap } from 'rxjs/operators';
import { LocalStorageObjectService } from './LocalStorageObject';
import { ReadingListElementEntity } from './entities';

import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { LoggerService } from './logger.service';

export class ReadingListElementExtras {
    title: string;
    description: string;
    image: string;
    url: string;
}

export class LinkPreviewResultObject {
    title: string;
    description: string;
    image: string;
    url: string;
    error: number;
}

@Injectable()
export class ExtrasHelpers {
    private linkPreviewApiKey: string = "5aeaa317b64a2ae9950b87ffc3b372739ad468bb2a676";

    private maxTitleLength: number = 75;
    private maxDescriptionLength: number = 150;
    private numTagStyles: number = 6;

    constructor(
        private http: HttpClient,
        private lso: LocalStorageObjectService,
        private logger: LoggerService
    ) { }

    public getTagStyle(tagId: number): string {
        return `tagcolor${tagId % this.numTagStyles}`;
    }

    public getRleExtra(rle: ReadingListElementEntity): Observable<LinkPreviewResultObject> {
        // TODO: Looks like this API throttles - figure out how we can delay load.
        var url = `http://api.linkpreview.net/?key=${this.linkPreviewApiKey}&q=${rle.link}`
        return this.http.get<LinkPreviewResultObject>(url)
            .pipe(
                tap(_ => this.log(`linkPreview(${rle.link})`)),
                catchError(this.handleError("linkPreview", null))
            );
    }
    public pickDescription(rle: ReadingListElementEntity, truncate: boolean = true): string {
        var desc;
        if (this.lso.getRleExtras()[rle.id] != null) {
            desc = this.lso.getRleExtras()[rle.id].description;
        }
        else {
            desc = rle.description;
        }
        if (truncate && (desc.length > this.maxDescriptionLength)) {
            return `${desc.substring(0, this.maxDescriptionLength)} ...`;
        }
        return desc;
    }
    public pickTitle(rle: ReadingListElementEntity, truncate: boolean = true): string {
        if (this.lso.getRleExtras()[rle.id] != null) {
            var title = this.lso.getRleExtras()[rle.id].title;
            if (truncate && (title.length > this.maxTitleLength)) {
                return `${title.substring(0, this.maxTitleLength)} ...`;
            }
            return title;
        }
        return rle.name;
    }
    public getImageUrl(rle: ReadingListElementEntity): string {
        if (this.extractRootDomain(rle.link) == "amazon.com") {
            var productId = this.extractAmazonProductId(rle.link);

            if (productId != null) {
                return `http://ws-na.amazon-adsystem.com/widgets/q?ASIN=${productId}&ServiceVersion=20070822&ID=AsinImage&WS=1&Format`;
            }
        }
        if (this.lso.getRleExtras()[rle.id] != null) {
            return this.lso.getRleExtras()[rle.id].image;
        }
        return "";
    }
    public getLink(rle: ReadingListElementEntity): string {
        if (this.extractRootDomain(rle.link) == "amazon.com") {
            var productId = this.extractAmazonProductId(rle.link);

            if (productId != null) {
                return `https://www.amazon.com/gp/product/${productId}/ref=as_li_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=0451495861&linkCode=as2&tag=samng96-20&linkId=0e3bf9c7ea3f23b726971dc8cfd7ba8d`;
            }
        }
        return rle.link;
    }
    public extractRootDomain(url: string): string {
        var domain = this.extractHostname(url);
        var splitArr = domain.split('.');
        var arrLen = splitArr.length;

        //extracting the root domain here
        //if there is a subdomain
        if (arrLen > 2) {
            domain = splitArr[arrLen - 2] + '.' + splitArr[arrLen - 1];
            //check to see if it's using a Country Code Top Level Domain (ccTLD) (i.e. ".me.uk")
            if (splitArr[arrLen - 2].length == 2 && splitArr[arrLen - 1].length == 2) {
                //this is using a ccTLD
                domain = splitArr[arrLen - 3] + '.' + domain;
            }
        }
        return domain;
    }

    private extractHostname(url: string): string {
        var hostname;
        //find & remove protocol (http, ftp, etc.) and get hostname

        if (url.indexOf("://") > -1) {
            hostname = url.split('/')[2];
        }
        else {
            hostname = url.split('/')[0];
        }

        //find & remove port number
        hostname = hostname.split(':')[0];
        //find & remove "?"
        hostname = hostname.split('?')[0];

        return hostname;
    }
    private extractAmazonProductId(url: string): string {
        return url.toLowerCase().split("dp/")[1].split("/")[0];
    }

    private log(message: string) { this.logger.log(`[Users]: ${message}`); }
    private handleError<T>(operation: string, result?:T) {
        return (error: any): Observable<T> => {
            this.log(`${operation} failed: ${error.message}`);
            return of(result as T);
        }
    }
}
