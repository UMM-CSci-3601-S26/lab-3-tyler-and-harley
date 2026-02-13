import { TodoListPage } from '../support/todo-list.po';

const page = new TodoListPage();

describe('Todo list', () => {

  before(() => {
    cy.task('seed:database');
  });

  beforeEach(() => {
    page.navigateTo();
  });

  it('Should have the correct title', () => {
    page.getTodoTitle().should('have.text', 'Todos');
  });

  it('Should click view profile on a todo and go to the right URL', () => {
    page.getTodoCards().first().then((card) => {
      const firstTodoOwner = card.find('.todo-card-owner').text();
      const firstTodoBody = card.find('.todo-card-body').text();

      // When the view profile button on the first todo card is clicked, the URL should have a valid mongo ID
      page.clickViewInformation(page.getTodoCards().first());

      // The URL should be '/users/' followed by a mongo ID
      cy.url().should('match', /\/todos\/[0-9a-fA-F]{24}$/);

      // On this profile page we were sent to, the name and company should be correct
      cy.get('.todo-card-owner').first().should('have.text', firstTodoOwner);
      cy.get('.todo-card-body').first().should('have.text', firstTodoBody);
    });
  });

});
