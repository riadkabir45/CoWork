
## PostgreSQL Setup Commands

Run these commands in the psql prompt to set up your local database and user:

```
CREATE USER "postgres.ubkhyjduerbjudoirhcx" WITH PASSWORD '#%xa&LecdTkFQ5s';
CREATE DATABASE postgres OWNER "postgres.ubkhyjduerbjudoirhcx";
GRANT ALL PRIVILEGES ON DATABASE postgres TO "postgres.ubkhyjduerbjudoirhcx";
GRANT CREATE ON SCHEMA public TO "postgres.ubkhyjduerbjudoirhcx";
GRANT USAGE ON SCHEMA public TO "postgres.ubkhyjduerbjudoirhcx";
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO "postgres.ubkhyjduerbjudoirhcx";
```

You can enter the psql prompt with:
```
sudo -u postgres psql
```
