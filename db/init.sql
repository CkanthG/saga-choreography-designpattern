-- init.sql

CREATE DATABASE IF NOT EXISTS delivery;
CREATE DATABASE IF NOT EXISTS orders;
CREATE DATABASE IF NOT EXISTS payments;
CREATE DATABASE IF NOT EXISTS stock;

-- Optional: grant privileges to user for these databases
GRANT ALL PRIVILEGES ON delivery.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON orders.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON payments.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON stock.* TO 'user'@'%';

FLUSH PRIVILEGES;
