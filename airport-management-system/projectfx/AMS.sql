create DATABASE AirportManagementSystem

Use AirportManagementSystem

create table Users (
userid INT PRIMARY KEY NOT NULL,
username varchar(255) NOT NULL,
password varchar(255) NOT NULL,
usertype int NOT NULL
)

select * FROM Users
select * FROM BoardingPass
select * FROM Booking

insert into Users values (1, 'admin', '123', 2)
delete from Users where userid=6

delete FROM Flight
CREATE TABLE Flight (
    Flight_ID INT PRIMARY KEY NOT NULL,
    date DATE NOT NULL,
    time VARCHAR(255) NOT NULL,
    Source_Location VARCHAR(255) NOT NULL,
    Destination_Location VARCHAR(255) NOT NULL,
    Airline_Name VARCHAR(255) NOT NULL,
    Available_Seats INT NOT NULL,
    price FLOAT NOT NULL -- Add	ed price column
);


select* FROM Flight

create table Airline 
(
airlineid INT PRIMARY KEY,
airlinename varchar(255),
)

create table Booking
(
bookingid INT PRIMARY KEY, 
passengerid int,
flightid int,
seattype varchar(255)
)
select* from BoardingPass
CREATE TABLE BoardingPass (
    id INT PRIMARY KEY , 
    passengerID int,
    flightID int 
);












-----------------------------INSERTIONS-----------------------------------------
delete FROM Airline


INSERT INTO Airline (airlineid, airlinename) 
VALUES 
(1, 'PIA'),
(2, 'Airblue'),
(3, 'Serene Air'),
(4, 'Gulf Air'),
(5, 'Flydubai'),
(6, 'Qatar Airways'),
(7, 'Emirates'),
(8, 'Pakistan Airways');

-- Karachi to Lahore
INSERT INTO Flight (Flight_ID, date, time, Source_Location, Destination_Location, Airline_Name, Available_Seats, price)
VALUES
(1, '2024-12-15', '14:30:00', 'Karachi', 'Lahore', 'PIA', 53, 15000.00),
(2, '2024-12-12', '12:30:00', 'Karachi', 'Lahore', 'Pakistan Airways', 221, 20000.00),
(3, '2024-12-14', '16:45:00', 'Karachi', 'Lahore', 'Airblue', 210, 18000.00),
(4, '2024-12-13', '10:00:00', 'Karachi', 'Lahore', 'Serene Air', 180, 16000.00),
(5, '2024-12-16', '18:15:00', 'Karachi', 'Lahore', 'Gulf Air', 150, 22000.00),
(6, '2024-12-17', '20:00:00', 'Karachi', 'Lahore', 'Flydubai', 130, 25000.00),
(7, '2024-12-18', '09:30:00', 'Karachi', 'Lahore', 'Qatar Airways', 200, 23000.00),
(8, '2024-12-19', '11:45:00', 'Karachi', 'Lahore', 'Emirates', 250, 28000.00),
(9, '2024-12-20', '14:00:00', 'Karachi', 'Lahore', 'PIA', 230, 15000.00);

-- Lahore to Karachi
INSERT INTO Flight (Flight_ID, date, time, Source_Location, Destination_Location, Airline_Name, Available_Seats, price)
VALUES
(10, '2024-12-15', '07:30:00', 'Lahore', 'Karachi', 'PIA', 200, 16000.00),
(11, '2024-12-16', '15:00:00', 'Lahore', 'Karachi', 'Airblue', 180, 19000.00),
(12, '2024-12-17', '08:45:00', 'Lahore', 'Karachi', 'Serene Air', 160, 17000.00),
(13, '2024-12-18', '17:00:00', 'Lahore', 'Karachi', 'Gulf Air', 250, 24000.00),
(14, '2024-12-19', '10:30:00', 'Lahore', 'Karachi', 'Flydubai', 220, 23000.00),
(15, '2024-12-20', '19:15:00', 'Lahore', 'Karachi', 'Qatar Airways', 190, 25000.00),
(16, '2024-12-21', '12:00:00', 'Lahore', 'Karachi', 'Emirates', 240, 28000.00),
(17, '2024-12-22', '14:45:00', 'Lahore', 'Karachi', 'Pakistan Airways', 210, 22000.00);

-- Islamabad to Lahore
INSERT INTO Flight (Flight_ID, date, time, Source_Location, Destination_Location, Airline_Name, Available_Seats, price)
VALUES
(18, '2024-12-15', '06:30:00', 'Islamabad', 'Lahore', 'PIA', 210, 12000.00),
(19, '2024-12-16', '09:00:00', 'Islamabad', 'Lahore', 'Airblue', 180, 15000.00),
(20, '2024-12-17', '11:30:00', 'Islamabad', 'Lahore', 'Serene Air', 150, 14000.00),
(21, '2024-12-18', '13:00:00', 'Islamabad', 'Lahore', 'Gulf Air', 230, 17000.00),
(22, '2024-12-19', '15:30:00', 'Islamabad', 'Lahore', 'Flydubai', 200, 19000.00),
(23, '2024-12-20', '17:15:00', 'Islamabad', 'Lahore', 'Qatar Airways', 190, 22000.00),
(24, '2024-12-21', '19:45:00', 'Islamabad', 'Lahore', 'Emirates', 240, 24000.00),
(25, '2024-12-22', '08:00:00', 'Islamabad', 'Lahore', 'Pakistan Airways', 220, 21000.00);

-- Lahore to Islamabad
INSERT INTO Flight (Flight_ID, date, time, Source_Location, Destination_Location, Airline_Name, Available_Seats, price)
VALUES
(26, '2024-12-23', '07:00:00', 'Lahore', 'Islamabad', 'PIA', 190, 14000.00),
(27, '2024-12-24', '08:45:00', 'Lahore', 'Islamabad', 'Airblue', 175, 16000.00),
(28, '2024-12-25', '10:30:00', 'Lahore', 'Islamabad', 'Serene Air', 160, 18000.00),
(29, '2024-12-26', '12:00:00', 'Lahore', 'Islamabad', 'Gulf Air', 230, 20000.00),
(30, '2024-12-27', '14:30:00', 'Lahore', 'Islamabad', 'Flydubai', 210, 21000.00);

-- Karachi to Islamabad
INSERT INTO Flight (Flight_ID, date, time, Source_Location, Destination_Location, Airline_Name, Available_Seats, price)
VALUES
(31, '2024-12-23', '09:00:00', 'Karachi', 'Islamabad', 'PIA', 220, 16000.00),
(32, '2024-12-24', '11:00:00', 'Karachi', 'Islamabad', 'Airblue', 180, 19000.00),
(33, '2024-12-25', '13:30:00', 'Karachi', 'Islamabad', 'Serene Air', 160, 18000.00),
(34, '2024-12-26', '16:00:00', 'Karachi', 'Islamabad', 'Gulf Air', 230, 21000.00),
(35, '2024-12-27', '18:00:00', 'Karachi', 'Islamabad', 'Flydubai', 210, 23000.00);

-- Islamabad to Karachi
INSERT INTO Flight (Flight_ID, date, time, Source_Location, Destination_Location, Airline_Name, Available_Seats, price)
VALUES
(36, '2024-12-23', '06:00:00', 'Islamabad', 'Karachi', 'PIA', 220, 16000.00),
(37, '2024-12-24', '08:30:00', 'Islamabad', 'Karachi', 'Airblue', 180, 19000.00),
(38, '2024-12-25', '11:00:00', 'Islamabad', 'Karachi', 'Serene Air', 160, 18000.00),
(39, '2024-12-26', '13:30:00', 'Islamabad', 'Karachi', 'Gulf Air', 230, 21000.00),
(40, '2024-12-27', '15:45:00', 'Islamabad', 'Karachi', 'Flydubai', 210, 23000.00);



