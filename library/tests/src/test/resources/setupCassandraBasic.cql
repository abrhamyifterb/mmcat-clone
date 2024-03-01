USE example_basic;

DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS order_items;

CREATE TABLE orders (
    number TEXT PRIMARY KEY
);

INSERT INTO orders (number) VALUES ('o_100');
INSERT INTO orders (number) VALUES ('o_200');

CREATE TABLE products (
    id TEXT PRIMARY KEY,
    label TEXT,
    price TEXT
);

INSERT INTO products (id, label, price) VALUES ('123', 'Clean Code', '125');
INSERT INTO products (id, label, price) VALUES ('765', 'The Lord of the Rings', '199');
INSERT INTO products (id, label, price) VALUES ('457', 'The Art of War', '299');
INSERT INTO products (id, label, price) VALUES ('734', 'Animal Farm', '350');

CREATE TABLE order_items (
    order_number TEXT,
    product_id TEXT,
    quantity TEXT,
    PRIMARY KEY (order_number, product_id)
);

INSERT INTO order_items (order_number, product_id, quantity) VALUES ('o_100', '123', '1');
INSERT INTO order_items (order_number, product_id, quantity) VALUES ('o_100', '765', '2');
INSERT INTO order_items (order_number, product_id, quantity) VALUES ('o_200', '457', '7');
INSERT INTO order_items (order_number, product_id, quantity) VALUES ('o_200', '734', '3');
