import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReadinglistsComponent } from './readinglists.component';

describe('ReadinglistsComponent', () => {
  let component: ReadinglistsComponent;
  let fixture: ComponentFixture<ReadinglistsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ReadinglistsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReadinglistsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
