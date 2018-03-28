import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FollowedlistsComponent } from './followedlists.component';

describe('FollowedlistsComponent', () => {
  let component: FollowedlistsComponent;
  let fixture: ComponentFixture<FollowedlistsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FollowedlistsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FollowedlistsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
