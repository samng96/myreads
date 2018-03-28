import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ReadinglistsComponent } from './readinglists/readinglists.component';

const routes: Routes = [
    { path: 'readinglists', component: ReadinglistsComponent }
];

@NgModule({
    imports: [ RouterModule.forRoot(routes) ],
    exports: [ RouterModule ]
})
export class AppRoutingModule { }
