import { Component, signal, inject, Signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { catchError, map, switchMap } from 'rxjs/operators';
import { TodoCardComponent } from './todo-card.component';
import { TodoService } from './todo.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { of } from 'rxjs';
import { Todo } from './todo';

@Component({
  selector: 'app-todo-profile',
  imports: [TodoCardComponent, MatCardModule],
  templateUrl: './todo-profile.component.html',
  styleUrl: './todo-profile.component.scss',
})
export class TodoProfileComponent {
  private route = inject(ActivatedRoute);
  private todoService = inject(TodoService);

  todo: Signal<Todo> = toSignal(
    this.route.paramMap.pipe(

      map((paramMap: ParamMap) => paramMap.get('id')),

      switchMap((id: string) => this.todoService.getTodoById(id)),
      catchError((_err) => {
        this.error.set({
          help: 'There was a problem loading the todo – try again.',
          httpResponse: _err.message,
          message: _err.error?.title,
        });
        return of();
      })

    )
  );

  error = signal({ help: '', httpResponse: '', message: '' });
}
