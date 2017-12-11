# --- !Ups


create table "USERS" ("ID" SERIAL PRIMARY KEY,"USER" VARCHAR NOT NULL,"PASSWORD" VARCHAR NOT NULL,"NICKNAME" VARCHAR NOT NULL,"ROLE" VARCHAR);

insert into "USERS" ("ID","USER","PASSWORD","NICKNAME") values (1,'bernard','jason','Dont call me Bernie');	
insert into "USERS" ("ID","USER","PASSWORD","NICKNAME","ROLE") values (2,'admin','admin','super user','admin');	

create table "JOBS" (
	"ID" SERIAL PRIMARY KEY,
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
	values(1, 1,'Dont call me Bernie',200,10,30,current_timestamp,current_timestamp,1);


# --- !Downs

drop table "JOBS";

drop table "USERS";
