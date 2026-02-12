import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
//import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Todo } from './todo';

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

  getTodos(filters?: { body?: string; owner?: string }) {
    let httpParams: HttpParams = new HttpParams();
    if (filters) {
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

  filterTodos(todos: Todo[], filters: { owner?: string; body?: string }): Todo[] { // skipcq: JS-0105
    let filteredUsers = todos;

    // Filter by owner
    if (filters.owner) {
      filters.owner = filters.owner.toLowerCase();
      filteredUsers = filteredUsers.filter(todo => todo.owner.toLowerCase().indexOf(filters.owner) !== -1);
    }

    // Filter by body
    if (filters.body) {
      filters.body = filters.body.toLowerCase();
      filteredUsers = filteredUsers.filter(todo => todo.body.toLowerCase().indexOf(filters.body) !== -1);
    }

    return filteredUsers;
  }
}
