import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { ReadinglistsComponent } from './readinglists/readinglists.component';
import { ReadinglistDetailComponent } from './readinglist-detail/readinglist-detail.component';
import { MessagesComponent } from './messages/messages.component';
import { MessageService } from './message.service';
import { ReadinglistService } from './readinglist.service';


@NgModule({
  declarations: [
    AppComponent,
    ReadinglistsComponent,
    ReadinglistDetailComponent,
    MessagesComponent
  ],
  imports: [
    BrowserModule,
    FormsModule
  ],
  providers: [MessageService, ReadinglistService],
  bootstrap: [AppComponent]
})
export class AppModule { }
