FROM cassandra:latest

COPY docker/cassandra/entrypoint.sh /entrypoint.sh
COPY library/tests/src/test/resources/setupCassandraBasic.cql /
COPY library/tests/src/test/resources/setupCassandraQueryEvolution.cql /
