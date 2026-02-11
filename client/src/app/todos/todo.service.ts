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
  private readonly ownerKey = 'owner'
  private readonly categoryKey = 'category'

  getTodos() {
    const httpParams: HttpParams = new HttpParams();
    return this.httpClient.get<Todo[]>(this.todoUrl, {
      params: httpParams,
    });
  }
  getUserById(id: string): Observable<Todo> {
    return this.httpClient.get<Todo>(`${this.todoUrl}/${id}`);
  }


}
