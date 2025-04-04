create database expensetracker;
use expensetracker;

create table expenses(
    id int primary key auto_increment,
    name varchar(100),
    amount decimal(10,2),
    category varchar(50),
    date date
)