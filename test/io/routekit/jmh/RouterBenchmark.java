package io.routekit.jmh;

import io.routekit.Router;
import io.routekit.RouterSetup;
import org.openjdk.jmh.annotations.*;

public class RouterBenchmark {
    @State(Scope.Benchmark)
    public static class ExecutionPlan {
        @Param({ "1000" })
        private int iterations;
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
                    .add("/comment/{id}", "comment")
                    .build();
        }
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void route_home(ExecutionPlan plan) {
        for (int i = 0; i < plan.iterations; i++) {
            plan.router.routeOrNull("/");
        }
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void route_user(ExecutionPlan plan) {
        for (int i = 0; i < plan.iterations; i++) {
            plan.router.routeOrNull("/user/123456");
        }
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void route_post(ExecutionPlan plan) {
        for (int i = 0; i < plan.iterations; i++) {
            plan.router.routeOrNull("/post/12345/java-microbenchmark-harness");
        }
    }
}
