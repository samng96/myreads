import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './login/login.component';
import { UsersComponent } from './users/users.component';
import { ReadingListsComponent } from './readinglists/readinglists.component';
import { ReadingListElementsComponent } from './readinglistelements/readinglistelements.component';
import { TagsComponent } from './tags/tags.component';

const routes: Routes = [
    // Basic service routes.
    { path: 'login', component: LoginComponent },
    { path: 'users/:userId', component: UsersComponent },
    { path: 'users/:userId/readinglists/:listId', component: ReadingListsComponent },
    { path: 'users/:userId/readinglistelements/:elementId', component: ReadingListElementsComponent },
    //{ path: 'followedlists/:listId', component: FollowedListsComponent },
    { path: 'tags/:tagId', component: TagsComponent },
    { path: 'tags', component: TagsComponent },

    { path: '', redirectTo: '/login', pathMatch: 'full' }
];

@NgModule({
    imports: [ RouterModule.forRoot(routes) ],
    exports: [ RouterModule ]
})
export class AppRoutingModule { }
