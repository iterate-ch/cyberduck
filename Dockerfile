FROM maven:3-openjdk-17-slim AS build
WORKDIR /build
COPY . /build
RUN maven -B verify --project cli/linux --also-make -DskipTests

FROM ubuntu:focal
ENTRYPOINT ["/usr/bin/duck"]
CMD ["--version"]

COPY --from=build /build/cli/linux/target/release/duck /opt/duck/
RUN ln -s /opt/duck/bin/duck /usr/bin/duck
