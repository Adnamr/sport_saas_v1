import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { InputComponent } from './input.component';

describe('InputComponent', () => {
  let component: InputComponent;
  let fixture: ComponentFixture<InputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InputComponent, FormsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(InputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have default type as text', () => {
    expect(component.type).toBe('text');
  });

  it('should display label when provided', () => {
    component.label = 'Test Label';
    fixture.detectChanges();
    const label = fixture.nativeElement.querySelector('.label');
    expect(label.textContent).toContain('Test Label');
  });

  it('should show required indicator when required', () => {
    component.label = 'Test';
    component.required = true;
    fixture.detectChanges();
    const required = fixture.nativeElement.querySelector('.required');
    expect(required).toBeTruthy();
  });

  it('should display error message when error is provided', () => {
    component.error = 'This field is required';
    fixture.detectChanges();
    const errorMessage = fixture.nativeElement.querySelector('.error-message');
    expect(errorMessage.textContent).toContain('This field is required');
  });

  it('should display hint when provided and no error', () => {
    component.hint = 'Enter your email';
    component.error = '';
    fixture.detectChanges();
    const hint = fixture.nativeElement.querySelector('.hint');
    expect(hint.textContent).toContain('Enter your email');
  });

  it('should not display hint when error is present', () => {
    component.hint = 'Enter your email';
    component.error = 'Invalid email';
    fixture.detectChanges();
    const hint = fixture.nativeElement.querySelector('.hint');
    expect(hint).toBeFalsy();
  });

  it('should update value on input', () => {
    const input = fixture.nativeElement.querySelector('input');
    input.value = 'test value';
    input.dispatchEvent(new Event('input'));
    expect(component.value).toBe('test value');
  });

  it('should call onChange when value changes', () => {
    const onChangeSpy = jasmine.createSpy('onChange');
    component.registerOnChange(onChangeSpy);

    const input = fixture.nativeElement.querySelector('input');
    input.value = 'new value';
    input.dispatchEvent(new Event('input'));

    expect(onChangeSpy).toHaveBeenCalledWith('new value');
  });

  it('should call onTouched when input loses focus', () => {
    const onTouchedSpy = jasmine.createSpy('onTouched');
    component.registerOnTouched(onTouchedSpy);

    const input = fixture.nativeElement.querySelector('input');
    input.dispatchEvent(new Event('blur'));

    expect(onTouchedSpy).toHaveBeenCalled();
  });

  it('should implement ControlValueAccessor writeValue', () => {
    component.writeValue('test');
    expect(component.value).toBe('test');
  });

  it('should handle null/undefined in writeValue', () => {
    component.writeValue(null as any);
    expect(component.value).toBe('');
  });

  it('should set disabled state', () => {
    component.setDisabledState(true);
    expect(component.disabled).toBe(true);

    component.setDisabledState(false);
    expect(component.disabled).toBe(false);
  });

  it('should add has-error class when error is present', () => {
    component.error = 'Error message';
    fixture.detectChanges();
    const wrapper = fixture.nativeElement.querySelector('.input-wrapper');
    expect(wrapper.classList.contains('has-error')).toBe(true);
  });
});
