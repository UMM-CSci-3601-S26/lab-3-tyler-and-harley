package umm3601.todo;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
// import static com.mongodb.client.model.Filters.regex;

// import java.nio.charset.StandardCharsets;
// import java.security.MessageDigest;
// import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
// import java.util.Objects;
// import java.util.regex.Pattern;
// import java.util.Set;

import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import com.mongodb.client.MongoDatabase;
// import com.mongodb.client.model.Sorts;
// import com.mongodb.client.result.DeleteResult;
// import com.mongodb.client.model.Filters;
// import com.mongodb.client.model.Sorts;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import umm3601.Controller;

/**
 * Controller that manages requests for info about todos.
 */
public class TodoController implements Controller {

  private static final String API_TODOS = "/api/todos";
  private static final String API_TODO_BY_ID = "/api/todos/{id}";

  private static final String STATUS_REGEX = "^(complete|incomplete)$";
  private static final String STATUS_KEY = "^(true|false)$";

  // private static final String STATUS_REGEX = "^(complete|incomplete)$";
  // private static final String BODY_REGEX = ".*";

  private final JacksonMongoCollection<Todo> todoCollection;

  /**
   * Construct a controller for todos.
   *
   *
   * @param database the database containing user data
   */
  public TodoController(MongoDatabase database) {
    todoCollection = JacksonMongoCollection.builder().build(
        database,
        "todos",
        Todo.class,
        UuidRepresentation.STANDARD);
  }

  /**
   * Set the JSON body of the response to be the single todo
   * specified by the `id` parameter in the request
   *
   * @param ctx a Javalin HTTP context
   */
  public void getTodo(Context ctx) {
    String id = ctx.pathParam("id");
    Todo todo;

    try {
      todo = todoCollection.find(eq("_id", new ObjectId(id))).first();
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse("The requested todo id wasn't a legal Mongo Object ID.");
    }
    if (todo == null) {
      throw new NotFoundResponse("The requested todo was not found");
    } else {
      ctx.json(todo);
      ctx.status(HttpStatus.OK);
    }
  }

  /**
   * Set the JSON body of the response to be a list of all the todos returned from the database
   * that match any requested filters and ordering
   *
   * @param ctx a Javalin HTTP context
   */
  public void getTodos(Context ctx) {
    Bson combinedFilter = constructFilter(ctx);

    int limit = ctx.queryParamAsClass("limit", Integer.class)
      .getOrDefault(0);
    //Creates a query parameter that can be used named "limit" which is an integer.
    //The default todo limit is '0' which means that there is no limit and all todos are displayed.

    ArrayList<Todo> matchingTodos = todoCollection
      .find(combinedFilter)
      .limit(limit)
      .into(new ArrayList<>());

    // Set the JSON body of the response to be the list of todos returned by the database.
    // According to the Javalin documentation (https://javalin.io/documentation#context),
    // this calls result(jsonString), and also sets content type to json
    ctx.json(matchingTodos);

    // Explicitly set the context status to OK
    ctx.status(HttpStatus.OK);
 }

 private Bson constructFilter(Context ctx) {
    List<Bson> filters = new ArrayList<>(); // start with an empty list of filters


    if (ctx.queryParamMap().containsKey("status")) {
      String status = ctx.queryParamAsClass("status", String.class)
        .check(it -> it.matches(STATUS_REGEX), "User must have a legal status")
        .get();

        Boolean boolStatus = switch (status) {
          case "complete" -> true;            //if status is "complete," then the boolean value returned is true
          case "incomplete" -> false;         //if the status is "incomplete," then the value returned is false
          default -> throw new IllegalArgumentException("Unexpected status: " + status);
        };  //in the case that the status is neither "compete" nor "incomplete,"
            // then and exception is thrown that stops execution
            // -Evie (I forgot to reference this early on, but I did have help from AI(Copilot) coming up
            //  with this portion of code)

     filters.add(eq("status", boolStatus));
    }
    Bson combinedFilter = filters.isEmpty() ? new Document() : and(filters);

    return combinedFilter;
  }

     public void addNewTodo(Context ctx) {
    /*
     * The follow chain of statements uses the Javalin validator system
     * to verify that instance of `User` provided in this context is
     * a "legal" user. It checks the following things (in order):
     *    - The user has a value for the name (`usr.name != null`)
     *    - The user name is not blank (`usr.name.length > 0`)
     *    - The provided email is valid (matches EMAIL_REGEX)
     *    - The provided age is > 0
     *    - The provided age is < REASONABLE_AGE_LIMIT
     *    - The provided role is valid (one of "admin", "editor", or "viewer")
     *    - A non-blank company is provided
     * If any of these checks fail, the Javalin system will throw a
     * `BadRequestResponse` with an appropriate error message.
     */
    String body = ctx.body();
    Todo newTodo = ctx.bodyValidator(Todo.class)
      .check(todo -> todo.owner != null && todo.owner.length() > 0,
        "Todo must have a non-empty todo owner; body was " + body)
      .check(todo -> todo.status.toString().matches(STATUS_KEY),
        "Todo must have a legal todo status; body was " + body)
      .check(todo -> todo.body != null && todo.body.length() > 0,
        "Todo must have a non-empty todo body; body was " + body)
      .check(todo -> todo.category != null && todo.category.length() > 0,
        "Todo must have a non-empty todo category; body was " + body)
      .get();

    // Add the new todo to the database
    todoCollection.insertOne(newTodo);

    ctx.json(Map.of("id", newTodo._id));

    ctx.status(HttpStatus.CREATED);
  }

  @Override
  public void addRoutes(Javalin server) {
    // List users, filtered using query parameters
    server.get(API_TODOS, this::getTodos);

    server.get(API_TODO_BY_ID, this::getTodo);

    server.post(API_TODOS, this::addNewTodo);

    //no specific endpoint needed for queries

  }
}
