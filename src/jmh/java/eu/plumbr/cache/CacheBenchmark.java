package eu.plumbr.cache;

import org.apache.commons.lang.RandomStringUtils;
import org.openjdk.jmh.annotations.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Warmup(iterations = 0, time = 3)
@Measurement(iterations = 2, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CacheBenchmark {

    private CacheApplication.Service service;

    @Setup
    public void setup() {
        ConfigurableApplicationContext context = SpringApplication.run(CacheApplication.class);

        service = context.getBean("service", CacheApplication.Service.class);
    }

    @TearDown
    public void tearDown() {
        System.out.println("Annotation calls: " + service.getAnnotationCalls());
        System.out.println("Caffeine calls: " + service.getCaffeineCalls());
    }

    @Benchmark
    public String nocache() {
        return service.noCache(getRandom(), getRandom());
    }

    @Benchmark
    public String annotationBased() {
        return service.annotationBased(getRandom(), getRandom());
    }

    @Benchmark
    public String keyBased() {
        return service.keyBased(new CacheApplication.Service.Key(getRandom(), getRandom()));
    }

    @Benchmark
    public String caffeineBased() {
        return service.caffeineBased(getRandom(), getRandom());
    }

    @Benchmark
    public String spel() {
        return service.annotationWithSpel(getRandom(), getRandom());
    }

    @Benchmark
    public long manual() {
        return service.manual(getRandom());
    }

    private String getRandom() {
        return RandomStringUtils.randomAlphabetic(2).toLowerCase();
    }

}
