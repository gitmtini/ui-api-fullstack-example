## Example reactnative project that has an api


###Dependencies

1. Google protobuf

```
npm install google-protobuf --save
```

2. Generate proto data structure:

```
protoc --js_out=import_style=commonjs,binary:./proto/lib ./proto/CustomerDetailsAndWallet.proto ./proto/RequestResponse.proto
```

3. Protobuf js
```
npm install protobufjs
```

4. Base64 encoder decoder
```
npm install --save react-native-base64
```


