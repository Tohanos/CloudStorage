DROP TABLE IF EXISTS cloudstorage.users;
CREATE TABLE cloudstorage.users (
	userid			SERIAL PRIMARY KEY NOT NULL,
    username    	varchar(40) NOT NULL,
    userpassword	varchar(40) NOT NULL,
    dateadd		   	timestamp,
    rootdir        	varchar(128) NOT NULL,
    userrights      varchar(3) NOT NULL
);