import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { AppRoutingModule } from './/app-routing.module';
import { UsersComponent } from './users/users.component';
import { ReadingListsComponent } from './readinglists/readinglists.component';
import { FollowedListsComponent } from './followedlists/followedlists.component';
import { TagsComponent } from './tags/tags.component';
import { ReadingListElementsComponent } from './readinglistelements/readinglistelements.component';


@NgModule({
    declarations: [
        AppComponent,
        LoginComponent,
        UsersComponent,
        ReadingListsComponent,
        FollowedListsComponent,
        TagsComponent,
        ReadingListElementsComponent
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        FormsModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule { }
