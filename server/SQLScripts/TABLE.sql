DROP TABLE IF EXISTS cloudstorage.users;
CREATE TABLE cloudstorage.users (
	userid			SERIAL PRIMARY KEY,
    username    	varchar(40) NOT NULL,
    userpassword	varchar(40) NOT NULL,
    dateadd		   	timestamp,
    rootdir        	varchar(128) NOT NULL
);