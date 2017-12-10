# --- !Ups

create table "USERS" ("ID" INTEGER PRIMARY KEY,"USER" VARCHAR NOT NULL,"PASSWORD" VARCHAR NOT NULL,"NICKNAME" VARCHAR NOT NULL,ROLE VARCHAR);

create table "JOBS" (
	"ID" INTEGER PRIMARY KEY,
	"USER_ID" INTEGER NOT NULL,
	"DESCRIPTION" VARCHAR NOT NULL, 
	"REQUIRED_EFFORT" REAL NOT NULL, 
	"ACTUAL_EFFORT" REAL NOT NULL, 
	"IDEAL_DAILY_EFFORT" REAL NOT NULL, 
	"ESTIMATE_LEFT_EFFORT" REAL , 
	"START_DATE" TIMESTAMP NOT NULL,
	"END_DATE" TIMESTAMP NOT NULL,
	"PRIORITY" INTEGER NOT NULL,
	FOREIGN KEY("USER_ID") REFERENCES "USERS"("ID"));

insert into "JOBS" ("ID","USER_ID","DESCRIPTION","REQUIRED_EFFORT","ACTUAL_EFFORT","IDEAL_DAILY_EFFORT","START_DATE","END_DATE" ,"PRIORITY" )
	values(1, 1,"Dont call me Bernie",200,10,30,strftime('%s', 'now'),strftime('%s', 'now'),1);


insert into "USERS" ("ID","USER","PASSWORD","NICKNAME") values (1,'bernard','jason','Dont call me Bernie');	
insert into "USERS" ("ID","USER","PASSWORD","NICKNAME","ROLE") values (2,'admin','admin','super user',"admin");
insert into "USERS" ("ID","USER","PASSWORD","NICKNAME") values (3,'base','hunter','Run more');	

# --- !Downs

drop table "USERS";

drop table "JOBS";
