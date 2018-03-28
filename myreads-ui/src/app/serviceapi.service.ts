import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs/Observable';

import { LoggerService } from './logger.service';

export class UserEntity {
    id: number;
    email: string;
    name: string;
    userId: string;
    deleted: boolean;
}

@Injectable()
export class ServiceApi {
    public static baseUrl = "http://localhost:8080/"

    constructor(
        private http: HttpClient,
        private logger: LoggerService
    ) { }

    getUser(userId: number): Observable<UserEntity> {
        var url = `${ServiceApi.baseUrl}users/${userId}`;
        return this.http.get<UserEntity>(url)
            .pipe(
                tap(_ => this.log(`Api: getUser(${userId})`))
            );
    }

    private log(message: string) { this.logger.log(message); }
}
