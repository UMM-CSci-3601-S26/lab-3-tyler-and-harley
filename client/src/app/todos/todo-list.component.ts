import { Component, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatOptionModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
//import { catchError, combineLatest, of, switchMap, tap } from 'rxjs';
import { status } from './todo';
import { TodoCardComponent } from './todo-card.component';
// import { TodoService } from './todo.service';
// import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { UserService } from '../users/user.service';

@Component({
  selector: 'app-todo-list-component',
  templateUrl: 'todo-list.component.html',
  styleUrls: ['./todo-list.component.scss'],
  providers: [],
  imports: [
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatSelectModule,
    MatOptionModule,
    MatRadioModule,
    TodoCardComponent,
    MatListModule,
    RouterLink,
    MatButtonModule,
    MatTooltipModule,
    MatIconModule,
  ],
})
export class TodoListComponent {

  private todoService = inject(UserService)
  private snackBar = inject(MatSnackBar);

  owner = signal<string | undefined>(undefined);
  category = signal<number | undefined>(undefined);
  status = signal<status | undefined>(undefined);
  body = signal<string | undefined>(undefined);

  viewType = signal<'card' | 'list'>('card');

}
