
use devdb;

db.createUser(
	{
		user:"devuser",
		pwd: "devpass",
		roles:[{
			role:"readWrite",
			db:"devdb"
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