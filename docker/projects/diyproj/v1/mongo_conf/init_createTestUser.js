
use testdb;

db.createUser(
	{
		user:"testuser",
		pwd: "testpass",
		roles:[{
			role:"readWrite",
			db:"testdb"
		}]
	}
);

use eatuserdatadb;

db.createUser(
	{
		user:"mongoeatapp",
		pwd: "mongoeatpass",
		roles:[{
			role:"readWrite",
			db:"eatuserdatadb"
		}]
	}
);