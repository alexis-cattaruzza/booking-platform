import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BusinessProfileComponent } from './business-profile.component';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { BusinessService } from '../../services/business.service';
import { of } from 'rxjs';

describe('BusinessProfileComponent', () => {
  let component: BusinessProfileComponent;
  let fixture: ComponentFixture<BusinessProfileComponent>;

  const mockBusiness = {
    businessName: 'Test Business',
    description: 'Test Description',
    category: 'HAIRDRESSER',
    address: '123 Test St',
    city: 'Test City',
    postalCode: '12345',
    phone: '+1234567890',
    email: 'test@example.com',
    logoUrl: ''
  };

  const mockBusinessService = {
    getMyBusiness: () => of(mockBusiness),
    updateMyBusiness: (request: any) => of({ ...mockBusiness, ...request })
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        BusinessProfileComponent,
        ReactiveFormsModule
      ],
      providers: [
        provideHttpClientTesting(), // ✅ replaces deprecated HttpClientTestingModule
        { provide: BusinessService, useValue: mockBusinessService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BusinessProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load business data on init', () => {
    expect(component.profileForm.value.businessName).toBe('Test Business');
    expect(component.profileForm.value.email).toBe('test@example.com');
  });
});
