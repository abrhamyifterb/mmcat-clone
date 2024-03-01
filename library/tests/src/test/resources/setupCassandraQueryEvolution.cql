USE example_query_evolution;

DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS knows;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS ordered;

CREATE TABLE products (
    pid TEXT PRIMARY KEY,
    title TEXT,
    price TEXT
);

INSERT INTO products (pid, title, price) VALUES ('P5', 'Sourcery', '350');
INSERT INTO products (pid, title, price) VALUES ('P7', 'Pyramids', '275');

CREATE TABLE customers (
    id TEXT PRIMARY KEY,
    name TEXT,
    surname TEXT
);

INSERT INTO customers (id, name, surname) VALUES ('1', 'Mary', 'Smith');
INSERT INTO customers (id, name, surname) VALUES ('2', 'John', 'Newlin');
INSERT INTO customers (id, name, surname) VALUES ('3', 'Anne', 'Maxwell');

CREATE TABLE knows (
    id1 TEXT,
    id2 TEXT,
    PRIMARY KEY (id1, id2)
);

INSERT INTO knows (id1, id2) VALUES ('1', '2');
INSERT INTO knows (id1, id2) VALUES ('1', '3');

CREATE TABLE orders (
    oid TEXT PRIMARY KEY,
    customer_id TEXT,
    street TEXT,
    city TEXT,
    postCode TEXT
);

INSERT INTO orders (oid, customer_id, street, city, postCode) VALUES ('2023001', '1', 'Ke Karlovu', 'Prague', '110 00');
INSERT INTO orders (oid, customer_id, street, city, postCode) VALUES ('2023002', '2', 'Technická', 'Prague', '162 00');

CREATE TABLE items (
    oid TEXT,
    pid TEXT,
    price TEXT,
    quantity TEXT,
    PRIMARY KEY (oid, pid)
);

INSERT INTO items (oid, pid, price, quantity) VALUES ('2023001', 'P5', '350', '1');
INSERT INTO items (oid, pid, price, quantity) VALUES ('2023001', 'P7', '250', '1');
INSERT INTO items (oid, pid, price, quantity) VALUES ('2023002', 'P7', '275', '2');

CREATE TABLE ordered (
    customer_id TEXT,
    oid TEXT,
    PRIMARY KEY (customer_id, oid)
);

INSERT INTO ordered (customer_id, oid) VALUES ('1', '2023001');
INSERT INTO ordered (customer_id, oid) VALUES ('2', '2023002');
