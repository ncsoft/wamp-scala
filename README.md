# WAMP?
[WAMP](http://wamp-proto.org/)(Web Application Messaging Protocol) is a protocol that provides most of messaging features. 
Compared to other protocols such as [XMPP](https://en.wikipedia.org/wiki/XMPP), WAMP is concise and straightforward to understand and implement.  

# wamp-scala?
wamp-scala is an implementation of [wamp](http://wamp-proto.org/) in Scala. 

# Restrictions
wamp-scala does not handle web socket connections yet.   

# For Contributors

## Development Environment
* Language: Scala(2.11)
* Framework: [Akka](http://akka.io)(2.4.1)
* Build: [sbt](http://www.scala-sbt.org/)(0.13.8)


## Package
```bash
> ./sbt package packageSrc 
```

## Test
```bash
> ./sbt test
```

