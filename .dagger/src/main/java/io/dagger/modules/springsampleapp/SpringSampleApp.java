package io.dagger.modules.springsampleapp;

import io.dagger.client.Container;
import io.dagger.client.DaggerQueryException;
import io.dagger.client.Directory;
import io.dagger.client.File;
import io.dagger.client.CacheVolume;
import io.dagger.module.AbstractModule;
import io.dagger.module.annotation.Function;
import io.dagger.module.annotation.Object;
import io.dagger.module.annotation.DefaultPath;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** SpringSampleApp main object */
@Object
public class SpringSampleApp extends AbstractModule {
  /** Build application */
  @Function
  public File build(@DefaultPath(".") Directory source)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    return dag.container()
        .from("maven:3.9-eclipse-temurin-23")
        .withMountedCache("/root/.m2", (CacheVolume) dag.cacheVolume("maven-cache"))
        .withDirectory("/src", source.withoutDirectory(".dagger"))
        .withWorkdir("/src")
        .withExec(List.of("mvn", "package"))
        .file("target/spring-petclinic-3.4.0-SNAPSHOT.jar");
  }

  /** Publish application */
  @Function
  public String publish(@DefaultPath(".") Directory source)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    return dag.container()
        .from("eclipse-temurin:23-alpine")
        .withLabel("org.opencontainers.image.title", "Java with Dagger")
        .withFile("/app/spring-petclinic-3.4.0-SNAPSHOT.jar", this.build(source))
        .withEntrypoint(List.of("java", "-jar", "/app/spring-petclinic-3.4.0-SNAPSHOT.jar"))
        .publish("ttl.sh/app-" + (int) (Math.random() * 10000000));
    }
}
