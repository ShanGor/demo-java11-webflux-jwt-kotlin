# Demo for Java11 Spring boot projects with webflux, JWT

#### Tech stack:
> - Spring boot 2.1.4 with webflux
> - io.jsonwebtoken:jjwt:0.9.1

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
