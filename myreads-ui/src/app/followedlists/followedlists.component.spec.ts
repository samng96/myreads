import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FollowedListsComponent } from './followedlists.component';

describe('FollowedListsComponent', () => {
  let component: FollowedListsComponent;
  let fixture: ComponentFixture<FollowedListsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FollowedListsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FollowedListsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
