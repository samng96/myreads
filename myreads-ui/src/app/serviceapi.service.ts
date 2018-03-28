import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';

import { LoggerService } from './logger.service';

export class UserEntity {
    id: long;
    email: string;
    name: string;
    userId: string;
}

@Injectable()
export class ServiceApi {
    public static baseUrl = "http://localhost:8080/"

    constructor(
        private http: HttpClient,
        private logger: LoggerService
    ) { }

    getUser(userId: long): Observable<UserEntity> {
        var url = `${ServiceApi.baseUrl}users/${userId}`;
        return this.http.get<UserEntity>(url));
            .pipe(
                tap(_ => log(`Api: getUser(${userId})`))
            );
    }

    private log(message: string) {
        logger.log(message);
    }
}
