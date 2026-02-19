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

      // The URL should be '/todos/' followed by a mongo ID
      cy.url().should('match', /\/todos\/[0-9a-fA-F]{24}$/);

      cy.get('.todo-card-owner').first().should('have.text', firstTodoOwner);
      cy.get('.todo-card-body').first().should('have.text', firstTodoBody);
    });
  });

  it('Should type something in the body filter and check that it returned correct elements', () => {

    cy.get('[data-test=todoBodyInput]').type('Lorem');

    page.getTodoCards().each(e => {
      cy.wrap(e).find('.todo-card-body').should('contain.text', 'Lorem');
    });

    page.getTodoCards().find('.todo-card-body').each(el =>
      expect(el.text()).to.contain('Lorem')
    );
  });

  it('Should type something in the category filter and check that it returned correct elements', () => {

    cy.get('[data-test=todoCategoryInput]').type('homework');

    page.clickViewInformation(page.getTodoCards().first());
    cy.get('.todo-card-category').should('have.text', 'homework');

  });

  it('Should type something in the owner filter and check that it returned correct elements', () => {

    cy.get('[data-test=todoOwnerInput]').type('Blanche');

    page.getTodoCards().each(e => {
      cy.wrap(e).find('.todo-card-owner').should('have.text', 'Blanche');
    });

    page.getTodoCards().find('.todo-card-owner').each(el =>
      expect(el.text()).to.equal('Blanche')
    );
  });

  it('Should select a status and check that it returned correct cards', () => {
    // Filter for status 'incomplete');
    page.selectStatus('incomplete');

    // Some of the todos should be listed
    page.getTodoCards().should('have.lengthOf.above', 0);

    // All of the todo list items that show should have the status icon we are looking for
    page.getTodoCards().each(($card) => {
      cy.wrap($card).find('[data-test="incompleteIcon"]').should('exist');
    });
  });
});
