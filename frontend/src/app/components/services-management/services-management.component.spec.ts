import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { ServicesManagementComponent } from './services-management.component';

describe('ServicesManagementComponent', () => {
  let component: ServicesManagementComponent;
  let fixture: ComponentFixture<ServicesManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ServicesManagementComponent
      , HttpClientTestingModule]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ServicesManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
