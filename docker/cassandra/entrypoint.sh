#!/bin/bash

CQLSH="cqlsh"

$CQLSH -e "CREATE ROLE IF NOT EXISTS $EXAMPLE_USERNAME WITH PASSWORD = '$EXAMPLE_PASSWORD' AND LOGIN = TRUE;"
$CQLSH -e "ALTER ROLE $EXAMPLE_USERNAME WITH SUPERUSER = true;"

$CQLSH -e "CREATE KEYSPACE IF NOT EXISTS $EXAMPLE_DATABASE_BASIC WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};"
$CQLSH -e "CREATE KEYSPACE IF NOT EXISTS $EXAMPLE_DATABASE_QUERY_EVOLUTION WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};"
$CQLSH -e "CREATE KEYSPACE IF NOT EXISTS test WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};"

$CQLSH -k $EXAMPLE_DATABASE_BASIC -f setupCassandraBasic.cql
$CQLSH -k $EXAMPLE_DATABASE_QUERY_EVOLUTION -f setupCassandraQueryEvolution.cql

