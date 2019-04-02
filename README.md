# spring-security-6683-issue-verification #

See https://github.com/spring-projects/spring-security/issues/6683

1. Run the application and by default a mocked client credentials call 
will occur and setup some basic authentication. 
1. If ran without the `apply` method being called in the [BackgroundClient.java](src/main/java/sample/BackgroundClient.java)
a `401` will occur