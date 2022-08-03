# Online Delivery System

## Description:
* This project is part of a student project to demonstrate the newly learned skills in Java and mySQL databases.
* This program captures new orders, also captures new restaurants and customers if they dont already exist. 
* The list of drivers is checked if a driver is available, if drivers are available, the driver with the lowest amount of deliveries in the area is allocated.
* An invoice is created once the load is finalised or the user can request to print a specific invoice number.

## Table of content methods build in the project:
1. "main" - Do while loop is created to action different methods as the user requires for changes in the database or output of data.
2. "enterNewOrder" - Method to add a new order to the databases and call on other methods if fields like Restaurant, Customer etc does not match.
3. "enterNewRestaurant" - Method to add a new Restaurant to the Restaurant database.
4. "enterNewCustomer" - Method to add a new Customer to the Customer database.
5. "enterNewDriver - Method to add a new Driver to the Driver database.
6. "checkAvailableDriver" - Method to check if a driver is available in the city and if there are drivers available, the name of the one with the least deliveries. Resturn Driver name.
7. "updateData" - Method to update data fields and call on other update methods if needed for the action.
8. "updateCustomerData" - Method to update data fields for Customer database.
9. "updateDriverData" - Method to update data fields for Driver database.
10. "updateRestaurantData" - Method to update data fields for Restaurant database.
11. "updateOrderData" - Method to update data fields for Orders database.
12. "searchData" - Method to search for data results by certain criteria.
13. "invoiceTextCreate" - Creating the text String that are to be used inside the invoice text file. Return String with text for text file.
14. "fileCreatorMethod" - Method to create a text file which is the invoice.
15. "displayCustomerResults" - Method to use the result set of selected Customer database and display in console.
16. "displayDriverResults" - Method to use the result set of selected Driver database and display in console.
17. "displayRestaurantResults" - Method to use the result set of selected Restaurant database and display in console.
18. "displayOrderResultsBasic" - Method to use the result set of selected Orders database and display in console some of the basic information regarding the order.
19. "displayOrderResults" - Method to use the result set of selected Orders database and display in console.
20. "displayFullOrdersResults" - Method to use the result set of selected Orders, Restaurant and Customer database and display in console.
21. "uploadDriversDatabase" - Method to use text files and upload multiple drivers from the text file to the Database.


## Installation
To place a copy of this work on your local computer, use the git cloning process.
* In command prompt change directory to where you wish the file to be placed.
* Enter the following command - git clone https://github.com/CarstensMartin/Delivery-System-Java-mySQL/
* Once cloned, open as a project in ecplise or your selected EDI. The file should be fully functional and can be edited to your requirements.

## How to use the project
*This project' code can also be modified to the requirements of your online delivery system:
![image](https://user-images.githubusercontent.com/)
// Image to be added once uploaded

// Image to be added once uploaded


## Autors of this project:
https://github.com/CarstensMartin
## Idea generated to create the web page:
* This page is part of a project from a course at http://hyperiondev.com/ 
* The purpose is to an online delivery system by applying the newly learned skills.
