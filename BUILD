load("@rules_java//java:defs.bzl", "java_binary", "java_library", "java_test")

package(default_visibility = ["//visibility:public"])

java_binary(
    name = "ServerRunner",
    srcs = ["src/main/java/sg/edu/ntu/nfs/ServerRunner.java"],
    main_class = "sg.edu.ntu.nfs.ServerRunner",
    deps = ["//:server",
	    "//:common"],
)

java_binary(
    name = "ClientRunner",
    srcs = ["src/main/java/sg/edu/ntu/nfs/ClientRunner.java"],
    main_class = "sg.edu.ntu.nfs.ClientRunner",
    deps = ["//:client",
	    "//:common"],
)

java_library(
    name = "server",
    srcs = glob(["src/main/java/sg/edu/ntu/nfs/server/*.java"]),
    deps = ["//:common"],
)

java_library(
    name = "client",
    srcs = glob(["src/main/java/sg/edu/ntu/nfs/client/*.java"]),
    deps = ["//:common"],
)

java_library(
    name = "common",
    srcs = glob(["src/main/java/sg/edu/ntu/nfs/common/*.java"]),
)
