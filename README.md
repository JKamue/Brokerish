# Brokerish

My own MQTT 5.0 Broker written in Kotlin for learning. I cannot recommend using this in any real prod environment.

## Goals

One of my main goals was having a clean OOP architecture that still performs very well and can handle a large amount of
clients sending and receiving simultaneously. As much of the data should be handled with only one copy.

I want to first be as much as possible QoS 0 feature complete before moving onto QoS 1 and maybe if I stay motivated
long enough even QoS 2.

## ktor

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- The [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). You'll need
  to [request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up) to join.

### Features

Here's a list of features included in this project:

| Name                                                                   | Description                                                 |
|------------------------------------------------------------------------|-------------------------------------------------------------|
| [Raw Sockets](https://start.ktor.io/p/ktor-network)                    | Adds raw socket support for TCP and UDP                     |
| [Raw Secure SSL/TLS Sockets](https://start.ktor.io/p/ktor-network-tls) | Adds secure socket support for TCP and UDP                  |
| [Routing](https://start.ktor.io/p/routing-default)                     | Allows to define structured routes and associated handlers. |

### Building & Running

To build or run the project, use one of the following tasks:

| Task                          | Description                                                          |
|-------------------------------|----------------------------------------------------------------------|
| `./gradlew test`              | Run the tests                                                        |
| `./gradlew build`             | Build everything                                                     |
| `buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `buildImage`                  | Build the docker image to use with the fat JAR                       |
| `publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `run`                         | Run the server                                                       |
| `runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

