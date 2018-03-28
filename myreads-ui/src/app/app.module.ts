import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';


import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { AppRoutingModule } from './/app-routing.module';
import { UsersComponent } from './users/users.component';
import { ReadinglistsComponent } from './readinglists/readinglists.component';
import { FollowedlistsComponent } from './followedlists/followedlists.component';
import { TagsComponent } from './tags/tags.component';


@NgModule({
    declarations: [
        AppComponent,
        LoginComponent,
        UsersComponent,
        ReadinglistsComponent,
        FollowedlistsComponent,
        TagsComponent
    ],
    imports: [
        BrowserModule,
        AppRoutingModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule { }
