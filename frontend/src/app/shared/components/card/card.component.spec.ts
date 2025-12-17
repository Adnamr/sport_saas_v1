import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CardComponent } from './card.component';

describe('CardComponent', () => {
  let component: CardComponent;
  let fixture: ComponentFixture<CardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CardComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(CardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display title when provided', () => {
    component.title = 'Test Card Title';
    fixture.detectChanges();
    const title = fixture.nativeElement.querySelector('.card-title');
    expect(title.textContent).toContain('Test Card Title');
  });

  it('should not display header when no title', () => {
    component.title = '';
    component.headerTemplate = false;
    fixture.detectChanges();
    const header = fixture.nativeElement.querySelector('.card-header');
    expect(header).toBeFalsy();
  });

  it('should add hoverable class when hoverable is true', () => {
    component.hoverable = true;
    fixture.detectChanges();
    const card = fixture.nativeElement.querySelector('.card');
    expect(card.classList.contains('hoverable')).toBe(true);
  });

  it('should not have hoverable class by default', () => {
    const card = fixture.nativeElement.querySelector('.card');
    expect(card.classList.contains('hoverable')).toBe(false);
  });

  it('should add no-padding class to body when noPadding is true', () => {
    component.noPadding = true;
    fixture.detectChanges();
    const body = fixture.nativeElement.querySelector('.card-body');
    expect(body.classList.contains('no-padding')).toBe(true);
  });

  it('should have padding by default', () => {
    const body = fixture.nativeElement.querySelector('.card-body');
    expect(body.classList.contains('no-padding')).toBe(false);
  });

  it('should have default values', () => {
    expect(component.title).toBe('');
    expect(component.hoverable).toBe(false);
    expect(component.noPadding).toBe(false);
    expect(component.headerTemplate).toBe(false);
    expect(component.footerTemplate).toBe(false);
  });
});
