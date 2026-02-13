import { Component, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { RouterLink } from '@angular/router';
import { Todo, status } from './todo';
import { toObservable } from '@angular/core/rxjs-interop';
@Component({
  selector: 'app-todo-card',
  templateUrl: './todo-card.component.html',
  styleUrls: ['./todo-card.component.scss'],
  imports: [MatCardModule, MatButtonModule, MatListModule, MatIconModule, RouterLink,]
})
export class TodoCardComponent {

  status = signal<status | undefined>(undefined);
  private status$ = toObservable(this.status);

  todo = input.required<Todo>();
  todoStatus = this.status();
  simple = input(false);

}
