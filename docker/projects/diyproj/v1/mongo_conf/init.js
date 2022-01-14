let db = connect("localhost:27017/TestInitDB");
db.auth("testuser", "testpass");
let collections = db.getCollectionNames();
let storeFound = false; 
let index;
for(index=0; index<collections.length; index++){
   if ("store" === collections[index]){
       storeFound = true;   
   }
}
if(!storeFound ){
   db.createCollection("store");
   db.store.createIndex({"name": 1});
}