import { Injectable } from '@angular/core';

@Injectable()
export class LoggerService {
    messages: string[] = [];

    log(message: string) {
        console.log(message);
        this.messages.push(message);
    }

    clear() {
        this.messages = [];
    }
}
