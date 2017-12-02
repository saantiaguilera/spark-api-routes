# Spark Routes

Spark routes is an AOP library that will look at all the routes that your app registers, and log them in the attached sfl4j logger of spark.

Of course, this uses AspectJ (either CTW or LTW, thats up to you). There are other possibilities for addressing this problem (we could use XPosed or some other injector framework, we could decorate the spark router class and force you to use that one, among other possibilities).

I've opted for this one, since its the least invasive for the developer, and since we are at a backend the compilation/runtime overhead shouldnt impose any risk (except for the runtime setup overhead.. But I guess you already know this). Also note that we wrap around the register method, so this wont be executed over every api call (if that would be the case, then the overhead would be wayyy to big).

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

The library adds a weaving method, pointcutting the moment a new route (with its corresponding http method) is registered. 

We simply wrap it around a method of ours that logs it to your sfl4j logger, thus notifying you of all the routes you have registered.

We dont configure your aspectj things, so bear in mind this just provides the aspect classes. Its up to you to configure aspectj and how it will weave.

## Usage

This simply provides the aspect, you must configure aspectj in your build (either CTW / LTW). Since it will depend on your sources how its woven.

### Example of CTW

Add a binary CTW plugin to gradle: (I'll use sedolvax ones)
```gradle
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath "com.github.sedovalx.gradle:gradle-aspectj-binary:1.0.25"
  }
}
...
apply plugin: 'com.github.sedovalx.gradle-aspectj-binary'
...
weaveClasses.dependsOn compileJava
classes.dependsOn weaveClasses
...
weaveClasses {
  source = '1.8'
  target = '1.8'
  writeToLog = true
}
```

Of course, dont forget to add `aspectjrt` to your dependencies and add the classes you wish to wove (in this case, spark-core-<version>.jar) to the in-path for compilation.
    
### Example of LTW

Download a jar for `aspectjweaver`, save it somewhere and add it to the `javaagent` params (so it gets executed on bootstrap)
```gradle
applicationDefaultJvmArgs = [ // or append if you already have others
  "-javaagent:<path_where_you_downloaded_it>/aspectjweaver-<version>.jar", your_other_params
]
```

Add to your `aop.xml` something like this:
```xml
<!-- This is the file: resources/META-INF/aop.xml inside your project -->
<aspectj>
    <aspects>
        <aspect name="com.saantiaguilera.spark.RouteAspect"/>
        <!-- Your aspects... -->
    </aspects>

    <weaver options="-verbose">
        <!-- Classes that will be woven -->
        <include within="spark.*"/>
        <include within="org.aspectj.*"/>
    </weaver>
</aspectj>
```

That should do the trick.
