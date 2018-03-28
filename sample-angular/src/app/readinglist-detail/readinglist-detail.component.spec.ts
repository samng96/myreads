import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReadinglistDetailComponent } from './readinglist-detail.component';

describe('ReadinglistDetailComponent', () => {
  let component: ReadinglistDetailComponent;
  let fixture: ComponentFixture<ReadinglistDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ReadinglistDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReadinglistDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
