# Demo for Java11 Spring boot projects with webflux, JWT

It is for the use case of `API <--> Consumer`, not for `Browser <--> Server`. Because it does not have `userDetails`. And you should separate the `JWT` generation and verification to `Consumer` and `API` server. 


It show how you can generate a JWT token, and verify it in resource.  
To run it, you need to set an environment variable `JWT_HEADER`, which is the header key you need to put to the HTTP Header while you access the resource.

For Example you start the app with below command line:
```
export JWT_HEADER=X-COMFORTHEART-JWT && java -jar target/demo-java11-webflux-0.0.1-SNAPSHOT.jar
```
Then access like this:
```
$ curl -i http://localhost:8080/jwt
HTTP/1.1 200 OK
Content-Type: text/plain;charset=UTF-8
Content-Length: 155

eyJhbGciOiJFUzI1NiJ9.eyJuYmYiOjE1NTQ2MjA0MzUsImV4cCI6MTU1NDYyMTAzNX0.J8YrBWv12kXdcRpKuCHZ6xCSdCAm_rvblobH5m0ERBNRA53F_E7U1AId5HtaJfZA0j_Gi7wREmg_Dl4iq5zTkA

-- While you get the token above, use it in your command:
$ curl -i --header "X-COMFORTHEART-JWT: eyJhbGciOiJFUzI1NiJ9.eyJuYmYiOjE1NTQ2MjA0MzUsImV4cCI6MTU1NDYyMTAzNX0.J8YrBWv12kXdcRpKuCHZ6xCSdCAm_rvblobH5m0ERBNRA53F_E7U1AId5HtaJfZA0j_Gi7wREmg_Dl4iq5zTkA" http://localhost:8080/api/test

```

#### Tech stack:
> - Spring boot 2.1.4 with webflux
> - io.jsonwebtoken:jjwt:0.9.1
> - Open JDK 11.0.2

#### Command to generate the keystore
> - We use  ECDSA instead of RSA as the signing/verifying algorithm.   
Because ECDSA is able to provide equivalent security to RSA cryptography but using shorter key sizes and with greater processing speed.  
```
# Generate the keystore with self-signed keypair.
keytool -genkeypair -alias E2E_Alias -sigalg SHA256withECDSA -keystore demo.jks -storetype pkcs12 -keysize 256 -keyalg EC -dname "CN=Consumer,OU=TechDept,O=Comfortheart.tech,L=GZ,ST=GD,C=China" -storepass changeit -keypass changeit

# Export the public key.
keytool -exportcert -keystore demo.jks -file demo.cer -alias E2E_Alias -storepass changeit -keypass changeit

# Ref commands for RSA key generation too, but if you use it, need to do some code changes
keytool -genkeypair -alias E2E_Alias -sigalg SHA256withRSA -keystore demo.jks -storetype jks -keysize 2048 -keyalg RSA -dname "CN=Consumer,OU=TechDept,O=Comfortheart.tech,L=GZ,ST=GD,C=China" -storepass changeit
```


# There is also a poc for formula engines
> Finally found Lua is the fastest, while JRuby is the best because it can reach the balance of performance, user-friendly and accuracy.

Please ref to https://github.com/jruby/jruby/wiki/RedBridge
