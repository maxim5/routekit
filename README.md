Java Ro*u*teKit
---------------

Super-fast and lightweight Java URL router.

### Features

- **Fast**: highly efficient routing algorithm using hashtable index, prefix trie and finite state machine. No regexp matching.
- **GC friendly**: minimal number of allocations during routing. No unnecessary copying and `String` and `char[]` waste.
- **Small**: whole library is one 30kb jar.
- **Scalable**: can handle thousands of rules without overhead.
- **Simple**: supports string variables and wildcards.
- **Lightweight**. No dependencies on other libraries. None. Zero.
- **Adaptable**: API does not require or force any web framework or library. Works with any `String` or `char[]` input.
- **Compatible**: Netty.
- **Extensible**: supports options and extension points for new rule syntax, tokens or customized matching.

### Example usage

```java
// The setup: map the urls to a handler (can be any class).
Router<T> router = new RouterSetup<T>()
    .add("/", ...)
    .add("/foo", ...)
    .add("/bar", ...)
    .build();
```

For example, if you have a `Handler` hierarchy, implemented by per-page classes (`HomeHandler`, `UserHandler`, etc),
it might look like this:

```java
Router<Handler> router = new RouterSetup<Handler>()
    .add("/", new HomeHandler())
    .add("/user", new ListUsersHandler())
    .add("/user/{id}", new UserHandler())
    .add("/post", new ListBlogPostsHandler())
    .add("/post/{id}", new BlogPostHandler())
    .add("/post/{id}/{slug}", new BlogPostHandler())
        ...
    .build();
```

URL navigation is as follows:

```java
Match<Handler> match = router.routeOrNull(url);
if (match == null)
    throw new NotFoundException();
return match.handler().accept(match.variables());
```

Note the handler can use the variables map, like `id` -> `123`.

### Performance

Benchmark results for a small/medium size setup show that routing static URL (without variables) throughput 
can reach <b>55m ops/sec</b>, one variable URL - <b>10m ops/sec</b>, two variables URL - <b>6m ops/sec</b>.
<br>
Environment: AMD Ryzen 5 4600H, 3.00 GHz, <b>single-threaded</b>.

<a href='/src/test/java/io/routekit/jmh'>JMH benchmark</a> output below:

```
Benchmark                                 (iterations)   Mode   Samples        Score  Score error    Units
i.r.j.RouterBenchmark.route_home                  1000  thrpt        10    55373,952     1356,069    ops/s
i.r.j.RouterBenchmark.route_post                  1000  thrpt        10    10935,147      190,314    ops/s
i.r.j.RouterBenchmark.route_post_slug             1000  thrpt        10     6207,792       59,925    ops/s
i.r.j.RouterBenchmark.route_user                  1000  thrpt        10     9918,927      290,196    ops/s
```
