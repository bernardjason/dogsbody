# --- !Ups

create table "USERS" ("ID" INTEGER PRIMARY KEY,"USER" VARCHAR NOT NULL,"PASSWORD" VARCHAR NOT NULL,"NICKNAME" VARCHAR NOT NULL,ROLE VARCHAR);

create table "TASKS" ("ID" INTEGER PRIMARY KEY, 
					"NAME" VARCHAR NOT NULL,
					"CODE" VARCHAR NOT NULL
					 );

create table "TIME_ENTRY" (
	"ID" INTEGER PRIMARY KEY,
	"USER_ID" INTEGER NOT NULL,
	"USER" VARCHAR NOT NULL, 
	"WHEN" TIMESTAMP NOT NULL,
	"TASK_ID" INTEGER NOT NULL,
	"TASK" VARCHAR NOT NULL,    
	"EFFORT" REAL NOT NULL , 
	FOREIGN KEY("TASK_ID") REFERENCES "TASKS"("ID"),
	FOREIGN KEY("USER_ID") REFERENCES "USERS"("ID"));

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

insert into "TASKS" ("ID","NAME","CODE") values (1,'TEA','123-456');	
insert into "TASKS" ("ID","NAME","CODE") values (2,'CODE','222-444');	
insert into "TASKS" ("ID","NAME","CODE") values (3,'LUNCH','111-111');	
insert into "TASKS" ("ID","NAME","CODE") values (4,'BUGS','222-444');
insert into "TASKS" ("ID","NAME","CODE") values (5,'TRAVEL','OH1234');
insert into "TASKS" ("ID","NAME","CODE") values (6,'HOLIDAY','OH1001');
insert into "TASKS" ("ID","NAME","CODE") values (7,'SUPPORT','SUP0001');

insert into "USERS" ("ID","USER","PASSWORD","NICKNAME") values (1,'bernard','jason','Dont call me Bernie');	
insert into "USERS" ("ID","USER","PASSWORD","NICKNAME","ROLE") values (2,'admin','admin','super user',"admin");
insert into "USERS" ("ID","USER","PASSWORD","NICKNAME") values (3,'base','hunter','Run more');	

insert into "TIME_ENTRY" ("USER_ID","USER","WHEN","TASK_ID","TASK","EFFORT") 
	values (1, "Dont call me Bernie",strftime('%s', 'now') ,1,"TEA","0.20");

# --- !Downs

drop table "TIME_ENTRY";

drop table "TASKS";

drop table "USERS";

drop table "JOBS";
