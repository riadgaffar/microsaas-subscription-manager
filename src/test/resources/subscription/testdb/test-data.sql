INSERT INTO authorities (name) VALUES ('ROLE_SUPERADMIN');
INSERT INTO authorities (name) VALUES ('ROLE_ADMIN');
INSERT INTO authorities (name) VALUES ('ROLE_USER');


INSERT INTO users (email, password) VALUES ('user1@example.com', '{bcrypt}$2a$10$EIoM3ay2dbNIW47wz0OSPuCWB0iGTxf4WCFbcGGhyar7sQeyOBOHe');
INSERT INTO users (email, password) VALUES ('user2@example.com', '{bcrypt}$2a$10$EIoM3ay2dbNIW47wz0OSPuCWB0iGTxf4WCFbcGGhyar7sQeyOBOHe');
INSERT INTO users (email, password) VALUES ('user3@example.com', '{bcrypt}$2a$10$EIoM3ay2dbNIW47wz0OSPuCWB0iGTxf4WCFbcGGhyar7sQeyOBOHe');
INSERT INTO users (email, password) VALUES ('user4@example.com', '{bcrypt}$2a$10$EIoM3ay2dbNIW47wz0OSPuCWB0iGTxf4WCFbcGGhyar7sQeyOBOHe');
INSERT INTO users (email, password) VALUES ('user5@example.com', '{bcrypt}$2a$10$EIoM3ay2dbNIW47wz0OSPuCWB0iGTxf4WCFbcGGhyar7sQeyOBOHe');
INSERT INTO users (email, password) VALUES ('user6@example.com', '{bcrypt}$2a$10$EIoM3ay2dbNIW47wz0OSPuCWB0iGTxf4WCFbcGGhyar7sQeyOBOHe');
INSERT INTO users (email, password) VALUES ('user7@example.com', '{bcrypt}$2a$10$EIoM3ay2dbNIW47wz0OSPuCWB0iGTxf4WCFbcGGhyar7sQeyOBOHe');
INSERT INTO users (email, password) VALUES ('user8@example.com', '{bcrypt}$2a$10$EIoM3ay2dbNIW47wz0OSPuCWB0iGTxf4WCFbcGGhyar7sQeyOBOHe');
INSERT INTO users (email, password) VALUES ('user9@example.com', '{bcrypt}$2a$10$EIoM3ay2dbNIW47wz0OSPuCWB0iGTxf4WCFbcGGhyar7sQeyOBOHe');
INSERT INTO users (email, password) VALUES ('user10@example.com', '{bcrypt}$2a$10$EIoM3ay2dbNIW47wz0OSPuCWB0iGTxf4WCFbcGGhyar7sQeyOBOHe');

INSERT INTO user_authorities (user_id, authority_id) VALUES (0, 0);
INSERT INTO user_authorities (user_id, authority_id) VALUES (1, 1);
INSERT INTO user_authorities (user_id, authority_id) VALUES (1, 2);
INSERT INTO user_authorities (user_id, authority_id) VALUES (2, 2);

INSERT INTO subscriptions (name, cost, renewal_date, user_id) VALUES ('Subscription 1', 9.99, '2024-05-01', 0);
INSERT INTO subscriptions (name, cost, renewal_date, user_id) VALUES ('Subscription 2', 19.99, '2024-06-01', 0);
INSERT INTO subscriptions (name, cost, renewal_date, user_id) VALUES ('Subscription 3', 29.99, '2024-07-01', 0);
INSERT INTO subscriptions (name, cost, renewal_date, user_id) VALUES ('Subscription 4', 3.99, '2024-08-01', 0);

INSERT INTO subscriptions (name, cost, renewal_date, user_id) VALUES ('Subscription 1', 9.99, '2024-05-01', 1);
INSERT INTO subscriptions (name, cost, renewal_date, user_id) VALUES ('Subscription 2', 19.99, '2024-06-01', 1);
INSERT INTO subscriptions (name, cost, renewal_date, user_id) VALUES ('Subscription 3', 29.99, '2024-07-01', 1);
INSERT INTO subscriptions (name, cost, renewal_date, user_id) VALUES ('Subscription 4', 3.99, '2024-08-01', 1);

INSERT INTO subscriptions (name, cost, renewal_date, user_id) VALUES ('Subscription 1', 9.99, '2024-05-01', 2);
INSERT INTO subscriptions (name, cost, renewal_date, user_id) VALUES ('Subscription 2', 19.99, '2024-06-01', 2);
INSERT INTO subscriptions (name, cost, renewal_date, user_id) VALUES ('Subscription 3', 29.99, '2024-07-01', 2);
INSERT INTO subscriptions (name, cost, renewal_date, user_id) VALUES ('Subscription 4', 3.99, '2024-08-01', 2);
