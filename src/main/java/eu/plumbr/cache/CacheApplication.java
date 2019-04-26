package eu.plumbr.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
@EnableCaching
public class CacheApplication {

  public static void main(String[] args) {
    SpringApplication.run(CacheApplication.class, args);
  }

  @Component("service")
  @Getter
  public static class Service {

    private final Cache cache;
    private final LoadingCache<Key, String> loadingCache;

    private AtomicLong caffeineCalls = new AtomicLong();
    private AtomicLong annotationCalls= new AtomicLong();

    public Service(CacheManager cacheManager) {
      cache = cacheManager.getCache("time");

      loadingCache = Caffeine.newBuilder()
              .expireAfterWrite(100, TimeUnit.MILLISECONDS)
              .build(key -> {
                caffeineCalls.incrementAndGet();

                return doGetTime(key.a, key.b);
              });
    }

    public String noCache(String a, String b) {
      return doGetTime(a, b);
    }

    @Cacheable("time")
    public String annotationBased(String a, String b) {
      return doGetTime(a, b);
    }

    @Cacheable("time")
    public String keyBased(Key key) {
      annotationCalls.incrementAndGet();

      return doGetTime(key.a, key.b);
    }

    public String caffeineBased(String a, String b) {
      return loadingCache.get(new Key(a, b));
    }

    @Cacheable(value = "time", key = "#p0.concat(#p1)")
    public String annotationWithSpel(String dummy1, String dummy2) {
      return doGetTime(dummy1, dummy1);
    }

    public long manual(String dummy) {
      Cache.ValueWrapper valueWrapper = cache.get(dummy);
      long result;
      if (valueWrapper == null) {
        result = System.currentTimeMillis();
        cache.put(dummy, result);
      } else {
        result = (long) valueWrapper.get();
      }
      return result;
    }

    private String doGetTime(String a, String b) {
      return a + b;
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    public static class Key {
      private String a;
      private String b;
    }
  }
}

