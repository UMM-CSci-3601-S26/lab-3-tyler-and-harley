import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Todo, TodoStatus } from './todo';

@Injectable({
  providedIn: 'root'
})
export class TodoService {
  private httpClient = inject(HttpClient);
  readonly todoUrl: string = `${environment.apiUrl}todos`;
  //needs to be replaced, but with what?

  private readonly statusKey = 'status';
  private readonly ownerKey = 'owner';
  private readonly bodyKey = 'body';

  // server side filtering
  getTodos(filters?: { body?: string; owner?: string; status?: TodoStatus;}) {
    let httpParams: HttpParams = new HttpParams();
    if (filters) {
      if (filters.status) {
        httpParams = httpParams.set(this.statusKey, filters.status);
      }
      if (filters.body) {
        httpParams = httpParams.set(this.bodyKey, filters.body.toString());
      }
      if (filters.owner) {
        httpParams = httpParams.set(this.ownerKey, filters.owner);
      }
    }
    return this.httpClient.get<Todo[]>(this.todoUrl, {
      params: httpParams,
    });
  }

  getTodoById(id: string): Observable<Todo> {
    return this.httpClient.get<Todo>(`${this.todoUrl}/${id}`);
  }

  //client side filtering
  filterTodos(todos: Todo[], filters: { owner?: string; body?: string; }): Todo[] {
    let filteredTodos = todos;

    // Filter by owner
    if (filters.owner) {
      filters.owner = filters.owner.toLowerCase();
      filteredTodos = filteredTodos.filter(todo => todo.owner.toLowerCase().indexOf(filters.owner) !== -1);
    }

    // Filter by body
    if (filters.body) {
      filters.body = filters.body.toLowerCase();
      filteredTodos = filteredTodos.filter(todo => todo.body.toLowerCase().indexOf(filters.body) !== -1);
    }

    return filteredTodos;
  }

  addTodo(newTodo: Partial<Todo>): Observable<string> {
    // Send post request to add a new user with the user data as the body.
    // `res.id` should be the MongoDB ID of the newly added `User`.
    return this.httpClient.post<{id: string}>(this.todoUrl, newTodo).pipe(map(response => response.id));
  }
}
