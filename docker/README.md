# example docker-compose process

## Docker_compose

### run mongo db, memcached and mqtt example 
Runs full backend stack i.e. persistent layer and endpoints

` docker-compose -f  v1/deploy_mongo_memcache_mqtt.yaml  up `

#### configure mongo db name , schema and auth
After successful mongo image initialization in docker, begin initialization:
- access image instance
	List running instance
	`docker ps`
	Get instance id and get into image bash
	`docker exec -it <instance id> /bin/bash`
	
- create a db
	begin mongo interaction
	`$> mongo`
	show current db
	`> db`
	change to or create new db
	`> use somenewdbname`
	copy and paste simple auth example found in 
	`mongo_conf/init_createDevUser.js`


