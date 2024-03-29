#!/bin/bash

# psql postgresql://${EXAMPLE_USERNAME}:${EXAMPLE_PASSWORD}@localhost:${EXAMPLE_PORT}/${EXAMPLE_DATABASE_BASIC} -f src/main/resources/setupPostgresqlBasic.sql
echo "PostgreSQL is Starting"
psql --username postgres << EOSQL
SET ROLE postgres;
CREATE ROLE "${EXAMPLE_USERNAME}" LOGIN PASSWORD '${EXAMPLE_PASSWORD}';
CREATE DATABASE "${EXAMPLE_DATABASE_BASIC}" OWNER "${EXAMPLE_USERNAME}";
CREATE DATABASE "${EXAMPLE_DATABASE_QUERY_EVOLUTION}" OWNER "${EXAMPLE_USERNAME}";
CREATE DATABASE "test" OWNER "${EXAMPLE_USERNAME}";

\c "${EXAMPLE_DATABASE_BASIC}";
SET ROLE "${EXAMPLE_USERNAME}";
\i setupPostgresqlBasic.sql;

\c "${EXAMPLE_DATABASE_QUERY_EVOLUTION}";
SET ROLE "${EXAMPLE_USERNAME}";
\i setupPostgresqlQueryEvolution.sql;

EOSQL
echo "PostgreSQL is ending - sleeping"