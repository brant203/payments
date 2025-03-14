# Payments
A demo payments app for WEX

## features:
1. Store a purchase transaction in USD
2. Retrieve a purchase transaction
3. Exchange a purchase transaction to a different currency based on Treasury Reporting Rates of Exchange API

## Implementation:
I used Spring Web MVC to build a rest endpoint which can be used to perform all required interactions.

## Getting Started:
This project uses Docker for containerization. Docker is needed in order to start the application in containers
1. Check out the code
2. Run "docker-compose up"
   - This will start the mariadb container as well as the application
   - After the containers finish starting the application is ready to be used
  
##Build


## Endpoint documentation:

### **GET** `/api/purchaseTx/{id}`
This endpoint returns a purchase transaction from the database
Path Variables:
id - id of an existing purchase transaction

### **GET** `/api/purchaseTx/{id}/{currency}`
Returns a purchase transaction along with the exchange rate used and the amount after an exchange 
This endpoint makes a call to the Treasury Reporting Rates of Exchange API to get the exchange rate
Path Variables:
id - id of an existing purchase transaction
currency - Name of the currency that you want to convert the transaction amount to. This needs to be an exact match to the "Country-Currency Description" used for the Treasury Reporting Rates of Exchange API

### **POST** `/api/purchaseTx`
This endpoint takes a purchase transaction from the request body and adds it to the database
Request Body:
purchase transaction as application/json
example:
```
{
  "description": "apiTest",
  "transactionDate": 2025-01-01,
  "purchaseAmt": 69.72
}
```
Required fields:
1. purchaseAmt
   - Zero and positive numbers are accepted
   - Stored values will be to the nearest cent. The rounding will be done automatically if given a number with more than 2 decimal points
3. transactionDate
   - date format: yyyy-MM-dd
   - this is the date that will be used to find an exchange rate when an exchange request is made
Optional fields:
1. description
   - Text value up to 50 characters
