import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './routes/login/login.component';
import { UsersComponent } from './routes/users/users.component';
import { ReadingListsComponent } from './routes/readinglists/readinglists.component';
import { ReadingListElementsComponent } from './routes/readinglistelements/readinglistelements.component';
import { TagsComponent } from './routes/tags/tags.component';

const routes: Routes = [
    // Basic service routes.
    { path: 'login', component: LoginComponent },
    { path: 'users', component: UsersComponent },
    { path: 'users/:userId', component: UsersComponent },
    { path: 'users/:userId/readinglists/:listId', component: ReadingListsComponent },
    { path: 'users/:userId/readinglistelements/:elementId', component: ReadingListElementsComponent },
    { path: 'tags/:tagId', component: TagsComponent },
    { path: 'tags', component: TagsComponent },
    { path: 'unread', component: ReadingListsComponent },
    { path: 'favorites', component: ReadingListsComponent },

    { path: '', redirectTo: '/login', pathMatch: 'full' }
];

@NgModule({
    imports: [ RouterModule.forRoot(routes) ],
    exports: [ RouterModule ]
})
export class AppRoutingModule { }
