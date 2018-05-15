import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';

import { LoginComponent } from './routes/login/login.component';
import { UsersComponent } from './routes/users/users.component';
import { ReadingListsComponent } from './routes/readinglists/readinglists.component';
import { TagsComponent } from './routes/tags/tags.component';
import { ReadingListElementsComponent } from './routes/readinglistelements/readinglistelements.component';
import { SearchComponent } from './routes/search/search.component';

import { LoggerService } from './utilities/logger.service';
import { ServiceApi } from './utilities/serviceapi.service';
import { LocalStorageObjectService } from './utilities/localstorageobject';
import { ExtrasHelpers } from './utilities/entityextras';

import { DebugComponent } from './debug/debug.component';
import { NavComponent } from './nav/nav.component';
import { ToolbarComponent } from './nav/toolbar.component';

import { ListOfElementsComponent, ListOfElementsCommunicationObject } from './components/listofelements.component';


@NgModule({
    declarations: [
        AppComponent,
        LoginComponent,
        UsersComponent,
        ReadingListsComponent,
        TagsComponent,
        ReadingListElementsComponent,
        DebugComponent,
        NavComponent,
        ToolbarComponent,
        ListOfElementsComponent,
        SearchComponent
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        FormsModule,
        HttpClientModule
    ],
    providers: [ LoggerService, ServiceApi, LocalStorageObjectService, ExtrasHelpers, ListOfElementsCommunicationObject ],
    bootstrap: [ AppComponent ]
})
export class AppModule { }
