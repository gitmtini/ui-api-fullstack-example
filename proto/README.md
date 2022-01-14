# example protobuf project
Depends on proto3, proto-javalite

## java projects
Generate java proto objects on commandline

```
protoc  --java_out=../api/src/ ./src/*proto
```


## npm and react projects protos
On commandline generate js protos lib/binary in bin directory

```
protoc --js_out=import_style=commonjs,binary:../ui-reactnative/proto/lib ./src/CustomerDetailsAndWallet.proto ./src/RequestResponse.proto
```


