package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.Formatter;
import java.util.Scanner;

public class Main {

	/** Initialize the scanner function */
	static Scanner scan = new Scanner(System.in);
	
	/**Main */
	public static void main(String[] args) {
		// Use a try statement to login and work on database
		try {
			/**
			 * Connect to the QuickFoodMS database, via the jdbc:mysql:channel on localhost
			 * (this PC) Use username "otheruser", password "swordfish".
			 */
			Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/QuickFoodMS?useSSL=false",
					"otheruser", "swordfish");

			// Create a direct line to the database for running our queries
			Statement statement = connection.createStatement();

			/**
			 * Run a do While loop for all the options to call methods to action on the
			 * database
			 */
			// Declare true for do loop to run
			boolean continueDo = true;

			do {
				System.out.println("\nWhat would you like to do? \n" + "1. Enter a New Order\n"
						+ "2. Update information\n" + "3. Search information\n" + "4. Print Invoice\n"
						+ "5. Add a new Driver\n" + "6. Upload drivers From Text file to Database\n" + "0. Exit");

				// Scan the answer given
				String choice = scan.nextLine();

				// Switch statement to execute method according to answer given
				switch (choice) {

				case "0":
					// End the while loop
					continueDo = false;
					break;
				case "1":
					enterNewOrder(statement);
					break;
				case "2":
					updateData(statement);
					break;
				case "3":
					searchData(statement);
					break;
				case "4":
					// Print by Order Number
					System.out.print("Enter Order Number:");
					int orderNumber = scan.nextInt();
					scan.nextLine();

					ResultSet results = statement
							.executeQuery("SELECT * FROM Orders WHERE OrderNumber =" + orderNumber);
					if (results.next() == true) {

						results = statement.executeQuery("SELECT * FROM Orders WHERE OrderNumber =" + orderNumber);
						results.next();

						// See if there is a driver allocated to see if an invoice can be created
						String driver = results.getString("Driver");

						if (!driver.equals("null")) {
							results = statement.executeQuery("SELECT * FROM Orders"
									+ " INNER JOIN Restaurant ON Orders.Restaurant = Restaurant.Name"
									+ " INNER JOIN Driver ON Orders.Driver = Driver.Name"
									+ " INNER JOIN Customer ON Orders.Customer = Customer.Name" + " WHERE OrderNumber ="
									+ orderNumber);
							String invoiceText = invoiceTextCreate(results);
							fileCreatorMethod(orderNumber, invoiceText);
						} else {
							String invoiceText = "Sorry! \r\nOur drivers are too far away from you to be able to deliver to your location.";
							fileCreatorMethod(orderNumber, invoiceText);
						}

					} else {
						// Display message if order number not found
						System.out.println("Order Number not found");
					}

					break;
				case "5":
					enterNewDriver(statement, null, null);
					break;

				case "6":
					uploadDriversDatabase(statement);
					break;

				// Default if user entered a wrong character
				default:
					System.out.println("\nEnter a correct number:");
					break;
				}

			}
			// Continue the "do" function N or n is not entered.
			while (continueDo == true);

			// Close up our connections
			statement.close();
			connection.close();

		} catch (SQLException e) {
			// We only want to catch a SQLException - anything else is off-limits for now.
			e.printStackTrace();
		}

		System.out.println("Goodbye!");

		// Close scanner
		scan.close();

	}

	/** Method to insert a new Order */
	public static void enterNewOrder(Statement statement) throws SQLException {
		// Prompt user information for new Order

		System.out.println("Which City do you want to order from?:\n" + "1. Bloemfontein\n" + "2. Cape Town\n"
				+ "3. Durban\n" + "4. Johannesburg\n" + "5. Port Elizabeth\n" + "6. Potchefstroom\n" + "7. Springbok\n"
				+ "8. Witbank\n" + "9. Other");
		String city = scan.nextLine();

		// Switch statement according to answer given
		switch (city) {

		case "1":
			city = "Bloemfontein";
			break;
		case "2":
			city = "Cape Town";
			break;
		case "3":
			city = "Durban";
			break;
		case "4":
			city = "Johannesburg";
			break;
		case "5":
			city = "Port Elizabeth";
			break;
		case "6":
			city = "Potchefstroom";
			break;
		case "7":
			city = "Springbok";
			break;
		case "8":
			city = "Witbank";
			break;
		case "9":
			System.out.print("Which Other city do you want to order from?:");
			city = scan.nextLine();
			break;

		// Default if user entered a wrong character
		default:
			System.out.println("\nWrong input - No Order was added\n");
			city = "Wrong Selected";
			break;
		}

		// If valid choice is made, continue to add order
		if (city != "Wrong Selected") {

			System.out.print("Please enter Customer Name (Name & Surname):");
			String customer = scan.nextLine();

			/**
			 * Check if the Name of the Customer exist in the Customer database, if not,
			 * capture into database
			 */
			ResultSet customerExist = statement
					.executeQuery("SELECT Name FROM Customer WHERE Name = '" + customer + "'");
			if (customerExist.next() == false) {
				enterNewCustomer(statement, customer, city);
			}

			/**
			 * Check the available drivers and allocate to Order if a driver is available
			 */

			String driver = checkAvailableDriver(statement, city);
			int loads = 0;

			// Check the amount of loads the driver has if a driver is available
			if (driver != null) {
				ResultSet drivers = statement.executeQuery("SELECT * FROM Driver WHERE Name ='" + driver + "'");
				drivers.next();
				loads = drivers.getInt("Deliveries");

			}
			loads++;

			System.out.print("Please enter Restaurant Name:");
			String restaurant = scan.nextLine();

			/**
			 * Check if the Name of the Restaurant exist in the Restaurant database, if not,
			 * capture into database
			 */
			ResultSet restaurantExist = statement
					.executeQuery("SELECT Name FROM Restaurant WHERE Name = '" + restaurant + "'");
			if (restaurantExist.next() == false) {
				enterNewRestaurant(statement, restaurant, city);
			}

			System.out.print("Do you want to order 1 or 2 items \nEnter number of items:");
			int numberOfItems = scan.nextInt();
			scan.nextLine();

			System.out.print("Name of the First item that you want to order:");
			String itemOne = scan.nextLine();

			System.out.print("Quantity of the First item:");
			int quantityOne = scan.nextInt();
			scan.nextLine();

			System.out.print("Price of First item:");
			double priceOne = scan.nextDouble();
			scan.nextLine();

			String itemTwo = null;
			int quantityTwo = 0;
			double priceTwo = 0;

			if (numberOfItems == 2) {
				System.out.print("Name of the Second item that you want to order:");
				itemTwo = scan.nextLine();

				System.out.print("Quantity of the Second item:");
				quantityTwo = scan.nextInt();
				scan.nextLine();

				System.out.print("Price of Second item:");
				priceTwo = scan.nextDouble();
				scan.nextLine();
			}

			System.out.print("Please enter Special Instructions for this order:");
			String specialInstructions = scan.nextLine();

			boolean finalised = false;

			// INSERT INTO Orders
			statement.executeUpdate(
					"INSERT INTO Orders (Customer, Driver, Restaurant, ItemOne, QuantityOne, PriceOne, ItemTwo, QuantityTwo, PriceTwo, SpecialInstructions, Finalised) VALUES"
							+ "('" + customer + "','" + driver + "','" + restaurant + "','" + itemOne + "',"
							+ quantityOne + "," + priceOne + ",'" + itemTwo + "'," + quantityTwo + "," + priceTwo + ",'"
							+ specialInstructions + "'," + finalised + ")");

			if (driver != null) {
				// UPDATE Drivers
				statement.executeUpdate("UPDATE Driver SET Deliveries =" + loads + " WHERE Name = '" + driver + "'");
			}

			System.out.println("New Order is captured successfully\n");
		}

	}

	/** Method to insert a Restaurant */
	public static void enterNewRestaurant(Statement statement, String name, String city) throws SQLException {
		System.out.println("\nAdding Restaurant information:\n");

		// Get variables to add to Restaurant
		if (name == null) {
			System.out.print("Please enter Restaurant Name:");
			name = scan.nextLine();
		}

		// Check if Name exist in the Restaurant database, if not, capture into database
		ResultSet nameExist = statement.executeQuery("SELECT * FROM Restaurant WHERE Name = '" + name + "'");
		if (nameExist.next() == false) {

			System.out.print("Please enter Restaurant Telephone number:");
			String contactNumber = scan.nextLine();

			// INSERT INTO Restaurant
			statement.executeUpdate(
					"INSERT INTO Restaurant VALUES('" + name + "','" + city + "','" + contactNumber + "')");
		} else {
			System.out.println("Restaurant name already exists");
		}
	}

	/** Method to insert a Customer */
	public static void enterNewCustomer(Statement statement, String name, String city) throws SQLException {
		System.out.println("\nAdding Customer information:\n");

		// Get variables to add to Customer
		if (name == null) {
			System.out.print("Please enter Customer Name:");
			name = scan.nextLine();
		}

		// Check if Name exist in the Customer database, if not, capture into database
		ResultSet nameExist = statement.executeQuery("SELECT * FROM Customer WHERE Name = '" + name + "'");
		if (nameExist.next() == false) {

			System.out.print("Please enter Customer Telephone number:");
			String contactNumber = scan.nextLine();

			System.out.print("Please enter Customer Street Address:");
			String streetAddress = scan.nextLine();

			System.out.print("Please enter Customer Suburb:");
			String suburb = scan.nextLine();

			System.out.print("Please enter Customer Email:");
			String email = scan.nextLine();

			// INSERT INTO Customer
			statement.executeUpdate("INSERT INTO Customer VALUES('" + name + "','" + contactNumber + "','"
					+ streetAddress + "','" + suburb + "','" + city + "','" + email + "')");
		} else {
			System.out.println("Customer name already exists");
		}
	}

	/** Method to insert a Driver */
	public static void enterNewDriver(Statement statement, String name, String city) throws SQLException {
		System.out.println("\nAdding Driver information:\n");

		if (name == null) {
			System.out.print("Please enter Driver Name:");
			name = scan.nextLine();
		}

		// Check if Name exist in the Driver database, if not, capture into database
		ResultSet nameExist = statement.executeQuery("SELECT * FROM Driver WHERE Name = '" + name + "'");
		if (nameExist.next() == false) {

			if (city == null) {
				System.out.println("Which City is the driver Operating From?:" + "1. Bloemfontein\n" + "2. Cape Town\n"
						+ "3. Durban\n" + "4. Johannesburg\n" + "5. Port Elizabeth\n" + "6. Potchefstroom\n"
						+ "7. Springbok\n" + "8. Witbank\n" + "9. Other");
				city = scan.nextLine();

				// Switch statement according to answer given
				switch (city) {

				case "1":
					city = "Bloemfontein";
					break;
				case "2":
					city = "Cape Town";
					break;
				case "3":
					city = "Durban";
					break;
				case "4":
					city = "Johannesburg";
					break;
				case "5":
					city = "Port Elizabeth";
					break;
				case "6":
					city = "Potchefstroom";
					break;
				case "7":
					city = "Springbok";
					break;
				case "8":
					city = "Witbank";
					break;
				case "9":
					System.out.print("Which Other city is the driver in?:");
					city = scan.nextLine();
					break;

				// Default if user entered a wrong character
				default:
					System.out.println("\nWrong input - No Driver was added\n");
					city = "Wrong Selected";
					break;

				}
			}

			if (city != "Wrong Selected") {
				// Start with no deliveries
				int deliveries = 0;
				// INSERT INTO Driver
				statement.executeUpdate("INSERT INTO Driver VALUES('" + name + "','" + city + "'," + deliveries + ")");
			}

		} else {
			System.out.println("Driver name already exists");
		}
	}

	/** Check if a driver is available in the city @return driver with least loads or null if no driver available*/
	public static String checkAvailableDriver(Statement statement, String city) throws SQLException {

		ResultSet drivers = statement.executeQuery("SELECT * FROM Driver WHERE City ='" + city + "'");

		// Start with a high number and get driver with lowest loads
		int loads = 1000000;
		String driverName = null;

		// Get lowest loads
		while (drivers.next()) {
			if (drivers.getInt("Deliveries") < loads) {
				driverName = drivers.getString("Name");
			}
		}
		return driverName;
	}

	/** Method to Update Data */
	public static void updateData(Statement statement) throws SQLException {

		System.out.println(
				"\nWhich item do you want to update? \n1. Finalize Last Order captured \n2. Finalize Order by Order number \n"
						+ "3. Update Customer Information \n4. Update Driver Information \n5. Update Restaurant Information \n6. Update Order Information \n0. Back to Main Screen");
		String itemUpdate = scan.nextLine();
		int orderNumber;

		// Switch statement according to answer given
		switch (itemUpdate) {

		case "0":
			// Back to Main Screen
			break;

		case "1":
			// Finalize Last Order
			ResultSet lastOrder = statement.executeQuery("SELECT MAX(OrderNumber) FROM Orders");
			lastOrder.next();
			orderNumber = lastOrder.getInt("MAX(OrderNumber)");

			statement.executeUpdate(
					"UPDATE Orders SET Finalised = True, CompletionDate = DATE(NOW()) WHERE OrderNumber = "
							+ orderNumber);

			ResultSet results = statement.executeQuery("SELECT * FROM Orders WHERE OrderNumber =" + orderNumber);
			results.next();

			String driver = results.getString("Driver");

			if (!driver.equals("null")) {
				// Create invoice for finalized order
				results = statement.executeQuery(
						"SELECT * FROM Orders" + " INNER JOIN Restaurant ON Orders.Restaurant = Restaurant.Name"
								+ " INNER JOIN Driver ON Orders.Driver = Driver.Name"
								+ " INNER JOIN Customer ON Orders.Customer = Customer.Name" + " WHERE OrderNumber ="
								+ orderNumber);
				String invoiceText = invoiceTextCreate(results);
				fileCreatorMethod(orderNumber, invoiceText);
			} else {
				// Create invoice stating no driver available
				String invoiceText = "Sorry! \r\nOur drivers are too far away from you to be able to deliver to your location.";
				fileCreatorMethod(orderNumber, invoiceText);
			}

			break;

		case "2":
			// Finalize Order by Order number
			System.out.print("Please enter Order Number to be Finalized:");
			orderNumber = scan.nextInt();
			scan.nextLine();

			ResultSet selectOrder = statement
					.executeQuery("SELECT OrderNumber FROM Orders WHERE OrderNumber =" + orderNumber);
			if (selectOrder.next() == true) {
				statement.executeUpdate(
						"UPDATE Orders SET Finalised = True, CompletionDate = DATE(NOW()) WHERE OrderNumber="
								+ orderNumber);
				System.out.println("\nOrder number: " + orderNumber + " was updated to finalised today");

				results = statement.executeQuery("SELECT * FROM Orders WHERE OrderNumber =" + orderNumber);
				results.next();

				driver = results.getString("Driver");

				if (!driver.equals("null")) {
					// Create invoice for finalized order
					results = statement.executeQuery(
							"SELECT * FROM Orders" + " INNER JOIN Restaurant ON Orders.Restaurant = Restaurant.Name"
									+ " INNER JOIN Driver ON Orders.Driver = Driver.Name"
									+ " INNER JOIN Customer ON Orders.Customer = Customer.Name" + " WHERE OrderNumber ="
									+ orderNumber);
					String invoiceText = invoiceTextCreate(results);
					fileCreatorMethod(orderNumber, invoiceText);
				} else {
					// Create invoice stating no driver available
					String invoiceText = "Sorry! \r\nOur drivers are too far away from you to be able to deliver to your location.";
					fileCreatorMethod(orderNumber, invoiceText);
				}

			} else {
				System.out.println("\nNo Data was updated, Order Number not found");
			}
			break;

		case "3":
			// Update Customer Data:
			updateCustomerData(statement);
			break;

		case "4":
			// Update Driver Data:
			updateDriverData(statement);
			break;

		case "5":
			// Update Restaurant Data:
			updateRestaurantData(statement);
			break;

		case "6":
			// Update Order Data:
			updateOrderData(statement);
			break;

		// Default if user entered a wrong character
		default:
			System.out.println("\nProject was not ammended\n");
			break;
		}

	}

	/** Method to update Customer data*/
	public static void updateCustomerData(Statement statement) throws SQLException {

		// Ask if user knows which Customer they want to amend
		System.out.println("Do you know which Customer you want to update by Name? \n1. YES \n2. NO");
		int userKnows = scan.nextInt();
		scan.nextLine();

		// If No, launch search method
		if (userKnows != 1) {
			searchData(statement);
		}

		// User enters Customer Name
		System.out.print("\nPlease enter Customer Name:");
		String name = scan.nextLine();

		ResultSet results = statement.executeQuery("SELECT * FROM Customer WHERE Name = '" + name + "'");

		// If Name exits, can make amendments
		if (results.next() == true) {

			ResultSet resultShow = statement.executeQuery("SELECT * FROM Customer WHERE Name = '" + name + "'");
			displayCustomerResults(resultShow);

			System.out.println(
					"\nWhich item do you want to update? \n1. Contact number \n2. Street Address \n3. Suburb \n4. Email \n0. Back to Main Screen");
			String itemUpdate = scan.nextLine();

			// Switch statement according to answer given
			switch (itemUpdate) {

			case "0":
				// Back to Main Screen
				break;

			case "1":
				// UPDATE the Contact Number:
				System.out.print("Please enter new Contact Number for the Customer:");
				String contactNumber = scan.nextLine();
				statement.executeUpdate(
						"UPDATE Customer SET ContactNumber ='" + contactNumber + "' WHERE Name = '" + name + "'");
				break;

			case "2":
				// UPDATE the Street Address:
				System.out.print("Please enter new Street Address for the Customer:");
				String streetAddress = scan.nextLine();
				statement.executeUpdate(
						"UPDATE Customer SET StreetAddress ='" + streetAddress + "' WHERE Name = '" + name + "'");
				break;

			case "3":
				// UPDATE the Suburb:
				System.out.print("Please enter new Suburb for the Customer:");
				String suburb = scan.nextLine();
				statement.executeUpdate("UPDATE Customer SET Suburb ='" + suburb + "' WHERE Name = '" + name + "'");
				break;

			case "4":
				// Update Email
				System.out.print("Please enter Customer Email:");
				String email = scan.nextLine();
				statement.executeUpdate("UPDATE Customer SET Email = '" + email + "' WHERE Name = '" + name + "'");
				break;

			// Default if user entered a wrong character
			default:
				System.out.println("\nCustomer was not ammended\n");
				break;

			}
		}

	}

	/** Method to update Driver data*/
	public static void updateDriverData(Statement statement) throws SQLException {

		// Ask if user knows which Driver they want to amend
		System.out.println("Do you know which Driver you want to update by Name? \n1. YES \n2. NO");
		int userKnows = scan.nextInt();
		scan.nextLine();

		// If No, launch search method
		if (userKnows != 1) {
			searchData(statement);
		}

		// User enters Driver Name
		System.out.print("\nPlease enter Driver Name:");
		String name = scan.nextLine();

		ResultSet results = statement.executeQuery("SELECT * FROM Driver WHERE Name = '" + name + "'");

		// If Name exits, can make amendments
		if (results.next() == true) {

			ResultSet resultShow = statement.executeQuery("SELECT * FROM Driver WHERE Name = '" + name + "'");
			displayDriverResults(resultShow);

			System.out.println(
					"\nWhich item do you want to update? \n1. Driver City \n2. Number of loads \n0. Back to Main Screen");
			String itemUpdate = scan.nextLine();

			// Switch statement according to answer given
			switch (itemUpdate) {

			case "0":
				// Back to Main Screen
				break;

			case "1":
				// UPDATE the City:
				System.out.print("Please enter new City for the Driver:");
				String city = scan.nextLine();
				statement.executeUpdate("UPDATE Driver SET City ='" + city + "' WHERE Name = '" + name + "'");
				System.out.println("The following Orders are affected by this change:");
				ResultSet resultShowAffected = statement
						.executeQuery("SELECT * FROM Orders WHERE Driver = '" + name + "'");
				displayOrderResultsBasic(resultShowAffected);

				break;

			case "2":
				// UPDATE the Number of loads:
				System.out.print("Please enter the ammended number of deliveries for the driver:");
				int deliveries = scan.nextInt();
				scan.nextLine();
				statement.executeUpdate("UPDATE Driver SET Deliveries =" + deliveries + " WHERE Name = '" + name + "'");
				break;

			// Default if user entered a wrong character
			default:
				System.out.println("\nDriver was not ammended\n");
				break;

			}
		}

	}

	/** Method to update Restaurant data*/
	public static void updateRestaurantData(Statement statement) throws SQLException {

		// Ask if user knows which Restaurant they want to amend
		System.out.println("Do you know which Restaurant you want to update by Name? \n1. YES \n2. NO");
		int userKnows = scan.nextInt();
		scan.nextLine();

		// If No, launch search method
		if (userKnows != 1) {
			searchData(statement);
		}

		// User enters Restaurant Name
		System.out.print("\nPlease enter Restaurant Name:");
		String name = scan.nextLine();

		ResultSet results = statement.executeQuery("SELECT * FROM Restaurant WHERE Name = '" + name + "'");

		// If Name exits, can make amendments
		if (results.next() == true) {

			ResultSet resultShow = statement.executeQuery("SELECT * FROM Restaurant WHERE Name = '" + name + "'");
			displayRestaurantResults(resultShow);

			System.out.println(
					"\nWhich item do you want to update? \n1. Restaurant City \n2. Contact Number \n0. Back to Main Screen");
			String itemUpdate = scan.nextLine();

			// Switch statement according to answer given
			switch (itemUpdate) {

			case "0":
				// Back to Main Screen
				break;

			case "1":
				// UPDATE the City:
				System.out.print("Please enter new City for the Restaurant:");
				String city = scan.nextLine();
				statement.executeUpdate("UPDATE Restaurant SET City ='" + city + "' WHERE Name = '" + name + "'");
				System.out.println("The following Orders are affected by this change:");
				ResultSet resultShowAffected = statement
						.executeQuery("SELECT * FROM Orders WHERE Restaurant = '" + name + "'");
				displayOrderResultsBasic(resultShowAffected);

				break;

			case "2":
				// UPDATE the Contact Number:
				System.out.print("Please enter the new Contact Number:");
				String contactNumber = scan.nextLine();
				statement.executeUpdate(
						"UPDATE Restaurant SET ContactNumber = '" + contactNumber + "' WHERE Name = '" + name + "'");
				break;

			// Default if user entered a wrong character
			default:
				System.out.println("\nRestaurant was not ammended\n");
				break;

			}
		}

	}

	/** Method to update Order data*/
	public static void updateOrderData(Statement statement) throws SQLException {

		// Ask if user knows which Order Number they want to amend
		System.out.println("Do you know which Order Number you want to update? \n1. YES \n2. NO");
		int userKnows = scan.nextInt();
		scan.nextLine();

		// If No, launch search method
		if (userKnows != 1) {
			searchData(statement);
		}

		// User enters Order Number
		System.out.print("\nPlease enter Order Number:");
		int orderNumber = scan.nextInt();
		scan.nextLine();

		ResultSet results = statement.executeQuery("SELECT * FROM Orders WHERE OrderNumber = " + orderNumber);

		// If Name exits, can make amendments
		if (results.next() == true) {

			ResultSet resultShow = statement.executeQuery("SELECT * FROM Orders WHERE OrderNumber = " + orderNumber);
			displayOrderResults(resultShow);

			// Get the Customer Name and City of the Order
			resultShow = statement.executeQuery("SELECT * FROM Orders WHERE OrderNumber = " + orderNumber);
			resultShow.next();
			String customerName = resultShow.getString("Customer");

			ResultSet customerCityResults = statement
					.executeQuery("SELECT City FROM Customer WHERE  Name = '" + customerName + "'");
			customerCityResults.next();
			String city = customerCityResults.getString("City");

			System.out.println(
					"\nWhich item do you want to update? \n1. Customer \n2. Driver \n3. Restaurant \n4. Item One Ammendments"
							+ "\n5. Item Two Ammendments \n6. Special Instructions \n7. Completion Date  \n0. Back to Main Screen");
			String itemUpdate = scan.nextLine();

			// Switch statement according to answer given
			switch (itemUpdate) {

			case "0":
				// Back to Main Screen
				break;

			case "1":
				// UPDATE the Customer:
				System.out.print("Please enter new Customer Name:");
				String newCustomer = scan.nextLine();
				statement.executeUpdate(
						"UPDATE Orders SET Customer ='" + newCustomer + "' WHERE OrderNumber = " + orderNumber);

				ResultSet customerResults = statement
						.executeQuery("SELECT * FROM Customer WHERE Name = '" + newCustomer + "'");

				// If New Name does not exist, add new customer
				if (customerResults.next() == false) {

					System.out.println("Adding the new Customer " + newCustomer + " to the database:");

					enterNewCustomer(statement, newCustomer, city);
				}
				break;

			case "2":
				// UPDATE the Driver:
				System.out.println("Available drivers in the City:");

				ResultSet resultAvailableDriversShow = statement
						.executeQuery("SELECT * FROM Driver WHERE City = '" + city + "'");
				displayDriverResults(resultAvailableDriversShow);

				// Get the Name of Current Driver & Amount of Deliveries
				results = statement.executeQuery("SELECT * FROM Orders WHERE OrderNumber = " + orderNumber);
				results.next();
				String currentDriver = results.getString("Driver");

				System.out.print("\nPlease enter the Name of the selected Driver:");
				String driver = scan.nextLine();

				// Check if driver name exist, if not create Driver
				ResultSet resultNewDriverDeliveries = statement
						.executeQuery("SELECT * FROM Driver WHERE Name = '" + driver + "'");
				if (resultNewDriverDeliveries.next() == false) {
					enterNewDriver(statement, driver, city);
				}

				// Plus the new driver a load
				statement.executeUpdate("UPDATE Driver SET Deliveries = Deliveries +1 WHERE Name = '" + driver + "'");

				// Minus the current driver a loads
				statement.executeUpdate(
						"UPDATE Driver SET Deliveries = Deliveries -1 WHERE Name = '" + currentDriver + "'");

				// Update Driver on Order
				statement
						.executeUpdate("UPDATE Orders SET Driver ='" + driver + "' WHERE OrderNumber = " + orderNumber);
				break;

			case "3":
				// UPDATE the Restaurant:
				System.out.print("\nPlease enter the correct Name of the Restaurant:");
				String restaurant = scan.nextLine();

				// Check if Restaurant name exist, if not create Restaurant
				ResultSet result = statement.executeQuery("SELECT * FROM Restaurant WHERE Name = '" + restaurant + "'");
				if (result.next() == false) {
					enterNewRestaurant(statement, restaurant, city);
				}

				// Update Restaurant
				statement.executeUpdate(
						"UPDATE Orders SET Restaurant = '" + restaurant + " WHERE OrderNumber = " + orderNumber);
				break;

			case "4":
				// Item 1 amendments
				System.out.print("Name of the First item that you want to order:");
				String itemOne = scan.nextLine();

				System.out.print("Quantity of the First item:");
				int quantityOne = scan.nextInt();
				scan.nextLine();

				System.out.print("Price of First item:");
				double priceOne = scan.nextDouble();
				scan.nextLine();

				// Update Item 1
				statement.executeUpdate("UPDATE Orders SET ItemOne = '" + itemOne + "', QuantityOne = " + quantityOne
						+ ", PriceOne =  " + priceOne + " WHERE OrderNumber = " + orderNumber);
				break;

			case "5":
				// Item 2
				System.out.print("Name of the Second item that you want to order:");
				String itemTwo = scan.nextLine();

				System.out.print("Quantity of the Second item:");
				int quantityTwo = scan.nextInt();
				scan.nextLine();

				System.out.print("Price of Second item:");
				double priceTwo = scan.nextDouble();
				scan.nextLine();

				// Update Item 2
				statement.executeUpdate("UPDATE Orders SET ItemTwo = '" + itemTwo + "', QuantityTwo = " + quantityTwo
						+ ", PriceTwo =  " + priceTwo + " WHERE OrderNumber = " + orderNumber);
				break;

			case "6":
				// Special Instructions
				System.out.print("New Special Instructions:");
				String specialInstructions = scan.nextLine();

				// Update Special Instructions
				statement.executeUpdate("UPDATE Orders SET SpecialInstructions = '" + specialInstructions
						+ "' WHERE OrderNumber = " + orderNumber);
				break;

			case "7":
				// Completion Date
				System.out.print("Please enter Completion Date (yyyymmdd):");
				int completionDate = scan.nextInt();
				scan.nextLine();

				// Update Completion Date
				statement.executeUpdate("UPDATE Orders SET CompletionDate = " + completionDate
						+ ", Finalised = True  WHERE OrderNumber = " + orderNumber);

				break;

			// Default if user entered a wrong character
			default:
				System.out.println("\nRestaurant was not ammended\n");
				break;

			}
		}

	}

	/** Method to Search for data */
	public static void searchData(Statement statement) throws SQLException {

		/** Ask user which item they want to search for */
		System.out.println(
				"\nSearching: \n1. Order by Order Number \n2. Order by Customer Name \n3. All Orders - Summary View \n4. All Orders - Detailed View"
						+ "\n5. All Not finalised Orders \n6. All Finished Orders  \n7. Display All Drivers "
						+ "\n8. Display All Customers \n9. Display All Restaurants\n0. Back to Main Screen");
		String itemUpdate = scan.nextLine();

		/**
		 * Switch statement according to answer given Search according to selected
		 * option
		 */
		switch (itemUpdate) {

		case "0":
			// Back to Main Screen
			break;

		case "1":
			// Search by Order Number
			System.out.print("Enter Order Number:");
			int orderNumber = scan.nextInt();
			scan.nextLine();
			ResultSet results = statement.executeQuery("SELECT * FROM Orders"
					+ " INNER JOIN Restaurant ON Orders.Restaurant = Restaurant.Name"
					+ " INNER JOIN Customer ON Orders.Customer = Customer.Name" + " WHERE OrderNumber =" + orderNumber);
			if (results.next() == true) {
				ResultSet showByOrderNumber = statement.executeQuery(
						"SELECT * FROM Orders" + " INNER JOIN Restaurant ON Orders.Restaurant = Restaurant.Name"
								+ " INNER JOIN Driver ON Orders.Driver = Driver.Name"
								+ " INNER JOIN Customer ON Orders.Customer = Customer.Name" + " WHERE OrderNumber ="
								+ orderNumber);
				displayFullOrderResults(showByOrderNumber);
			} else {
				results = statement.executeQuery("SELECT * FROM Orders WHERE OrderNumber =" + orderNumber);
				displayOrderResults(results);
			}

			break;

		case "2":
			// Search by Customer Name
			System.out.print("Enter Customer Name:");
			String customerName = scan.nextLine();
			results = statement.executeQuery("SELECT * FROM Orders WHERE Customer = '" + customerName + "'");
			displayOrderResults(results);
			break;

		case "3":
			// Confirm if user wants to see all the Orders Summary View
			System.out.println("\nAre you sure you want to show all the Orders? \n1. YES \n2. NO");
			int confirmSeeOrders = scan.nextInt();
			scan.nextLine();
			if (confirmSeeOrders == 1) {
				results = statement.executeQuery("SELECT * FROM Orders");
				displayOrderResults(results);
			}
			break;

		case "4":
			// Confirm if user wants to see all the Orders Detailed view
			System.out.println("\nAre you sure you want to show all the Orders? \n1. YES \n2. NO");
			confirmSeeOrders = scan.nextInt();
			scan.nextLine();
			if (confirmSeeOrders == 1) {
				results = statement.executeQuery(
						"SELECT * FROM Orders" + " INNER JOIN Restaurant ON Orders.Restaurant = Restaurant.Name"
								+ " INNER JOIN Driver ON Orders.Driver = Driver.Name"
								+ " INNER JOIN Customer ON Orders.Customer = Customer.Name");
				displayFullOrderResults(results);
			}
			break;

		case "5":
			// All Not finalized Orders
			results = statement.executeQuery("SELECT * FROM Orders WHERE Finalised = False");
			displayOrderResults(results);
			break;

		case "6":
			// All finalized Orders
			results = statement.executeQuery("SELECT * FROM Orders WHERE Finalised = True");
			displayOrderResults(results);
			break;

		case "7":
			// Display All drivers
			results = statement.executeQuery("SELECT * FROM Driver");
			displayDriverResults(results);
			break;

		case "8":
			// Display All Customers
			results = statement.executeQuery("SELECT * FROM Customer");
			displayCustomerResults(results);
			break;

		case "9":
			// Display All Restaurants
			results = statement.executeQuery("SELECT * FROM Restaurant");
			displayRestaurantResults(results);
			break;

		// Default if user entered a wrong character
		default:
			System.out.println("\nInvalid number input\n");
			break;
		}

	}

	/** Method to create Text for Invoice File */
	public static String invoiceTextCreate(ResultSet results) throws SQLException {
		results.next();

		// Compiling the output message with the invoice detail
		String outputMessage = ("Order number " + results.getInt("OrderNumber") + "\r\nCustomer: "
				+ results.getString("Customer") + "\r\nEmail: " + results.getString("Customer.Email")
				+ "\r\nPhone number: " + results.getString("Customer.ContactNumber") + "\r\nLocation: "
				+ results.getString("Customer.City") + "\r\n" + "\r\nYou have ordered the following from  "
				+ results.getString("Restaurant") + " in " + results.getString("Restaurant.City") + "\r\n" + "\r\n"
				+ results.getInt("QuantityOne") + " x " + results.getString("ItemOne") + "("
				+ results.getDouble("PriceOne") + ")" + "\r\n" + results.getInt("QuantityTwo") + " x "
				+ results.getString("ItemTwo") + "(" + results.getDouble("PriceTwo") + ")" + "\r\n"
				+ "\r\nSpecial instructions: " + results.getString("SpecialInstructions") + "\r\n" + "\r\nTotal: R"
				+ ((results.getInt("QuantityOne") * results.getDouble("PriceOne"))
						+ (results.getInt("QuantityTwo") + results.getDouble("PriceTwo")))
				+ "\r\n" + "\r\n" + results.getString("Driver")
				+ " is nearest to the restaurant and so he will be delivering your" + "\r\norder to you at:" + "\r\n "
				+ "\r\n" + results.getString("Customer.StreetAddress") + "\r\n" + results.getString("Customer.Suburb")
				+ "\r\n " + "\r\nIf you need to contact the restaurant, their number is "
				+ results.getString("Restaurant.ContactNumber"));

		// Return the invoice text
		return outputMessage;
	}

	/** Method to create a Text file for the invoice*/
	public static void fileCreatorMethod(int orderNumber, String outputMessage) {
		String writingFileLocation = "invoices/invoice";
		try {
			// Create a new Text file
			Formatter newFileWrite = new Formatter(writingFileLocation + orderNumber + ".txt");
			// Write the new String text inside
			newFileWrite.format("%s", outputMessage);
			// Close the file writer
			newFileWrite.close();
			System.out.println("New invoice file " + orderNumber + " have been created.");
		}
		// If there was a problem creating the file - display the below error message
		catch (FileNotFoundException e) {
			System.out.println("Error - Invoice File NOT created for invoice:" + orderNumber);
		}
	}

	/** Method to display the results of Customers in database*/
	public static void displayCustomerResults(ResultSet results) throws SQLException {

		// Display Fields
		System.out.println("Name, Contact Number, Street Address, Suburb, City, Email");
		// Display selected data rows
		while (results.next()) {
			System.out.println(results.getString("Name") + ", " + results.getString("ContactNumber") + ", "
					+ results.getString("StreetAddress") + ", " + results.getString("Suburb") + ", "
					+ results.getString("City") + ", " + results.getString("Email"));
		}
	}

	/**Method to display the results of Drivers in database*/
	public static void displayDriverResults(ResultSet results) throws SQLException {

		// Display Fields
		System.out.println("Name, City, Number of Deliveries");
		// Display selected data rows
		while (results.next()) {
			System.out.println(
					results.getString("Name") + ", " + results.getString("City") + ", " + results.getInt("Deliveries"));
		}
	}

	/**Method to display the results of Drivers in database*/
	public static void displayRestaurantResults(ResultSet results) throws SQLException {

		// Display Fields
		System.out.println("Name, City, Contact Number");
		// Display selected data rows
		while (results.next()) {
			System.out.println(results.getString("Name") + ", " + results.getString("City") + ", "
					+ results.getString("ContactNumber"));
		}
	}

	/** Method to display the results of Orders in database (Basic Form)*/
	public static void displayOrderResultsBasic(ResultSet results) throws SQLException {
		// Display selected data Fields
		while (results.next()) {
			System.out.println("Order Number:" + results.getInt("OrderNumber") + ", Customer:"
					+ results.getString("Customer") + ", Driver:" + results.getString("Driver") + ", Restaurant:"
					+ results.getString("Restaurant"));
		}
	}

	/** Method to display the results of Orders in database*/
	public static void displayOrderResults(ResultSet results) throws SQLException {

		// Display selected data rows
		while (results.next()) {
			// Display Fields
			System.out.println("\nOrder Number:" + results.getInt("OrderNumber") + ", Customer:"
					+ results.getString("Customer") + ", Driver:" + results.getString("Driver") + ", Restaurant:"
					+ results.getString("Restaurant") + ", Completion Date:" + results.getDate("CompletionDate"));
			// Display Fields
			System.out.println("Item One:" + results.getString("ItemOne") + " x " + results.getInt("QuantityOne")
					+ " @ R" + results.getDouble("PriceOne") + " each, Item Two:" + results.getString("ItemTwo") + " x "
					+ results.getInt("QuantityTwo") + " @ R" + results.getDouble("PriceTwo")
					+ " each, Special Instructions:" + results.getString("SpecialInstructions"));
		}
	}

	/**Method to display all the detail results of Orders in database*/
	public static void displayFullOrderResults(ResultSet results) throws SQLException {

		// Display selected data rows
		while (results.next()) {
			// Display Fields
			System.out.println("\nOrder Number:" + results.getInt("OrderNumber") + ", Completion Date:"
					+ results.getDate("CompletionDate") + ", Finalised:" + results.getBoolean("Finalised"));

			System.out.println("Customer:" + results.getString("Customer") + ", Address:"
					+ results.getString("Customer.StreetAddress") + ", " + results.getString("Customer.Suburb") + ", "
					+ results.getString("Customer.City") + ", Contact Number:"
					+ results.getString("Customer.ContactNumber") + ", Email:" + results.getString("Customer.Email"));

			System.out
					.println("Driver:" + results.getString("Driver") + ", Restaurant:" + results.getString("Restaurant")
							+ ", Restaurant Number:" + results.getString("Restaurant.ContactNumber"));

			System.out.println("Item One: " + results.getInt("QuantityOne") + " x " + results.getString("ItemOne")
					+ " @ R" + results.getDouble("PriceOne") + " each");
			// If no 2nd item, do not display
			if (results.getInt("QuantityTwo") != 0) {
				System.out.println("Item Two: " + results.getInt("QuantityTwo") + " x " + results.getString("ItemTwo")
						+ " @ R" + results.getDouble("PriceTwo") + " each");
			}
			System.out.println("Special Instructions: " + results.getString("SpecialInstructions"));

		}
	}

	/** Method to Upload Drivers from a Text file to Database */
	public static void uploadDriversDatabase(Statement statement) throws SQLException {

		// File path and create an array for Driver details
		String driverTextFile = "projectFiles/drivers.txt";

		// Use the scanner method to scan the customer text file line my line
		try {
			File file = new File(driverTextFile);
			Scanner lineScan = new Scanner(file);
			while (lineScan.hasNextLine()) {
				// Get the string value of the line
				String textLine = lineScan.nextLine();

				// Use the scanner function to scan the items in the line
				try (Scanner itemScanner = new Scanner(textLine)) {
					// Use the delimiter to know until where the scanner must scan
					itemScanner.useDelimiter(", ");
					while (itemScanner.hasNext()) {
						// Declare the String variables to store the attributes
						String driverName = itemScanner.next();
						String cityDriver = itemScanner.next();
						String deliveriesString = itemScanner.next();

						// Parse the string to an integer
						int deliveries = Integer.parseInt(deliveriesString);

						// Check if Name exist in the Driver database, if not, capture into database
						ResultSet nameExist = statement
								.executeQuery("SELECT * FROM Driver WHERE Name = '" + driverName + "'");
						if (nameExist.next() == false) {

							// Insert Data into Database
							statement.executeUpdate("INSERT INTO Driver VALUES('" + driverName + "','" + cityDriver
									+ "'," + deliveries + ")");

						} else {
							System.out.println("Driver name: " + driverName + " - already exists and was not added");
						}

					}
				}
			}
			// Close the scanner
			lineScan.close();
		}
		// If problem with executing scanner, give error message
		catch (FileNotFoundException e) {
			System.out.println("Error reading Driver Objects at location: " + driverTextFile);
		}
	}

}
