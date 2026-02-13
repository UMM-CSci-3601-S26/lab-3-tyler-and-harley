import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { TodoCardComponent } from './todo-card.component';
import { Todo } from './todo';

describe('TodoCardComponent', () => {
  let component: TodoCardComponent;
  let fixture: ComponentFixture<TodoCardComponent>;
  let expectedTodo: Todo;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        TodoCardComponent
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TodoCardComponent);
    component = fixture.componentInstance;
    expectedTodo = {
      _id: 'Barney_id',
      owner: 'PBS',
      category: 'TheDinosaur',
      body: 'buy apples',
      status: 'complete',
    };
    fixture.componentRef.setInput('todo', expectedTodo);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should be associated with the correct todo', () => {
    expect(component.todo()).toEqual(expectedTodo);
  });

  it('should be the todo owned by PBS', () => {
    expect(component.todo().owner).toEqual('PBS');
  });
});
