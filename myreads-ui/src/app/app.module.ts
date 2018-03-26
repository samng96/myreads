import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';


import { AppComponent } from './app.component';
import { ReadinglistsComponent } from './readinglists/readinglists.component';


@NgModule({
  declarations: [
    AppComponent,
    ReadinglistsComponent
  ],
  imports: [
    BrowserModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
