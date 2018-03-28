import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReadingListsComponent } from './readinglists.component';

describe('ReadingListsComponent', () => {
  let component: ReadingListsComponent;
  let fixture: ComponentFixture<ReadingListsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ReadingListsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReadingListsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
