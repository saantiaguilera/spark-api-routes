# Spark Routes

Spark routes is a runtime aspectj weaver that will look at all the routes that your app registers, and log them in the attached sfl4j logger of spark.

## Why

Its really common to have all the routes of a backend registered in a single class. While this is extremely awful and highly coupled, it gives all the app routes a lot of visibility, thus this is something highly done by developers. 

Eg:
```java
class Router {
    public void exposeRoutes() {
        path("/api", () -> {
            get("", apiController::get);
            path("/user", () -> {
                get("/:id", userController::get);
                post("", userController::post);
                path("/image", () -> {
                    // ...
                });
                // ...
            });
            // ...
        });
    } 
}
```

On the contrary, if you are a maniac that wants your application nice and cute, you probably have all dependencies decoupled from the single class. The main disadvantage is that if you want to see all the routes that are registered, its a pain (duhh). 

Eg:
```java
class ApiRouter implements Router {
    @Override
    public void exposeRoutes() {
        path("/api", () -> {
            with(apiController).bind(); // Bind controller to this current path
            
            userRouter.exposeRoutes();
        });
    }
}

class UserRouter implements Router {
    @Override
    public void exposeRoutes() {
        path("/user", () -> {
            with(userController).bind(); // Bind controller to this current path
            
            imageRouter.exposeRoutes();
        });
    }
}

// ...
```

The aim is to leverage tha pain of the devs in the second case

## How?

The library adds a runtime weaving method, pointcutting the moment a new route (with its corresponding http method) is registered. 

When this occur, we simply wrap it around a method of ours that logs it to your sfl4j logger, thus notifying you of all the routes you have registered.