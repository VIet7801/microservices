-- Local development credentials; executed only on an empty PostgreSQL volume.
CREATE USER order_app WITH PASSWORD 'order_dev';
CREATE DATABASE order_db OWNER order_app;
CREATE USER inventory_app WITH PASSWORD 'inventory_dev';
CREATE DATABASE inventory_db OWNER inventory_app;
CREATE USER payment_app WITH PASSWORD 'payment_dev';
CREATE DATABASE payment_db OWNER payment_app;
