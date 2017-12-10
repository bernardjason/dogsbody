To use type

./sbt run

visit http://127.0.0.1:9000

First time you will see evolution screen, press "Apply this script"

<img src="/evolution.png" width="75%"/>

The play evolution will add a user bernard with a password jason

You will need to log on to add a task.

The project is intended as a small example play application using slick over a sqlite database. It uses a simple ActionBuilder for the authentication

Note that the project shows foreign keys working with sqlite. As well as configuring key in database setup 1.sql you also need this line in application.conf

slick.dbs.default.db.connectionInitSql="PRAGMA foreign_keys = ON"

To prove this works, log onto sqlite3
PRAGMA foreign_keys = ON;
delete from users;
