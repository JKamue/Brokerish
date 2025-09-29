rootProject.name = "brokerish"

include("packets")
include("parser")
include("server")
include("logic")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
