package main

import (
	"context"
	"dagger/spring-sample-app/internal/dagger"
	"fmt"
	"math"
	"math/rand/v2"
)

type SpringSampleApp struct{}

func (m *SpringSampleApp) Build(ctx context.Context, source *dagger.Directory) *dagger.File {
	return dag.Java().
		WithJdk("17").
		WithMaven("3.9.5").
		WithProject(source.WithoutDirectory("dagger")).
		Maven([]string{"package"}).
		File("target/spring-petclinic-3.4.0-SNAPSHOT.jar")
}

func (m *SpringSampleApp) Publish(ctx context.Context, source *dagger.Directory) (string, error) {
	return dag.Container(dagger.ContainerOpts{Platform: "linux/amd64"}).
		From("eclipse-temurin:17-alpine").
		WithLabel("org.opencontainers.image.title", "Java with Dagger").
		WithFile("/app/spring-petclinic-3.4.0-SNAPSHOT.jar", m.Build(ctx, source)).
		WithEntrypoint([]string{"java", "-jar", "/app/spring-petclinic-3.4.0-SNAPSHOT.jar"}).
		Publish(ctx, fmt.Sprintf("ttl.sh/app-%.0f", math.Floor(rand.Float64()*10000000)))
}
