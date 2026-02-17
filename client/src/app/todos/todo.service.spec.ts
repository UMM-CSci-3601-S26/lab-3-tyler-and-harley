import { HttpClient, HttpParams, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed, waitForAsync } from '@angular/core/testing';
import { of } from 'rxjs';
import { Todo } from './todo';
import { TodoService } from './todo.service';

describe('TodoService', () => {
  // A small collection of test todos
  const testTodos: Todo[] = [
    {
      _id: 'snake_id',
      owner: 'snake',
      category: 'reptile',
      body: 'a snake is a reptile',
      status: 'incomplete'
    },
    {
      _id: 'pat_id',
      owner: 'pat',
      category: 'games',
      body: 'something interesting about games played by Pat',
      status: 'complete'
    },
    {
      _id: 'kermit_id',
      owner: 'kermit',
      category: 'frog',
      body: 'something interesting about kermit',
      status: 'complete'
    }
  ];

  let todoService: TodoService;
  // These are used to mock the HTTP requests so that we (a) don't have to
  // have the server running and (b) we can check exactly which HTTP
  // requests were made to ensure that we're making the correct requests.
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    // Set up the mock handling of the HTTP requests
    TestBed.configureTestingModule({
      imports: [],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    // Construct an instance of the service with the mock
    // HTTP client.
    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
    todoService = TestBed.inject(TodoService);
  });

  afterEach(() => {
    // After every test, assert that there are no more pending requests.
    httpTestingController.verify();
  });

  describe('When getTodos() is called with no parameters', () => {
    it('calls `api/todos`', waitForAsync(() => {

      const mockedMethod = spyOn(httpClient, 'get').and.returnValue(of(testTodos));


      todoService.getTodos().subscribe(() => {

        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);
        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(todoService.todoUrl, { params: new HttpParams() });
      });
    }));
  });

  describe('When getTodoById() is given an ID', () => {

    it('calls api/todos/id with the correct ID', waitForAsync(() => {
      // We're just picking a Todo "at random" from our little
      // set of Todos up at the top.
      const targetTodo: Todo = testTodos[1];
      const targetId: string = targetTodo._id;

      // Mock the `httpClient.get()` method so that instead of making an HTTP request
      // it just returns one todo from our test data
      const mockedMethod = spyOn(httpClient, 'get').and.returnValue(of(targetTodo));

      // Call `todoService.getTodo()` and confirm that the correct call has
      // been made with the correct arguments.

      todoService.getTodoById(targetId).subscribe(() => {

        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);
        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(`${todoService.todoUrl}/${targetId}`);
      });
    }));
  });

  describe('When getTodos() is called with parameters, it correctly forms the HTTP request (Javalin/Server filtering)', () => {


    it('correctly calls api/todos with filter parameter \'complete\'', () => {
      const mockedMethod = spyOn(httpClient, 'get').and.returnValue(of(testTodos));

      todoService.getTodos({ status: 'complete' }).subscribe(() => {
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);
        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(todoService.todoUrl, { params: new HttpParams().set('status', 'complete') });
      });
    });
  });

  describe('Adding a todo using `addTodo()`', () => {
    it('talks to the right endpoint and is called once', waitForAsync(() => {
      const todo_id = 'pat_id';
      const expected_http_response = { id: todo_id } ;

      // Mock the `httpClient.addTodo()` method, so that instead of making an HTTP request,
      // it just returns our expected HTTP response.
      const mockedMethod = spyOn(httpClient, 'post')
        .and
        .returnValue(of(expected_http_response));

      todoService.addTodo(testTodos[1]).subscribe((new_todo_id) => {
        expect(new_todo_id).toBe(todo_id);
        expect(mockedMethod)
          .withContext('one call')
          .toHaveBeenCalledTimes(1);
        expect(mockedMethod)
          .withContext('talks to the correct endpoint')
          .toHaveBeenCalledWith(todoService.todoUrl, testTodos[1]);
      });
    }));
  });
});

