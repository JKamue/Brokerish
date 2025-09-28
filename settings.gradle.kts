rootProject.name = "brokerish"

include("packets")
include("parser")
include("server")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
