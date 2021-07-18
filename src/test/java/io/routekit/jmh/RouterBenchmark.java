package io.routekit.jmh;

import io.routekit.Router;
import io.routekit.RouterSetup;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Fork(value = 1, warmups = 1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 4, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
public class RouterBenchmark {
    @State(Scope.Benchmark)
    public static class ExecutionPlan {
        @Param({ "1000" })
        private int iterations = 0;
        private Router<String> router;

        @Setup(Level.Invocation)
        public void setUp() {
            router = new RouterSetup<String>()
                    .add("/", "home")
                    .add("/index", "index")
                    .add("/about", "about")
                    .add("/contact", "contact")
                    .add("/user", "all_users")
                    .add("/user/{id}", "user")
                    .add("/blog", "all_blogs")
                    .add("/blog/{id}", "blog")
                    .add("/post", "all_posts")
                    .add("/post/{id}", "post")
                    .add("/post/{id}/{slug}", "post")
                    .add("/post/{id}/{slug}/comments", "post_comments")
                    .add("/p/{id}/{*slug}", "post")
                    .add("/comment/{id}", "comment")
                    .add("/tag", "all_tags")
                    .add("/tag/{tag}", "tag")
                    .build();
        }
    }

    @Benchmark
    public void route_home(ExecutionPlan plan) {
        for (int i = 0; i < plan.iterations; i++) {
            plan.router.routeOrNull("/");
        }
    }

    @Benchmark
    public void route_about(ExecutionPlan plan) {
        for (int i = 0; i < plan.iterations; i++) {
            plan.router.routeOrNull("/about");
        }
    }

    @Benchmark
    public void route_user(ExecutionPlan plan) {
        for (int i = 0; i < plan.iterations; i++) {
            plan.router.routeOrNull("/user/123456");
        }
    }

    @Benchmark
    public void route_post(ExecutionPlan plan) {
        for (int i = 0; i < plan.iterations; i++) {
            plan.router.routeOrNull("/post/123");
        }
    }

    @Benchmark
    public void route_post_slug(ExecutionPlan plan) {
        for (int i = 0; i < plan.iterations; i++) {
            plan.router.routeOrNull("/post/12345/java-microbenchmark-harness");
        }
    }

    @Benchmark
    public void route_post_wildcard_slug(ExecutionPlan plan) {
        for (int i = 0; i < plan.iterations; i++) {
            plan.router.routeOrNull("/p/12345/java-microbenchmark-harness");
        }
    }

    @Benchmark
    public void route_post_slug_comments(ExecutionPlan plan) {
        for (int i = 0; i < plan.iterations; i++) {
            plan.router.routeOrNull("/post/12345/java-microbenchmark-harness/comments");
        }
    }
}
