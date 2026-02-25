package umm3601.todo;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.argThat;
// import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
// import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
// import java.util.Collections;
// import java.util.Collections;
// import java.util.HashMap;
import java.util.List;
import java.util.Map;
// import java.util.stream.Collectors;
// import java.util.stream.Collectors;

import org.bson.Document;
// import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
// import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.JsonMappingException;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.json.JavalinJackson;
import io.javalin.validation.BodyValidator;
import io.javalin.validation.ValidationError;
import io.javalin.validation.ValidationException;
// import io.javalin.validation.Validation;
import io.javalin.validation.Validator;
// import io.javalin.validation.Validation;
// import umm3601.todo.TodoController;


public class TodoControllerSpec {

  private TodoController todoController;
  private ObjectId taskId;
  private static MongoClient mongoClient;
  private static MongoDatabase db;
  private static JavalinJackson javalinJackson = new JavalinJackson();

  @Mock
  private Context ctx;

  @Captor
  private ArgumentCaptor<ArrayList<Todo>> todoArrayListCaptor;

  @Captor
  private ArgumentCaptor<Todo> todoCaptor;

  @Captor
  private ArgumentCaptor<Map<String, String>> mapCaptor;

  @BeforeAll
  static void setupAll() {
    String mongoAddr = System.getenv().getOrDefault("MONGO_ADDR", "localhost");

    mongoClient = MongoClients.create(
      MongoClientSettings.builder()
            .applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(mongoAddr))))
            .build());
    db = mongoClient.getDatabase("test");
  }

  @AfterAll
  static void teardown() {
    db.drop();
    mongoClient.close();
  }

  @BeforeEach
  void setUpEach() throws IOException {
    MockitoAnnotations.openMocks(this);

    MongoCollection<Document> todoDocuments = db.getCollection("todos");
    todoDocuments.drop();
    List<Document> testTodos = new ArrayList<>();
    testTodos.add(
      new Document()
        .append("owner", "Mike")
        .append("body", "Wash shirts and pants")
        .append("status", false)
        .append("category", "chores"));

    testTodos.add(
      new Document()
        .append("owner", "Marty")
        .append("body", "Buy applesauce from Willie's")
        .append("status", false)
        .append("category", "groceries"));

    testTodos.add(
      new Document()
        .append("owner", "Katie")
        .append("body", "Calculus Assignment #3")
        .append("status", true)
        .append("category", "homework"));

    testTodos.add(
      new Document()
        .append("owner", "Katy")
        .append("body", "CSCI Lab #2")
        .append("status", false)
        .append("category", "homework"));

    taskId = new ObjectId();
    Document task = new Document()
      .append("_id", taskId)
      .append("owner", "Pam")
      .append("body", "This is a default task description")
      .append("status", false)
      .append("category", "chores");

    todoDocuments.insertMany(testTodos);
    todoDocuments.insertOne(task);

    todoController = new TodoController(db);
  }

 @Test
  void getTodoWithExistentId() throws IOException {
    String id = taskId.toHexString();
    when(ctx.pathParam("id")).thenReturn(id);

    todoController.getTodo(ctx);

    verify(ctx).json(todoCaptor.capture());
    verify(ctx).status(HttpStatus.OK);
    assertEquals("Pam", todoCaptor.getValue().owner);
    assertEquals(taskId.toHexString(), todoCaptor.getValue()._id);
  }

  @Test
  void getTodoWithBadId() throws IOException {
    when(ctx.pathParam("id")).thenReturn("bad");

    Throwable exception = assertThrows(BadRequestResponse.class, () -> {
      todoController.getTodo(ctx);
    });

    assertEquals("The requested todo id wasn't a legal Mongo Object ID.", exception.getMessage());
  }

  @Test
  void getTodoWithNonexistentId() throws IOException {
    String id = "588935f5c668650dc77df581";
    when(ctx.pathParam("id")).thenReturn(id);

    Throwable exception = assertThrows(NotFoundResponse.class, () -> {
      todoController.getTodo(ctx);
    });

    assertEquals("The requested todo was not found", exception.getMessage());
  }

@Test
void addRoutes() {
  Javalin mockServer = mock(Javalin.class);
  todoController.addRoutes(mockServer);
  verify(mockServer, Mockito.atLeast(1)).get(any(), any());
  //will need to update this when more routes are added -HH
}

@Test
void canGetAllTodos() throws IOException {
  Validator<Integer> validator = mock(Validator.class);
  when(ctx.queryParamAsClass("limit", Integer.class)).thenReturn(validator);

  when(validator.getOrDefault(0)).thenReturn(0); //avoids null pointer for limit query that may or may not exist

  todoController.getTodos(ctx);
  //filling queryParamMap

  verify(ctx).json(todoArrayListCaptor.capture());
  //capturing the Array list to refer to later when we check it with database
  verify(ctx).status(HttpStatus.OK);

  assertEquals(
    db.getCollection("todos").countDocuments(),
    todoArrayListCaptor.getValue().size());
  // checking if the database has the same number of todos as the captured list
}
@Test
  void canGetCompleteTodoStatus() throws IOException {

  String completeStatus = "complete";
  String completeStatusString = completeStatus.toString(); //this variable was unneeded lol

    Map<String, List<String>> queryParams = new HashMap<>();

    queryParams.put("status", Arrays.asList(new String[] {completeStatusString}));
  // When the code being tested calls `ctx.queryParamMap()` return the
  // the `queryParams` map we just built.
    when(ctx.queryParamMap()).thenReturn(queryParams);

  // When the code being tested calls `ctx.queryParam("status")` return the
  // `completeStatusString`.
    when(ctx.queryParam("status")).thenReturn(completeStatusString);

  // Create a validator that confirms that when we ask for the value associated with
  // You can actually put whatever you want here, because it's only used in the generation
  // of testing error reports, but using the actually key value will make those reports more informative.
  Validator<String> statusValidator = mock(Validator.class);
  when(ctx.queryParamAsClass("status", String.class)).thenReturn(statusValidator);
  when(statusValidator.check(any(), anyString())).thenReturn(statusValidator);
  when(statusValidator.get()).thenReturn(completeStatus);

  Validator<Integer> limitValidator = mock(Validator.class);
  when(ctx.queryParamAsClass("limit", Integer.class)).thenReturn(limitValidator);
  when(limitValidator.getOrDefault(0)).thenReturn(0);
    // When the code being tested calls `ctx.queryParamAsClass("status", Integer.class)`
    // we'll return the `Validator` we just constructed.

    todoController.getTodos(ctx);

    // Confirm that the code being tested calls `ctx.json(…)`, and capture whatever
    // is passed in as the argument when `ctx.json()` is called.
    verify(ctx).json(todoArrayListCaptor.capture());
    // Confirm that the code under test calls `ctx.status(HttpStatus.OK)` is called.
    verify(ctx).status(HttpStatus.OK);

    // Confirm that we get back one todo.

    assertEquals(1, todoArrayListCaptor.getValue().size());


    // Confirm that todos have desired status: complete
    for (Todo todo : todoArrayListCaptor.getValue()) {
      assertTrue(todo.status);

    }
  }

  @Test
  void canGetInCompleteTodoStatus() throws IOException {

  String incompleteStatus = "incomplete";

    Map<String, List<String>> queryParams = new HashMap<>();

    queryParams.put("status", Arrays.asList(new String[] {incompleteStatus}));

    when(ctx.queryParamMap()).thenReturn(queryParams);
    when(ctx.queryParam("status")).thenReturn(incompleteStatus);

  Validator<String> statusValidator = mock(Validator.class);
  when(ctx.queryParamAsClass("status", String.class)).thenReturn(statusValidator);
  when(statusValidator.check(any(), anyString())).thenReturn(statusValidator);
  when(statusValidator.get()).thenReturn(incompleteStatus);

  Validator<Integer> limitValidator = mock(Validator.class);
  when(ctx.queryParamAsClass("limit", Integer.class)).thenReturn(limitValidator);
  when(limitValidator.getOrDefault(0)).thenReturn(0);

    todoController.getTodos(ctx);

    verify(ctx).json(todoArrayListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    // Confirm that todos have desired status: incomplete
    for (Todo todo : todoArrayListCaptor.getValue()) {
      assertFalse(todo.status);
    }
  }

  @Test
  void canGetInvalidTodoStatus() throws IOException {

  String invalidStatus = "complet";

    Map<String, List<String>> queryParams = new HashMap<>();

    queryParams.put("status", Arrays.asList(new String[] {invalidStatus}));
    when(ctx.queryParamMap()).thenReturn(queryParams);
    when(ctx.queryParam("status")).thenReturn(invalidStatus);

  Validator<String> statusValidator = mock(Validator.class);
  when(ctx.queryParamAsClass("status", String.class)).thenReturn(statusValidator);
  when(statusValidator.check(any(), anyString())).thenReturn(statusValidator);
  when(statusValidator.get()).thenReturn(invalidStatus);

  Validator<Integer> limitValidator = mock(Validator.class);
  when(ctx.queryParamAsClass("limit", Integer.class)).thenReturn(limitValidator);
  when(limitValidator.getOrDefault(0)).thenReturn(0);
    Exception thrown = assertThrows(IllegalArgumentException.class, () -> todoController.getTodos(ctx));

    assertEquals("Unexpected status: complet", thrown.getMessage());

    //We realize we missed this bit of coverage from the last lab and wanted to
    //test how the server would act if given an invalid input for its contents

    }

  @Test
  void testOrderBy() {
    Validator<Integer> validator = mock(Validator.class);
    when(ctx.queryParamAsClass("limit", Integer.class)).thenReturn(validator);

    when(validator.getOrDefault(0)).thenReturn(0); //avoids null pointer for limit query that may or may not exist

    when(ctx.queryParamMap()).thenReturn(Map.of("orderBy", List.of("owner")));
    //making sure that when the map is filled, it will be ordered by owner

    when(ctx.queryParam("orderBy")).thenReturn("owner");

    todoController.getTodos(ctx);
    //filling queryParamMap

    verify(ctx).json(todoArrayListCaptor.capture());
    //capturing the Array list to refer to later when we check it with database
    verify(ctx).status(HttpStatus.OK);

    List<String> returned = new ArrayList();
    for (Todo todo : todoArrayListCaptor.getValue()) {
      returned.add(todo.owner);
    } // adding each owner to the list in alphabetical order so we know they're ordered correctly

  List<String> expected = new ArrayList();
  expected.add("Katie");
  expected.add("Katy");
  expected.add("Marty");
  expected.add("Mike");
  expected.add("Pam");

  assertEquals(expected, returned);
}

  @Test
  void addTodo() throws IOException {
    // Create a new todo to add
    Todo newTodo = new Todo();
    newTodo.owner = "Test Todo";
    newTodo.category = "Test category";
    newTodo.body = "testers";
    newTodo.status = true;

    String newTodoJson = javalinJackson.toJsonString(newTodo, Todo.class);

    // A `BodyValidator` needs
    //   - The string (`newTodoJson`) being validated
    //   - The class (`Todo.class) it's trying to generate from that string
    //   - A function (`() -> Todo`) which "shows" the validator how to convert
    //     the JSON string to a `Todo` object. We'll again use `javalinJackson`,
    //     but in the other direction.
    when(ctx.bodyValidator(Todo.class))
      .thenReturn(new BodyValidator<Todo>(newTodoJson, Todo.class,
                    () -> javalinJackson.fromJsonString(newTodoJson, Todo.class)));

    todoController.addNewTodo(ctx);
    verify(ctx).json(mapCaptor.capture());

    // Our status should be 201, i.e., our new todo was successfully created.
    verify(ctx).status(HttpStatus.CREATED);

    // Verify that the todo was added to the database with the correct ID
    Document addedTodo = db.getCollection("todos")
        .find(eq("_id", new ObjectId(mapCaptor.getValue().get("id")))).first();

    // Successfully adding the todo should return the newly generated, non-empty
    // MongoDB ID for that todo.
    assertNotEquals("", addedTodo.get("_id"));
    // The new todo in the database (`addedTodo`) should have the same
    // field values as the todo we asked it to add (`newTodo`).
    assertEquals(newTodo.owner, addedTodo.get("owner"));
    assertEquals(newTodo.category, addedTodo.get("category"));
    assertEquals(newTodo.body, addedTodo.get("body"));
    assertEquals(newTodo.status, addedTodo.get("status"));
  }

  @Test
  void addEmptyAttributesTodo() throws IOException {
    String newTodoJson = """
        {
          "owner": "",
          "status": true,
          "body": "",
          "category": ""
        }
        """;

    when(ctx.body()).thenReturn(newTodoJson);
    when(ctx.bodyValidator(Todo.class))
        .then(value -> new BodyValidator<Todo>(newTodoJson, Todo.class,
                        () -> javalinJackson.fromJsonString(newTodoJson, Todo.class)));

    // This should now throw a `ValidationException`
    ValidationException exception = assertThrows(ValidationException.class, () -> {
      todoController.addNewTodo(ctx);
    });
    // This `ValidationException` was caused by a custom check, so we just get the message from the first
    // error (which is a `"REQUEST_BODY"` error) and convert that to a string with `toString()`. This gives
    // a `String` that has all the details of the exception, which we can make sure contains information
    // that would help a developer sort out validation errors.
    List<ValidationError<Object>> errors = exception.getErrors().get("REQUEST_BODY");

    // The message should be the message from our code under test, which should also include some text
    // indicating that there was an empty string for the todo owner.
    String ownerExceptionMessage = errors.get(0).toString();
    assertTrue(ownerExceptionMessage.contains("non-empty todo owner"));

    // The message should be the message from our code under test, which should also include some text
    // indicating that there was an empty string for the todo body.
    String bodyExceptionMessage = errors.get(1).toString();
    assertTrue(bodyExceptionMessage.contains("non-empty todo body"));


    String categoryExceptionMessage = errors.get(2).toString();
    assertTrue(categoryExceptionMessage.contains("non-empty todo category"));
  }

@Test
void addNullOwnerAndCategoryTodo() throws IOException {
  String newTodoJson = """
        {
          "status": true,
          "body": "bees"
        }
        """;

    when(ctx.body()).thenReturn(newTodoJson);
    when(ctx.bodyValidator(Todo.class))
        .then(value -> new BodyValidator<Todo>(newTodoJson, Todo.class,
                        () -> javalinJackson.fromJsonString(newTodoJson, Todo.class)));

    // This should now throw a `ValidationException`
    ValidationException exception = assertThrows(ValidationException.class, () -> {
      todoController.addNewTodo(ctx);
    });
    // This `ValidationException` was caused by a custom check, so we just get the message from the first
    // error (which is a `"REQUEST_BODY"` error) and convert that to a string with `toString()`. This gives
    // a `String` that has all the details of the exception, which we can make sure contains information
    // that would help a developer sort out validation errors.
    List<ValidationError<Object>> errors = exception.getErrors().get("REQUEST_BODY");

    // The message should be the message from our code under test, which should also include some text
    // indicating that there was an empty string for the todo owner.
    String ownerExceptionMessage = errors.get(0).toString();
    assertTrue(ownerExceptionMessage.contains("non-empty todo owner"));

    // The message should be the message from our code under test, which should also include some text
    // indicating that there was an empty string for the todo category.
    String categoryExceptionMessage = errors.get(1).toString();
    assertTrue(categoryExceptionMessage.contains("non-empty todo category"));
  }

@Test
void addNullBodyTodo() throws IOException {
  String newTodoJson = """
        {
          "owner": "guy",
          "status": true,
          "category": "beans"
        }
        """;

    when(ctx.body()).thenReturn(newTodoJson);
    when(ctx.bodyValidator(Todo.class))
        .then(value -> new BodyValidator<Todo>(newTodoJson, Todo.class,
                        () -> javalinJackson.fromJsonString(newTodoJson, Todo.class)));

    // This should now throw a `ValidationException`
    ValidationException exception = assertThrows(ValidationException.class, () -> {
      todoController.addNewTodo(ctx);
    });
    // This `ValidationException` was caused by a custom check, so we just get the message from the first
    // error (which is a `"REQUEST_BODY"` error) and convert that to a string with `toString()`. This gives
    // a `String` that has all the details of the exception, which we can make sure contains information
    // that would help a developer sort out validation errors.
    List<ValidationError<Object>> errors = exception.getErrors().get("REQUEST_BODY");

    // The message should be the message from our code under test, which should also include some text
    // indicating that there was an empty string for the todo body.
    String bodyExceptionMessage = errors.get(0).toString();
    assertTrue(bodyExceptionMessage.contains("non-empty todo body"));
  }

}
