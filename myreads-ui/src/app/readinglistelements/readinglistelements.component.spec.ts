import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReadingListElementsComponent } from './readinglistelements.component';

describe('ReadingListElementsComponent', () => {
  let component: ReadingListElementsComponent;
  let fixture: ComponentFixture<ReadingListElementsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ReadingListElementsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReadingListElementsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
