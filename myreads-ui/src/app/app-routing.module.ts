import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './login/login.component';
import { UsersComponent } from './users/users.component';
import { ReadingListsComponent } from './readinglists/readinglists.component';
import { ReadingListElementsComponent } from './readinglistelements/readinglistelements.component';
import { FollowedListsComponent } from './followedlists/followedlists.component';
import { TagsComponent } from './tags/tags.component';

const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'users/:userId', component: UsersComponent },
    { path: 'users/:userId/readinglists/:listId', component: ReadingListsComponent },
    { path: 'users/:userId/readinglistelements/:elementId', component: ReadingListElementsComponent },
    { path: 'users/:userId/followedlists/:followedId', component: FollowedListsComponent },
    { path: 'users/:userId/tags/:tagId', component: TagsComponent },
    { path: '', redirectTo: '/login', pathMatch: 'full' }
];

@NgModule({
    imports: [ RouterModule.forRoot(routes) ],
    exports: [ RouterModule ]
})
export class AppRoutingModule { }
