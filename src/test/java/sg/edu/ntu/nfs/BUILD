load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "requests",
    srcs = (["common/requests/RequestTest.java"]),
    test_class = "sg.edu.ntu.nfs.common.requests.RequestTest",
    deps = [
        "//:common",
        "//:java_test_deps",
    ],
)

java_test(
    name = "responses",
    srcs = (["common/responses/ResponseTest.java"]),
    test_class = "sg.edu.ntu.nfs.common.responses.ResponseTest",
    deps = [
        "//:common",
        "//:java_test_deps",
    ],
)