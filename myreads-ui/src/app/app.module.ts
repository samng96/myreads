import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { AppRoutingModule } from './app-routing.module';
import { UsersComponent } from './users/users.component';
import { ReadingListsComponent } from './readinglists/readinglists.component';
import { TagsComponent } from './tags/tags.component';
import { ReadingListElementsComponent } from './readinglistelements/readinglistelements.component';
import { AddListElementComponent } from './create/addlistelement.component';

import { LoggerService } from './logger.service';
import { ServiceApi } from './serviceapi.service';
import { DebugComponent } from './debug/debug.component';
import { NavComponent } from './nav/nav.component';
import { LocalStorageObjectService } from './localstorageobject';

import { ExtrasHelpers } from './entityextras';


@NgModule({
    declarations: [
        AppComponent,
        LoginComponent,
        UsersComponent,
        ReadingListsComponent,
        TagsComponent,
        ReadingListElementsComponent,
        AddListElementComponent,
        DebugComponent,
        NavComponent
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        FormsModule,
        HttpClientModule
    ],
    providers: [ LoggerService, ServiceApi, LocalStorageObjectService, ExtrasHelpers ],
    bootstrap: [ AppComponent ]
})
export class AppModule { }
