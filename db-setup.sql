-- ============================================================
-- PayVault - Database setup for pgAdmin
-- Open pgAdmin -> connect to your local PostgreSQL server ->
-- right-click "Databases" -> Query Tool (on the server, NOT
-- inside an existing database) -> paste and run this file.
-- ============================================================

CREATE DATABASE auth_db;
CREATE DATABASE user_db;

-- Wallet Service and Transaction Service DBs (for after Review 1,
-- create these now if you want to get ahead):
-- CREATE DATABASE wallet_db;
-- CREATE DATABASE transaction_db;

-- No manual table creation needed — each Spring Boot service has
-- spring.jpa.hibernate.ddl-auto=update, so Hibernate creates the
-- tables automatically the first time you run it. After running
-- auth-service and user-service once, refresh the database in
-- pgAdmin's left tree and you'll see:
--   auth_db -> Schemas -> public -> Tables -> users
--   user_db -> Schemas -> public -> Tables -> user_profiles
