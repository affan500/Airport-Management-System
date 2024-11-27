package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import java.io.IOException;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class z extends Application {

  

    // LoginController for handling login and signup
    public static class LoginController {
        private TextField usernameField;
        private PasswordField passwordField;
        private Button loginButton;
        private Label loginerrorLabel;
        private Hyperlink signuplink;
        private Hyperlink loginlink;
        private AnchorPane signupform;
        private AnchorPane signuppanel;
        private Label signuperrorLabel;
        private TextField setun;
        private TextField setpass;
        private TextField fn;
        private TextField ln;
        private Button signupButton;
       
        
        
  
        private Stage stage;
        private Parent root;

        public LoginController(Parent root, Stage stage) {
            this.stage = stage;
            this.root = root;
            initializeLoginControls();
            setupLoginActions();
        }

        private void initializeLoginControls() {
            usernameField = (TextField) root.lookup("#username");
            passwordField = (PasswordField) root.lookup("#password");
            loginButton = (Button) root.lookup("#login");
            loginerrorLabel = (Label) root.lookup("#loginerror");
            signuplink = (Hyperlink) root.lookup("#signuphl");

            loginlink = (Hyperlink) root.lookup("#loginhl");
            signupform = (AnchorPane) root.lookup("#signupform");
            signuppanel = (AnchorPane) root.lookup("#signuppanel");
            setun = (TextField) root.lookup("#setun");
            setpass = (TextField) root.lookup("#setpass");
            fn = (TextField) root.lookup("#fn");
            ln = (TextField) root.lookup("#ln");
            signupButton = (Button) root.lookup("#signup");
            signuperrorLabel = (Label) root.lookup("#signuperror");

         
        }

        private void setupLoginActions() {
            loginButton.setOnAction(e -> handleLogin());
            signuplink.setOnAction(e -> showSignupForm());
            loginlink.setOnAction(e -> hideSignupForm());
            signupButton.setOnAction(e -> handleSignup());
        }

        private void handleLogin() {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (FlightManagementSystem.database.checkCredentials(username, password)) {
                // Check if user is regular (usertype == 1) or admin (usertype == 2)
                try (Connection conn = DriverManager.getConnection(FlightManagementSystem.database.url);
                     PreparedStatement stmt = conn.prepareStatement("SELECT usertype, userid FROM Users WHERE username = ?")) {

                    stmt.setString(1, username);
                    ResultSet rs = stmt.executeQuery();

                    // Ensure there is a result in the ResultSet before accessing values
                    if (rs.next()) {
                        int userType = rs.getInt("usertype");
                        int userId = rs.getInt("userid");
                        System.out.println(userType);
                        // Extract user info and read data for the flight system
                        FlightManagementSystem.database.UserInfoExtract(username);
                       

                        if (userType == 1) {
                         	loginButton.setStyle("-fx-background-color: grey; -fx-text-fill: white;");

                            System.out.println("User ID: " + userId);
                            FlightManagementSystem.database.readuserdata();
                            loadUserInterface();
                        } 
                        
                        else if (userType == 2) {
                         	loginButton.setStyle("-fx-background-color: grey; -fx-text-fill: white;");

                            System.out.println("User ID: " + userId); 
                            FlightManagementSystem.database.readAdminData();
                            loadAdminInterface();
                        }
                        
                        else if (userType == 3) {
                            System.out.println("suspended " + userId); // Optional: Display admin ID
                            loginerrorLabel.setText("Account Suspended.");

                        }
                        
                        else {
                            loginerrorLabel.setText("Access denied. Invalid user type.");
                        }
                    } 
                    
                    
                    else {
                        loginerrorLabel.setText("User not found.");
                    }
                }
                
                catch (SQLException e) {
                    e.printStackTrace();
                    loginerrorLabel.setText("Error during login.");
                }
            } else {
                loginerrorLabel.setText("Invalid Username/Password Combination");
            }
        }

        private void loadUserInterface() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("userinterface.fxml"));
                Parent userInterfaceRoot = loader.load();

                stage.setScene(new Scene(userInterfaceRoot, 900, 500));
                new PassengerInterfaceController(userInterfaceRoot, stage);  // Initialize user interface controller

            } catch (IOException e) {
                System.out.println("Failed to load user interface FXML: " + e.getMessage());
            }
        }
        
        private void loadAdminInterface() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("admininterface.fxml"));
                Parent adminInterfaceRoot = loader.load();

                stage.setScene(new Scene(adminInterfaceRoot, 900, 500));
                new AdminInterfaceController(adminInterfaceRoot, stage);  // Initialize user interface controller

            } catch (IOException e) {
                System.out.println("Failed to load admin interface FXML: " + e.getMessage());
            }
        }


        private void showSignupForm() {
            signupform.setVisible(true);
            signuppanel.setVisible(true);
        }

        private void hideSignupForm() {
            signupform.setVisible(false);
        }

        private void handleSignup() {
            String username = setun.getText();
            String password = setpass.getText();
            String firstname = fn.getText();
            String lastname = ln.getText();

            // Check for missing fields
            if (username.isEmpty() || password.isEmpty() || firstname.isEmpty() || lastname.isEmpty()) {
                signuperrorLabel.setText("One or more Fields missing");
                return;
            }

            // Check if username is already taken
            String checkUserQuery = "SELECT userid FROM Users WHERE username = ?";
            try (Connection conn = DriverManager.getConnection(FlightManagementSystem.database.url);
                 PreparedStatement checkStmt = conn.prepareStatement(checkUserQuery)) {

                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    signuperrorLabel.setText("Username is already taken");
                } else {
                    // Insert new user if username is available
                    String insertUserQuery = "INSERT INTO Users (userid, username, password, usertype) " +
                                             "VALUES ((SELECT COALESCE(MAX(userid) + 1, 1) FROM Users), ?, ?, 1)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertUserQuery)) {
                        insertStmt.setString(1, username);
                        insertStmt.setString(2, password);
                        insertStmt.executeUpdate();

                        signuperrorLabel.setText(""); 
                        setun.clear();
                        setpass.clear();
                        fn.clear();
                        ln.clear();
                        // Clear error label
                        signuppanel.setVisible(false); // Hide the signup panel
                        System.out.println("Signup successful");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    // UserInterfaceController for handling user actions in userinterface.fxml
    public static class PassengerInterfaceController {
    	//main buttons
        private Button logoutbutton;
        private Button confirmbutton;
        private Button cancelbutton;
        private Button searchflightbutton;
        private Button myflightbutton;
        private Button viewpassbutton;
        
        private ChoiceBox seattypes;
        private Stage stage;
        
        //search flight panel
        private Pane searchflightspanel;
        private TableView allflighttable;
        private TextField flightid;
        private Button check;
        private Label seatprice;
        private Label booksuccess;
        private Label bookerror;
        private Button bookbutton;
        
        // my flights panel
        private Pane myflightspanel;
        private TableView myflighttable;
        private TextField flightid2;
        private Button set;
        private Button generatepassbutton;
        private Button cancelflightbutton;
        private Button confirmcancelbutton;
        private Button cancelcancelbutton;
        private Label myflighterror;
        private Label passlabel;
        
        
        //boardingpasses
        private Pane boardingpasspanel;
        private TableView boardingpasstable;
        private TextField passid;
        private Button view;
        private Label viewlabel;
        
        //view pass
        private Pane viewpass;
        private Button exit;
        private Label first;
        private Label economy;
        private Label business;
        private Label name;
        private Label date;
        private Label time;
        private Label fID;
        private Label to;
        private Label from;
        private Label amenities;
        private Label amenitiesprompt;
        private Label meal;
        
        
        
        
        
        
       
        
        
        float flightprice;
        int flightID;
        int passID;
        String seattype;

        public PassengerInterfaceController(Parent root, Stage stage) {
            this.stage = stage;
            logoutbutton = (Button) root.lookup("#logout1");
            confirmbutton = (Button) root.lookup("#confirm");
            cancelbutton = (Button) root.lookup("#cancel");
            searchflightbutton  = (Button) root.lookup("#searchflights");
            myflightbutton = (Button) root.lookup("#myflights");
            viewpassbutton = (Button) root.lookup("#viewpass");
            
    
            
            //panes
            
            //searchflight
            searchflightspanel = (Pane) root.lookup("#searchflightspanel");
            searchflightspanel.setVisible(false);
            
            seattypes = (ChoiceBox) root.lookup("#seattype");
            seattypes.getItems().addAll("First Class", "Business Class", "Economy Class");
            flightid = (TextField) root.lookup("#flightid");
            check = (Button) root.lookup("#check");
            seatprice = (Label) root.lookup("#seatprice");
            booksuccess = (Label) root.lookup("#booksuccess");
            bookerror = (Label) root.lookup("#bookerror");
            bookbutton = (Button) root.lookup("#bookbutton");
            bookbutton.setVisible(false);
            
            
            // myflights
            myflightspanel = (Pane) root.lookup("#myflightspanel");
            myflighttable = (TableView) root.lookup("#myflighttable");
            flightid2 = (TextField) root.lookup("#flightid2");
            set = (Button) root.lookup("#set");
            generatepassbutton = (Button) root.lookup("#generatepass");
            cancelflightbutton = (Button) root.lookup("#cancelflight");
            confirmcancelbutton = (Button) root.lookup("#confirmcancel");
            cancelcancelbutton = (Button) root.lookup("#cancelcancel");
            myflighterror = (Label) root.lookup("#myflighterror");
            passlabel = (Label) root.lookup("#passlabel");
            
            
            
            //boardingpasses
            boardingpasspanel = (Pane) root.lookup("#boardingpasspanel");
            boardingpasstable = (TableView) root.lookup("#boardingpasstable");
            passid = (TextField) root.lookup("#passid");
            view = (Button) root.lookup("#view");
            viewlabel = (Label) root.lookup("#viewlabel");
            
            
           //view pass 
            viewpass = (Pane) root.lookup("#viewpass2");
            exit = (Button) root.lookup("#back");
            first = (Label) root.lookup("#first");
            economy = (Label) root.lookup("#economy");
            business = (Label) root.lookup("#business");
            name = (Label) root.lookup("#name");
            date = (Label) root.lookup("#date");
            time = (Label) root.lookup("#time");
            fID = (Label) root.lookup("#fID");
            to = (Label) root.lookup("#to");
            from = (Label) root.lookup("#from");
            amenitiesprompt = (Label) root.lookup("#amenitiesprompt");
            amenities = (Label) root.lookup("#amenities");
            meal = (Label) root.lookup("#meal");

            
            
            clearLabels();
            //tables
            allflighttable = (TableView) root.lookup("#allflighttable");
            populateFlightTable(0, 0);
            
            setupUserInterfaceActions();
        }

        
        
        private void setupUserInterfaceActions() {
            logoutbutton.setOnAction(e -> handleLogout());
            cancelbutton.setOnAction(e -> cancelLogout());
            confirmbutton.setOnAction(e -> confirmLogout());
            searchflightbutton.setOnAction(e -> enableflightsearchpanel());
            myflightbutton.setOnAction(e -> enablemyflightspanel());
            check.setOnAction(e -> verifyflight());
            bookbutton.setOnAction(e -> bookflight());
            set.setOnAction(e -> verifymyflight());
            cancelflightbutton.setOnAction(e -> showcancelconfirmation());
            cancelcancelbutton.setOnAction(e -> hidecancelconfirmation());
            confirmcancelbutton.setOnAction(e -> cancelBooking());
            generatepassbutton.setOnAction(e -> createPass());
            viewpassbutton.setOnAction(e->enableboardingpassespanel());
            view.setOnAction(e->verifyPass());
            exit.setOnAction(e->closepass());
        }
        
        
        private void displaypass() {
            viewpass.setVisible(true);
            int id = -1; // Initialize fid to avoid uninitialized variable error

            // Iterate through the list of boarding passes and match the passID
            for (BoardingPass pass : FlightManagementSystem.boardingpasses) { 
                if (pass.getPassID() == passID) { // Check if the passID matches
                    id = pass.getFlightID();

                    // Based on the pass type, set visibility of labels
                    if (pass instanceof EconomyPass)
                    {
                        EconomyPass economyPass = (EconomyPass) pass; // Cast to access EconomyPass-specific fields
                        economy.setVisible(true);
                        business.setVisible(false);
                        first.setVisible(false);
                        amenitiesprompt.setVisible(false);
                        meal.setText(economyPass.mealtype);
                        amenities.setText("");
                    }
                    
                    else if (pass instanceof BusinessPass)
                    {
                        BusinessPass businessPass = (BusinessPass) pass; // Cast to access BusinessPass-specific fields
                        economy.setVisible(false);
                        business.setVisible(true);
                        first.setVisible(false);
                        amenitiesprompt.setVisible(true); // Assuming amenitiesprompt is a Label for amenities info
                        meal.setText(businessPass.mealtype);
                        amenities.setText(businessPass.amenities);
                    }
                    
                    else if (pass instanceof FirstclassPass)
                    {
                        FirstclassPass firstclassPass = (FirstclassPass) pass; // Cast to access FirstclassPass-specific fields
                        economy.setVisible(false);
                        business.setVisible(false);
                        first.setVisible(true);
                        amenitiesprompt.setVisible(true);
                        meal.setText(firstclassPass.mealtype);
                        amenities.setText(firstclassPass.amenities);
                    }
                    break; // Once the pass is found, no need to continue the loop
                }
            }
    System.out.println(id);
            // If a valid flight ID was found, find the corresponding flight and update the labels
            if (id != -1)
            {
                for (Flight flight : FlightManagementSystem.flights)
                { 
                    if (flight.getFlightID() == id) { // Check if the flightID matches
                        date.setText(flight.getDate()); // Assuming Flight class has a getDate() method
                        time.setText(flight.getTime()); // Assuming Flight class has a getTime() method
                        fID.setText(String.valueOf(flight.getFlightID())); // Convert flight ID to String
                        to.setText(flight.getDestinationLocation()); // Assuming Flight class has a getDestinationLocation() method
                        from.setText(flight.getSourceLocation()); // Assuming Flight class has a getSourceLocation() method
                        break; // Exit the loop once the flight is found
                    }
                }
            }
            
            name.setText(FlightManagementSystem.user.username);
            
        }

        
        private void closepass()
        {
        	viewpass.setVisible(false);
        }
        
        
        private void createPass()
        {
           int id = flightID;
           boolean status = FlightManagementSystem.database.createPass(flightID);   
           
           
           if (status == true )
           {
        	   passlabel.setText("Pass Created.");
           }
           
           else 
           {
        	   passlabel.setText("Boarding pass already generated.");
           }
           
           
        }
        
        
        
         
        private void showcancelconfirmation()
        {
        	cancelcancelbutton.setVisible(true);
        	confirmcancelbutton.setVisible(true);
        }
        
        private void hidecancelconfirmation()
        {
        	cancelcancelbutton.setVisible(false);
        	confirmcancelbutton.setVisible(false);
        }
       
        
        //cancellation of a bookings
        private void cancelBooking()
        {
        	
        	hidecancelconfirmation();	
        	int fid = flightID;
        	boolean status = FlightManagementSystem.database.deleteBooking(flightID);
        	
        	if(status == true )
        	{
        		 
        		 passlabel.setText("Booking deleted.");
        		 // also removing from array list
        		 
        		 populateMyFlightTable(); 
        		 populateFlightTable(0, 0);//updating the table
        		 
        		 }
        		
        		
        }
        
        
        
        private void bookflight()
        {
        	boolean status = FlightManagementSystem.database.createBooking(flightID, seattype);
        	if (status == false)
        		booksuccess.setText("Already booked");
        	
        	else
        	{ 
        		
        		booksuccess.setText("Flight Booked!");
        		bookbutton.setVisible(false);
        		
        		  
        		populateFlightTable(1, flightID);
        		
        	}
        		
        	
        }
        
        
        // response to the check button in search flights
        private void verifyflight() {
            // Get the entered Flight ID
            String enteredFlightID = flightid.getText().trim();
            seattype = (String) seattypes.getValue(); // Retrieve selected value from ChoiceBox

            // Check if either field is empty
            if (enteredFlightID.isEmpty() || seattype == null || seattype.isEmpty()) {
                bookerror.setText("Field is Blank");
                bookbutton.setVisible(false);
                seatprice.setText("");
                booksuccess.setText("");
                populateFlightTable(0, 0);
                return;
            }

            try {
                // Parse the Flight ID as an integer
                 flightID = Integer.parseInt(enteredFlightID);

                // Search for the flight in the flights list
                Flight matchingFlight = FlightManagementSystem.flights.stream()
                        .filter(flight -> flight.getFlightID() == flightID)
                        .findFirst()
                        .orElse(null);
                 
                if (matchingFlight != null) {
                    // Flight found, clear error labels
                    clearLabels();
                    flightprice = matchingFlight.getPrice();

                    // Adjust price based on seat type
                    if (seattype.equals("First Class")) {
                        flightprice += 12000;
                    } else if (seattype.equals("Business Class")) {
                        flightprice += 6000;
                    }
                    bookbutton.setVisible(true);
                    populateFlightTable(1, flightID);
                    // Update the seatprice label with the price
                    seatprice.setText("PKR " + flightprice);
                } else {
                    // No flight found with the given ID
                    bookerror.setText("Incorrect ID");
                    bookbutton.setVisible(false);
                    seatprice.setText("");
                    booksuccess.setText("");
                    populateFlightTable(0, 0);
                   
                }

            } catch (NumberFormatException e) {
                // Handle invalid Flight_ID format
                bookerror.setText("Invalid Flight ID");
                seatprice.setText("");
                booksuccess.setText("");
                
                bookbutton.setVisible(false);
                populateFlightTable(0, 0);
            }
        }

 
        private void verifymyflight() {
        	passlabel.setText("");
            // Get the entered Flight ID from the text field
            String enteredFlightID = flightid2.getText().trim();

            // Check if the field is empty
            if (enteredFlightID.isEmpty()) {
                // If the field is empty, set the error message and hide the buttons
                myflighterror.setText("Field not entered");
                generatepassbutton.setVisible(false);
                cancelflightbutton.setVisible(false);
                passlabel.setText("");
                return;
            }

            try {
                // Parse the Flight ID entered by the user
                int enteredID = Integer.parseInt(enteredFlightID);

                // Check if the entered Flight ID exists in the bookings
                boolean bookingExists = FlightManagementSystem.bookings.stream()
                        .anyMatch(booking -> booking.getFlightID() == enteredID && booking.getPassengerID() == FlightManagementSystem.user.ID);

                if (bookingExists) {
                    // If a booking with the given Flight ID exists, show the buttons and clear the error label
                    myflighterror.setText("");  // Clear error message
                    generatepassbutton.setVisible(true);
                    cancelflightbutton.setVisible(true);
                    flightID = Integer.parseInt(enteredFlightID);
                } else {
                    // If no booking with that Flight ID exists, show the error message and hide the buttons
                    myflighterror.setText("Incorrect ID");
                    generatepassbutton.setVisible(false);
                    cancelflightbutton.setVisible(false);
                    passlabel.setText("");
                }
            } catch (NumberFormatException e) {
                // Handle invalid Flight ID format
                myflighterror.setText("Invalid Flight ID");
                generatepassbutton.setVisible(false);
                cancelflightbutton.setVisible(false);
            }
        }

        
        
        private void verifyPass() {
            viewlabel.setText("");  // Clear previous text on the label

            // Get the entered Pass ID from the text field
            String enteredPassID = passid.getText().trim();

            // Check if the field is empty
            if (enteredPassID.isEmpty()) {
                // If the field is empty, set the error message
                viewlabel.setText("Field is empty");
                return;  // Exit the function early if the field is empty
            }

            try {
                // Parse the Pass ID entered by the user
                int enteredID = Integer.parseInt(enteredPassID);

                // Check if the entered Pass ID exists in the boarding passes list
                boolean passExists = FlightManagementSystem.boardingpasses.stream()
                        .anyMatch(pass -> pass.getPassID() == enteredID);

                if (passExists) {
                	passID = enteredID;
                    // If the Pass ID exists, show the pass details (you can add this part as needed)
                	displaypass();
                 } 
                
                else {
                    // If no matching pass exists, show the error message
                    viewlabel.setText("Incorrect ID");
                }
            } catch (NumberFormatException e) {
                // Handle invalid Pass ID format (non-integer input)
                viewlabel.setText("Invalid Pass ID");
            }
        }

        
        // Helper method to clear labels
        private void clearLabels() {
            seatprice.setText("");
            booksuccess.setText("");
            bookerror.setText("");
            passlabel.setText("");
            name.setText("");
            date.setText("");
            time.setText("");
            to.setText("");
            from.setText("");
            economy.setVisible(false);
            first.setVisible(false);
            business.setVisible(false);
        }
        
        
        ////////////////////////////panel disable//////////////////
        private void disableallpanels()
        {
        	searchflightspanel.setVisible(false);
        	myflightspanel.setVisible(false);
        	boardingpasspanel.setVisible(false);
        	viewpass.setVisible(false);
        	
        	myflightbutton.setStyle("-fx-background-color: #008080; -fx-text-fill: white;");
        	searchflightbutton.setStyle("-fx-background-color: #008080; -fx-text-fill: white;");
        	logoutbutton.setStyle("-fx-background-color: #008080; -fx-text-fill: white;");
        	viewpassbutton.setStyle("-fx-background-color: #008080; -fx-text-fill: white;");
             clearLabels();
        }
        
        
        private void enableboardingpassespanel()
        {
        	disableallpanels();
        	populateBoardingPassTable();
        	viewpassbutton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                  //insert more teals
        	boardingpasspanel.setVisible(true);
        }
        
        private void enableflightsearchpanel()
        { 
        	
          
        	disableallpanels();
       
        	searchflightbutton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                  //insert more teals
        	searchflightspanel.setVisible(true);
        	
        	
        	
        	
        	
        }
        
        
        private void enablemyflightspanel()
        { 
        	populateMyFlightTable();
        	disableallpanels();
        	
        	myflightbutton.setStyle("-fx-background-color: red; -fx-text-fill: white;");

                    //insert more teals
        	myflightspanel.setVisible(true);
        	
        	
        	
        }
        
        
        
        //fills the tableview for searchallflights
        //type 0 for all flights, type 
        private void populateFlightTable(int type, int id) { 
            // Clear any existing columns to prevent duplicates
            allflighttable.getColumns().clear();

            // Define columns for the flight table
            TableColumn<Flight, Integer> flightIDColumn = new TableColumn<>("Flight ID");
            flightIDColumn.setCellValueFactory(new PropertyValueFactory<>("flightID"));

            TableColumn<Flight, String> dateColumn = new TableColumn<>("Date");
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

            TableColumn<Flight, String> timeColumn = new TableColumn<>("Time");
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

            TableColumn<Flight, String> sourceColumn = new TableColumn<>("Source");
            sourceColumn.setCellValueFactory(new PropertyValueFactory<>("sourceLocation"));

            TableColumn<Flight, String> destinationColumn = new TableColumn<>("Destination");
            destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destinationLocation"));

            TableColumn<Flight, String> airlineColumn = new TableColumn<>("Airline");
            airlineColumn.setCellValueFactory(new PropertyValueFactory<>("airlineName"));

            TableColumn<Flight, Integer> seatsColumn = new TableColumn<>("seats");
            seatsColumn.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));

            TableColumn<Flight, Float> priceColumn = new TableColumn<>("Min Price");
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

            // Add columns to the table view
            allflighttable.getColumns().addAll(flightIDColumn, dateColumn, timeColumn, sourceColumn, destinationColumn, airlineColumn, seatsColumn, priceColumn);

            // Create the filtered flight list
            ObservableList<Flight> flightList;
            if (type == 0) {
                // Display all flights
                flightList = FXCollections.observableArrayList(FlightManagementSystem.flights);
            } else if (type == 1) {
                // Display only the flight with the specified id
                flightList = FXCollections.observableArrayList(
                    FlightManagementSystem.flights.stream()
                        .filter(flight -> flight.getFlightID() == id)
                        .toList()
                );
            } else {
                // Invalid type, use an empty list
                flightList = FXCollections.observableArrayList();
            }

            // Set the items of the table view
            allflighttable.setItems(flightList);
        }

        
     // Method to populate the 'myflighttable' with flights from the bookings list
        private void populateMyFlightTable() {
            // Clear any existing columns to prevent duplicates
            myflighttable.getColumns().clear();

            // Define columns for the myflighttable
            TableColumn<Flight, Integer> flightIDColumn = new TableColumn<>("Flight ID");
            flightIDColumn.setCellValueFactory(new PropertyValueFactory<>("flightID"));

            TableColumn<Flight, String> dateColumn = new TableColumn<>("Date");
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

            TableColumn<Flight, String> timeColumn = new TableColumn<>("Time");
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

            TableColumn<Flight, String> sourceColumn = new TableColumn<>("Source");
            sourceColumn.setCellValueFactory(new PropertyValueFactory<>("sourceLocation"));

            TableColumn<Flight, String> destinationColumn = new TableColumn<>("Destination");
            destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destinationLocation"));

            TableColumn<Flight, String> airlineColumn = new TableColumn<>("Airline");
            airlineColumn.setCellValueFactory(new PropertyValueFactory<>("airlineName"));

            TableColumn<Flight, Integer> seatsColumn = new TableColumn<>("Seats");
            seatsColumn.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));

            TableColumn<Flight, Float> priceColumn = new TableColumn<>("Min Price");
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

            // Add columns to the table view
            myflighttable.getColumns().addAll(flightIDColumn, dateColumn, timeColumn, sourceColumn, destinationColumn, airlineColumn, seatsColumn, priceColumn);

            // Create the filtered flight list based on user's bookings
            ObservableList<Flight> myFlightList = FXCollections.observableArrayList();

            // Filter bookings for the current user (passenger ID matches)
            for (Booking booking : FlightManagementSystem.bookings) {
                if (booking.getPassengerID() == FlightManagementSystem.user.ID) {
                    // For each booking, find the corresponding flight
                    Flight matchingFlight = FlightManagementSystem.flights.stream()
                        .filter(flight -> flight.getFlightID() == booking.getFlightID())
                        .findFirst()
                        .orElse(null);

                    if (matchingFlight != null) {
                        // Add matching flight to the list
                        myFlightList.add(matchingFlight);
                    }
                }
            }

            // Set the items of the myflighttable to the filtered list of flights
            myflighttable.setItems(myFlightList);
        }

        
        
      

        private void populateBoardingPassTable() {
            // Clear any existing columns to prevent duplicates
            boardingpasstable.getColumns().clear();

            // Define columns for the boarding pass table
            TableColumn<BoardingPass, Integer> passIDColumn = new TableColumn<>("Boarding Pass ID");
            passIDColumn.setCellValueFactory(new PropertyValueFactory<>("passID"));

            TableColumn<BoardingPass, Integer> flightIDColumn = new TableColumn<>("Flight ID");
            flightIDColumn.setCellValueFactory(new PropertyValueFactory<>("flightID"));

            // Source column - Get flight source by matching flightID
            TableColumn<BoardingPass, String> sourceColumn = new TableColumn<>("Source");
            sourceColumn.setCellValueFactory(cellData -> {
                BoardingPass pass = cellData.getValue();
                Flight matchingFlight = null;
                // Loop through the flights to find the matching one
                for (Flight flight : FlightManagementSystem.flights) {
                    if (flight.getFlightID() == pass.getFlightID()) {
                        matchingFlight = flight;
                        break;
                    }
                }
                return matchingFlight != null ? new SimpleStringProperty(matchingFlight.getSourceLocation()) : new SimpleStringProperty("");
            });

            // Destination column - Get flight destination by matching flightID
            TableColumn<BoardingPass, String> destinationColumn = new TableColumn<>("Destination");
            destinationColumn.setCellValueFactory(cellData -> {
                BoardingPass pass = cellData.getValue();
                Flight matchingFlight = null;
                // Loop through the flights to find the matching one
                for (Flight flight : FlightManagementSystem.flights) {
                    if (flight.getFlightID() == pass.getFlightID()) {
                        matchingFlight = flight;
                        break;
                    }
                }
                return matchingFlight != null ? new SimpleStringProperty(matchingFlight.getDestinationLocation()) : new SimpleStringProperty("");
            });

            // Date column - Get flight date by matching flightID
            TableColumn<BoardingPass, String> dateColumn = new TableColumn<>("Date");
            dateColumn.setCellValueFactory(cellData -> {
                BoardingPass pass = cellData.getValue();
                Flight matchingFlight = null;
                // Loop through the flights to find the matching one
                for (Flight flight : FlightManagementSystem.flights) {
                    if (flight.getFlightID() == pass.getFlightID()) {
                        matchingFlight = flight;
                        break;
                    }
                }
                return matchingFlight != null ? new SimpleStringProperty(matchingFlight.getDate()) : new SimpleStringProperty("");
            });

            // Seat Type column - Get seat type from Booking (matching passengerID)
            TableColumn<BoardingPass, String> seatTypeColumn = new TableColumn<>("Seat Type");
            seatTypeColumn.setCellValueFactory(cellData -> {
                BoardingPass pass = cellData.getValue();
                Booking matchingBooking = null;
                // Loop through the bookings to find the matching one
                for (Booking booking : FlightManagementSystem.bookings) {
                    if (booking.getFlightID() == pass.getFlightID() && booking.getPassengerID() == pass.getPassengerID()) {
                        matchingBooking = booking;
                        break;
                    }
                }
                return matchingBooking != null ? new SimpleStringProperty(matchingBooking.seattype) : new SimpleStringProperty("");
            });

            // Add columns to the table view
            boardingpasstable.getColumns().addAll(passIDColumn, flightIDColumn, sourceColumn, destinationColumn, dateColumn, seatTypeColumn);

            // Create a list for the boarding passes
            ObservableList<BoardingPass> boardingPassList = FXCollections.observableArrayList();

            // Iterate over boarding passes and add valid ones to the list
            for (BoardingPass pass : FlightManagementSystem.boardingpasses) {
                if (pass.getPassengerID() == FlightManagementSystem.user.ID) {
                    boardingPassList.add(pass); // Add the boarding pass if it matches the user
                }
            }

            // Set the items of the boarding pass table
            boardingpasstable.setItems(boardingPassList);
        }



  
        

        private void handleLogout() {
        	//red
        	logoutbutton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        	searchflightbutton.setStyle("-fx-background-color: #008080; -fx-text-fill: white;");
           
            confirmbutton.setVisible(true);
            cancelbutton.setVisible(true);
        }

        private void cancelLogout() {
        	//teal
        	logoutbutton.setStyle("-fx-background-color: #008080; -fx-text-fill: white;");
            confirmbutton.setVisible(false);
            cancelbutton.setVisible(false);
        }

        private void confirmLogout() {
          //teal         	logoutbutton.setStyle("-fx-background-color: #008080; -fx-text-fill: white;");

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("loginsignup.fxml"));
                Parent loginRoot = loader.load();
                stage.setScene(new Scene(loginRoot, 700, 500));
                new LoginController(loginRoot, stage);  // Initialize LoginController for login view
            } catch (IOException e) {
                System.out.println("Failed to load loginsignup FXML: " + e.getMessage());
            }
        }
    }

    
    
    
    
    
    ///////////////////////////////////Admin Interface///////////////////////
    
    public static class AdminInterfaceController {
    	//main buttons
        private Button logoutbutton;
        private Button confirmbutton;
        private Button cancelbutton; 
        private Button viewairlines; 
        private Button viewusers;
        private Button revenue;
        private Button viewflights2;
       
        
        int aID;
        int uID;
        int flightID;
        
        //airlines
        private Pane viewairlinespanel;
        private Pane addairlinepanel;
        private Pane removeairlinepanel;
       private TableView airlinetable;
       private Button addairline;
       private Button removeairline;
       private Button register;
       private TextField airlinename;
       private TextField airlineID;
       private Button set2;
       private Button remove;
       private Button cancelairlineremove;
       private Button confirmairlineremove;
       private Label warninglabel;
       private Label airlineiderror;
       private Label addairlineprompt;
       private Label addairlineerror;
       
       
       
       
       //users 
       private Pane viewuserspanel;
       private Pane removeuserpanel;
       private Pane suspensionpanel;
       private TableView usertable;
       private TextField userid;
       private Button set3;
       private Button suspension;
       private Button removeuser;
       private Button suspend;
       private Button unsuspend;
       private Button cancel3;
       private Button confirm3;
       private Label suspensionprompt;
       private Label useriderror;
       private Label userdeleteprompt;
       
       
       //revenue
       private Pane revenuereportpanel;
       private ChoiceBox airlinebox1;
       private Button set4;
       private Label airlinenamelabel;
       private Label totalbookings;
       private Label activeflights;
       private Label revenuegenerated;
       
       
       
       //reschedule flights\
       private Pane rescheduleflightspanel;
       private Pane subpanel;
       private ChoiceBox airlinebox2;
       private TableView rescheduletable;
       private Button set5;
       private TextField rescheduleid;
       private DatePicker date;
       private Spinner hrspinner;
       private Spinner minspinner;
       private Button reschedule;
       private Button cancel4;
       private Button confirm4;
       private Label rescheduleprompt;
       private Label rescheduleerrorlabel;
       
       //add flights
       private Button addflight;
       private Button add;
       private Button cancel5;
       private Button confirm5;
       private Label fromtolabel;
       private ChoiceBox from;
       private ChoiceBox to;
       private TextField minprice;
       private TextField seats;


       
       
       
       
       
       
       private Stage stage;
    public AdminInterfaceController(Parent root, Stage stage) {
        this.stage = stage;
        logoutbutton = (Button) root.lookup("#logout2");
        confirmbutton = (Button) root.lookup("#confirm2");
        cancelbutton = (Button) root.lookup("#cancel2");
        viewairlines = (Button) root.lookup("#viewairlines");
        viewusers = (Button) root.lookup("#viewusers");
        revenue = (Button) root.lookup("#revenue");
        viewflights2 = (Button) root.lookup("#viewflights2");
        
        //airlines
        viewairlinespanel = (Pane) root.lookup("#viewairlinespanel");
        addairlinepanel = (Pane) root.lookup("#addairlinepanel");
        removeairlinepanel = (Pane) root.lookup("#removeairlinepanel");
        airlinetable = (TableView) root.lookup("#airlinetable");
        addairline = (Button) root.lookup("#addairline");
        removeairline = (Button) root.lookup("#removeairline");
        register = (Button) root.lookup("#register");
        airlinename = (TextField) root.lookup("#airlinename");
        addairlineprompt = (Label) root.lookup("#addairlineprompt");
        airlineID = (TextField) root.lookup("#airlineID");
        set2 = (Button) root.lookup("#set2");
        remove = (Button) root.lookup("#remove");
        cancelairlineremove = (Button) root.lookup("#cancelairlineremove");
        confirmairlineremove = (Button) root.lookup("#confirmairlineremove");
        warninglabel = (Label) root.lookup("#warninglabel");
        airlineiderror = (Label) root.lookup("#airlineiderror");
        addairlineerror = (Label) root.lookup("#addairlineerror");
        
        
        //users
        viewuserspanel = (Pane) root.lookup("#viewuserspanel");
        removeuserpanel = (Pane) root.lookup("#removeuserpanel");
        suspensionpanel = (Pane) root.lookup("#suspensionpanel");
        usertable = (TableView<?>) root.lookup("#usertable"); // Cast to TableView with generic type if required
        userid = (TextField) root.lookup("#userid");
        set3 = (Button) root.lookup("#set3");
        suspension = (Button) root.lookup("#suspension");
        removeuser = (Button) root.lookup("#removeuser");
        suspend = (Button) root.lookup("#suspend");
        unsuspend = (Button) root.lookup("#unsuspend");
        cancel3 = (Button) root.lookup("#cancel3");
        confirm3 = (Button) root.lookup("#confirm3");
        suspensionprompt = (Label) root.lookup("#suspensionprompt");
        useriderror = (Label) root.lookup("#useriderror");
        userdeleteprompt = (Label) root.lookup("#userdeleteprompt");
        
        
        // revenue
     // Initialize buttons
        revenuereportpanel = (Pane) root.lookup("#revenuereportpanel");
        logoutbutton = (Button) root.lookup("#logout2");
        confirmbutton = (Button) root.lookup("#confirm2");
        revenuereportpanel = (Pane) root.lookup("#revenuereportpanel");
        airlinebox1 = (ChoiceBox) root.lookup("#airlinebox1");
        airlinenamelabel = (Label) root.lookup("#airlinenamelabel");
        totalbookings = (Label) root.lookup("#totalbookings");
        activeflights = (Label) root.lookup("#activeflights");
        revenuegenerated = (Label) root.lookup("#revenuegenerated");
        
        
        
        
        // flights
        rescheduleflightspanel = (Pane) root.lookup("#rescheduleflightspanel");
        subpanel = (Pane) root.lookup("#subpanel");
        airlinebox2 = (ChoiceBox) root.lookup("#airlinebox2");
        rescheduletable = (TableView<?>) root.lookup("#rescheduletable"); // Use <?> for generics if required
        set5 = (Button) root.lookup("#set5");
        rescheduleid = (TextField) root.lookup("#rescheduleid");
        date = (DatePicker) root.lookup("#date");
        hrspinner = (Spinner<Integer>) root.lookup("#hrspinner");
        minspinner = (Spinner<Integer>) root.lookup("#minspinner");
        reschedule = (Button) root.lookup("#reschedule");
        cancel4 = (Button) root.lookup("#cancel4");
        confirm4 = (Button) root.lookup("#confirm4");
        rescheduleprompt = (Label) root.lookup("#rescheduleprompt");
        rescheduleerrorlabel = (Label) root.lookup("#rescheduleerrorlabel");

        // Optionally, initialize additional configurations for spinners
        hrspinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12)); // Hours: 0-23
        minspinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0)); // Minutes: 0-59
        
        
        //add flights 
        cancel5 = (Button) root.lookup("#cancel5");
        confirm5 = (Button) root.lookup("#confirm5");

        // Initialize label
        fromtolabel = (Label) root.lookup("#fromtolabel");
        // Initialize ChoiceBoxes
        from = (ChoiceBox<String>) root.lookup("#from");
        to = (ChoiceBox<String>) root.lookup("#to");

        if (from != null) {
            from.getItems().addAll("Karachi", "Islamabad", "Lahore");
        }
        
        if (to != null) {
            to.getItems().addAll("Karachi", "Islamabad", "Lahore");
        }
        // Initialize TextFields
        minprice = (TextField) root.lookup("#minprice");
        seats = (TextField) root.lookup("#seats");
        addflight = (Button) root.lookup("#addf");
        add = (Button) root.lookup("#add");
        
        setupUserInterfaceActions();
    }

    
    
    

    private void setupUserInterfaceActions() {
        logoutbutton.setOnAction(e -> handleLogout());
        cancelbutton.setOnAction(e -> cancelLogout());
        confirmbutton.setOnAction(e -> confirmLogout());
        viewairlines.setOnAction(e-> enableairlinespanel());
        addairline.setOnAction(e-> enableaddairlinepanel());
        register.setOnAction(e-> registerairline());
        removeairline.setOnAction(e-> enableremoveairlinepanel());
        set2.setOnAction(e-> verifyairline());
        remove.setOnAction(e->handleRemoveAirline());
        cancelairlineremove.setOnAction(e->hideremovebuttons());
        confirmairlineremove.setOnAction(e->removeAirline());
        viewusers.setOnAction(e->enableuserspanel());
        set3.setOnAction(e->verifyUser());
        suspension.setOnAction(e->handlesuspension());
        removeuser.setOnAction(e->handleuserremove());
        suspend.setOnAction(e->suspendUser());
        cancel3.setOnAction(e->hideremoveuserpanel());
        unsuspend.setOnAction(e->unsuspendUser());
        confirm3.setOnAction(e->deleteUser());
        revenue.setOnAction(e->enablerevenuereportpanel());
        airlinebox1.setOnAction(e->generateRevenueReport());      
        viewflights2.setOnAction(e->enableflightspanel());
        set5.setOnAction(e->verifyflight());
        reschedule.setOnAction(e->handlereschedule());
        cancel4.setOnAction(e->hidebuttons());
        confirm4.setOnAction(e->rescheduleFlight());
        addflight.setOnAction(e->enableaddflightpanel());
        add.setOnAction(e->handleaddflight());
        cancel5.setOnAction(e->hidebuttons());
        confirm5.setOnAction(e->addFlight());
    }
    
    private void disableallpanels()
    {
    	viewairlinespanel.setVisible(false);
    	addairlinepanel.setVisible(false);
    	removeairlinepanel.setVisible(false);
    	viewuserspanel.setVisible(false);
    	suspensionpanel.setVisible(false);
    	removeuserpanel.setVisible(false);
        revenuereportpanel.setVisible(false);
        subpanel.setVisible(false);
        rescheduleflightspanel.setVisible(false);
        
        
    	revenue.setStyle("-fx-background-color: #008080; -fx-text-fill: white;");
    	viewairlines.setStyle("-fx-background-color: #008080; -fx-text-fill: white;");
    	viewusers.setStyle("-fx-background-color: #008080; -fx-text-fill: white;");
    	viewflights2.setStyle("-fx-background-color: #008080; -fx-text-fill: white;");

    	clearlabels();
    }
    
    private void clearlabels()
    {
    	
    	remove.setVisible(false);
    	cancelairlineremove.setVisible(false);
    	confirmairlineremove.setVisible(false);
    	addairlineprompt.setText("");
    	warninglabel.setVisible(false);
    	airlineiderror.setText("");
    	addairlineerror.setText("");
    	removeuser.setVisible(false); 
        suspension.setVisible(false);// Show the remove button if the user ID is found
        useriderror.setText("");  // Clear error message if ID is found
        suspensionpanel.setVisible(false);
    	removeuserpanel.setVisible(false);
		userdeleteprompt.setText("");
		
		airlinenamelabel.setText("");
		totalbookings.setText("");
		activeflights.setText("");
		revenuegenerated.setText("");
		rescheduleerrorlabel.setText("");
		rescheduleprompt.setText("");
		cancel3.setVisible(false);
		confirm3.setVisible(false);
		addflightpaneldisable();

    }

    
    
 
    
    //////////////////flights
    // response to the check button in search flights
    
    
    private void addFlight() {
        try {
            // Validate all fields
            if (from.getValue() == null || to.getValue() == null || minprice.getText().isEmpty() || seats.getText().isEmpty()) {
                rescheduleerrorlabel.setText("Field(s) not entered");
                rescheduleerrorlabel.setVisible(true);
                return;
            }

            // Check if source and destination are the same
            String sourceLocation = from.getValue().toString();
            String destinationLocation = to.getValue().toString();
            if (sourceLocation.equals(destinationLocation)) {
                rescheduleerrorlabel.setText("Destination/Source are same");
                rescheduleerrorlabel.setVisible(true);
                return;
            }

            // Parse price and available seats
            float price = Float.parseFloat(minprice.getText());
            int availableSeats = Integer.parseInt(seats.getText());

            // Get selected airline
            String airlineName = (airlinebox2.getValue() != null && !airlinebox2.getValue().toString().isEmpty())
                    ? airlinebox2.getValue().toString()
                    : "Default Airline"; // Set a default value if not selected

            // Get date and time
            LocalDate selectedDate = date.getValue();
            if (selectedDate == null) {
                rescheduleerrorlabel.setText("Field(s) not entered");
                rescheduleerrorlabel.setVisible(true);
                return;
            }

            Integer hour = (Integer) hrspinner.getValue();
            Integer minute = (Integer) minspinner.getValue();

            LocalTime selectedTime = LocalTime.of(hour, minute);
            LocalDateTime combinedDateTime = LocalDateTime.of(selectedDate, selectedTime);

            String formattedDate = combinedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String formattedTime = combinedDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            // Call the non-database function to add flight to the list
            FlightManagementSystem.database.addNewFlight(formattedDate, formattedTime, sourceLocation,
                    destinationLocation, airlineName, availableSeats, price);

            // Indicate success
            rescheduleprompt.setText("Flight added.");
            rescheduleprompt.setVisible(true);
            subpanel.setVisible(false);
            populateFlightTable(0, 0);
            // Hide error label if previously visible
            rescheduleerrorlabel.setVisible(false);

            // Reset fields
            from.setValue(null);
            to.setValue(null);
            minprice.clear();
            seats.clear();
            airlinebox2.setValue(null);
            date.setValue(null);
            hrspinner.getValueFactory().setValue(0);
            minspinner.getValueFactory().setValue(0);

        } catch (NumberFormatException e) {
            rescheduleerrorlabel.setText("Invalid number format in fields.");
            rescheduleerrorlabel.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            rescheduleerrorlabel.setText("Error adding flight.");
            rescheduleerrorlabel.setVisible(true);
        }
    }

    
    private void handleaddflight()
    {
    	cancel5.setVisible(true);
    	confirm5.setVisible(true);
    }
    
    private void enableaddflightpanel()
    {
    	addflightpanelenable();
    	subpanel.setVisible(true);
    	
    	
    	
    }
    
    private void rescheduleFlight() {
        try {
            // Get selected date from DatePicker
            LocalDate selectedDate = date.getValue();
            if (selectedDate == null) {
                System.out.println("No date selected!");
                return;
            }

            // Get hour and minute values from Spinners
            Integer hour = (Integer) hrspinner.getValue();
            Integer minute = (Integer) minspinner.getValue();

            // Combine into LocalDateTime
            LocalTime selectedTime = LocalTime.of(hour, minute);
            LocalDateTime combinedDateTime = LocalDateTime.of(selectedDate, selectedTime);

            // Format date and time for SQL
            String formattedDate = combinedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String formattedTime = combinedDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            // Get flight ID from text field
            int flightID = Integer.parseInt(rescheduleid.getText());

            // Check the airline selection, default to "Unchanged" if not selected
            String selectedAirline = (airlinebox2.getValue() != null && !airlinebox2.getValue().toString().isEmpty())
                    ? airlinebox2.getValue().toString() 
                    : "Unchanged";

            // Update the database and ArrayList with all the values
            FlightManagementSystem.database.updateFlightSchedule(flightID, formattedDate, formattedTime, selectedAirline);
            rescheduleprompt.setText("Flight rescheduled.");
            populateFlightTable(1, flightID);
            subpanel.setVisible(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hidebuttons()
    {
  	  confirm4.setVisible(false);
  	  cancel4.setVisible(false);
  	cancel5.setVisible(false);
	confirm5.setVisible(false);
    }
    
    
  private void handlereschedule()
  {
	  confirm4.setVisible(true);
	  cancel4.setVisible(true);
  }
     private void enableflightspanel()
     {
    	 disableallpanels();
     	viewflights2.setStyle("-fx-background-color: red; -fx-text-fill: white;");
     	rescheduleflightspanel.setVisible(true);
        populateFlightTable(0, 0);
        loadairlinebox2(); 

     }
 private void addflightpanelenable()
    {
    	reschedule.setVisible(false);
    	add.setVisible(true);
    	fromtolabel.setVisible(true);
    	from.setVisible(true);
    	to.setVisible(true);
    	minprice.setVisible(true);
    	seats.setVisible(true);
    	cancel5.setVisible(false);
    	confirm5.setVisible(false);
    }
    
    private void addflightpaneldisable()
    {
    	reschedule.setVisible(true);
    	add.setVisible(false);
    	fromtolabel.setVisible(false);
    	from.setVisible(false);
    	to.setVisible(false);
    	minprice.setVisible(false);
    	seats.setVisible(false);
    	cancel5.setVisible(false);
    	confirm5.setVisible(false);
    	
    }
    private void verifyflight() {
        // Get the entered Flight ID
        String enteredFlightID = rescheduleid.getText().trim();

        // Check if either field is empty
        if (enteredFlightID.isEmpty() ) {
        	rescheduleerrorlabel.setText("Field is Blank");
            
            return;
        }

        try {
            // Parse the Flight ID as an integer
             flightID = Integer.parseInt(enteredFlightID);

            // Search for the flight in the flights list
            Flight matchingFlight = FlightManagementSystem.flights.stream()
                    .filter(flight -> flight.getFlightID() == flightID)
                    .findFirst()
                    .orElse(null);
             
            if (matchingFlight != null) {
                // Flight found, clear error labels
              
               
                clearlabels();
               
                subpanel.setVisible(true);
                
                populateFlightTable(1, flightID);
                // Update the seatprice label with the price
            } 
            
            else {
            	 clearlabels();
                // No flight found with the given ID
            	rescheduleerrorlabel.setText("Incorrect ID");
            
            	subpanel.setVisible(false);
                populateFlightTable(0, 0);
               
            }

        } catch (NumberFormatException e) {
            // Handle invalid Flight_ID format
        	subpanel.setVisible(false);
        	rescheduleerrorlabel.setText("Incorrect ID");
           
            
            populateFlightTable(0, 0);
        }
    }

    
    
    
    
    
    //fills the tableview for searchallflights
    //type 0 for all flights, type 
    private void populateFlightTable(int type, int id) { 
        // Clear any existing columns to prevent duplicates
    	rescheduletable.getColumns().clear();

        // Define columns for the flight table
        TableColumn<Flight, Integer> flightIDColumn = new TableColumn<>("Flight ID");
        flightIDColumn.setCellValueFactory(new PropertyValueFactory<>("flightID"));

        TableColumn<Flight, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Flight, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        TableColumn<Flight, String> sourceColumn = new TableColumn<>("Source");
        sourceColumn.setCellValueFactory(new PropertyValueFactory<>("sourceLocation"));

        TableColumn<Flight, String> destinationColumn = new TableColumn<>("Destination");
        destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destinationLocation"));

        TableColumn<Flight, String> airlineColumn = new TableColumn<>("Airline");
        airlineColumn.setCellValueFactory(new PropertyValueFactory<>("airlineName"));

        TableColumn<Flight, Integer> seatsColumn = new TableColumn<>("seats");
        seatsColumn.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));

        TableColumn<Flight, Float> priceColumn = new TableColumn<>("Min Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Add columns to the table view
        rescheduletable.getColumns().addAll(flightIDColumn, dateColumn, timeColumn, sourceColumn, destinationColumn, airlineColumn, seatsColumn, priceColumn);

        // Create the filtered flight list
        ObservableList<Flight> flightList;
        if (type == 0) {
            // Display all flights
            flightList = FXCollections.observableArrayList(FlightManagementSystem.flights);
        } else if (type == 1) {
            // Display only the flight with the specified id
            flightList = FXCollections.observableArrayList(
                FlightManagementSystem.flights.stream()
                    .filter(flight -> flight.getFlightID() == id)
                    .toList()
            );
        } else {
            // Invalid type, use an empty list
            flightList = FXCollections.observableArrayList();
        }

        // Set the items of the table view
        rescheduletable.setItems(flightList);
    }

    
    
    
    
    
    //////////////////users
    private void generateRevenueReport() {
        String option = airlinebox1.getValue().toString(); // Get the selected airline option
        double revenue = 0;
        int totalBookingsCount = 0;
        int activeFlightsCount = 0;

        if (option.equals("All")) {
            // Update the airline label
            airlinenamelabel.setText("All Airlines");

            // Count total bookings
            totalBookingsCount = FlightManagementSystem.bookings.size();

            // Count active flights
            activeFlightsCount = FlightManagementSystem.flights.size();

            // Calculate revenue for all airlines
            for (Booking booking : FlightManagementSystem.bookings) {
                Flight flight = FlightManagementSystem.flights.stream()
                                       .filter(f -> f.getFlightID() == booking.getFlightID())
                                       .findFirst()
                                       .orElse(null);

                if (flight != null) {
                    double price = flight.getPrice();
                    switch (booking.getSeatType()) {
                        case "Economy Class":
                            revenue += price;
                            break;
                        case "Business Class":
                            revenue += price + 6000;
                            break;
                        case "First Class":
                            revenue += price + 12000;
                            break;
                        default:
                            break; // No action for other seat types
                    }
                }
            }
        } else {
            // Update the airline label
            airlinenamelabel.setText(option);

            // Filter flights for the selected airline
            List<Flight> airlineFlights = FlightManagementSystem.flights.stream()
                                                 .filter(f -> f.getAirlineName().equals(option))
                                                 .collect(Collectors.toList());
            activeFlightsCount = airlineFlights.size();

            // Count bookings and calculate revenue for the selected airline
            for (Booking booking : FlightManagementSystem.bookings) {
                Flight flight = airlineFlights.stream()
                                              .filter(f -> f.getFlightID() == booking.getFlightID())
                                              .findFirst()
                                              .orElse(null);

                if (flight != null) {
                    totalBookingsCount++;
                    double price = flight.getPrice();
                    switch (booking.getSeatType()) {
                        case "Economy Class":
                            revenue += price;
                            break;
                        case "Business Class":
                            revenue += price + 6000;
                            break;
                        case "First Class":
                            revenue += price + 12000;
                            break;
                        default:
                            break; // No action for other seat types
                    }
                }
            }
        }

        // Update the labels
        totalbookings.setText(String.valueOf(totalBookingsCount));
        activeflights.setText(String.valueOf(activeFlightsCount));
        revenuegenerated.setText(String.format("%.2f", revenue)); // Format to 2 decimal places
    }

    
    private void enablerevenuereportpanel()
    {
    	disableallpanels();
    	revenue.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        revenuereportpanel.setVisible(true);
        loadairlinebox1();
        
    }
    
    private void loadairlinebox1() {
        // Clear the ChoiceBox to ensure it's empty before populating
        airlinebox1.getItems().clear();

        // Add all airline names from the airlines ArrayList
        for (Airline airline : FlightManagementSystem.airlines) {
            airlinebox1.getItems().add(airline.getAirlineName());
        }

        // Add the "All" option at the end
        airlinebox1.getItems().add("All");

        // Optionally, set a default value
        airlinebox1.setValue("All");
    }

    
    
    private void loadairlinebox2() {
        // Clear the ChoiceBox to ensure it's empty before populating
        airlinebox2.getItems().clear();

        // Add all airline names from the airlines ArrayList
        for (Airline airline : FlightManagementSystem.airlines) {
            airlinebox2.getItems().add(airline.getAirlineName());
        }

        // Add the "All" option at the end
        airlinebox2.getItems().add("Unchanged");

        // Optionally, set a default value
        airlinebox2.setValue("Unchanged");
    }

    
    
    
    
    
    
    private void deleteUser()
    {
    	boolean status = FlightManagementSystem.database.removeUser(uID);
    	
    	if (status == true)
    	{
    		userdeleteprompt.setText("User deleted successfully.");
    	}
    	
    	else 
    	{
    		userdeleteprompt.setText("Error removing user.");
    	}
    	hideremoveuserpanel();
    	populateUsersTable(-1);
    }
    
    private void suspendUser()
    {
    	FlightManagementSystem.database.suspend(uID);
    	populateUsersTable(uID);
    	suspensionprompt.setText("");
        }
    
    private void unsuspendUser()
    {
    	FlightManagementSystem.database.unsuspend(uID);
    	populateUsersTable(uID);
    	
        }
    
    
    private void hideremoveuserpanel()
    {
    	removeuserpanel.setVisible(false);
    }
    private void handlesuspension()
    {
    	suspensionpanel.setVisible(true);
    	removeuserpanel.setVisible(false);
    	
    }
    private void handleuserremove()
    {
    	suspensionpanel.setVisible(false);
    	removeuserpanel.setVisible(true);
    	cancel3.setVisible(true);
    	confirm3.setVisible(true);
    	
    }
    
    
    private void verifyUser() 
    {
        // Check if the TextField is blank
        if (userid.getText().equals("")) {
            useriderror.setText("Field is empty");
            removeuser.setVisible(false); 
            clearlabels();
            // Hide the remove button if the field is empty
        }
        
        else {
            try {
                // Parse the entered user ID from the TextField
                  uID = Integer.parseInt(userid.getText());

                // Assume that 'found' is initially false
                boolean found = false;

                // Iterate through the users ArrayList to check if the entered ID exists
                for (User user : FlightManagementSystem.users) {
                    if (user.getID() == uID) {
                        found = true;
                        break;  // Stop searching once the ID is found
                    }
                }

                // Show/hide elements based on whether the user ID was found
                if (found) {
                	populateUsersTable(uID);
                    removeuser.setVisible(true); 
                    suspension.setVisible(true);// Show the remove button if the user ID is found
                    useriderror.setText("");  // Clear error message if ID is found
                } else {
                    removeuser.setVisible(false); 
                    clearlabels();// Hide the remove button if the ID is not found
                    useriderror.setText("Incorrect ID");  // Show an error message for incorrect ID
                }
            } catch (NumberFormatException e) {
                // Handle invalid number format
            	clearlabels();
                useriderror.setText("Invalid ID format");
                removeuser.setVisible(false);  // Hide remove button if the format is incorrect
            }
        } 
    }

    
    private void enableuserspanel()
    {
    	disableallpanels();
    	populateUsersTable(-1);
    	viewuserspanel.setVisible(true);
    	viewusers.setStyle("-fx-background-color: red; -fx-text-fill: white;");

    }
    
    
    
    private void populateUsersTable(int userID) {
        // Clear any existing columns to prevent duplicates
        usertable.getColumns().clear();

        // Define the columns for the usertable
        TableColumn<User, Integer> userIDColumn = new TableColumn<>("User ID");
        userIDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));

        TableColumn<User, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        // Column to display Account Status (Active or Suspended)
        TableColumn<User, String> accountStatusColumn = new TableColumn<>("Account Status");
        accountStatusColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String status = "";
            
            // Directly map the user type to account status
            if (user.getType() == 1) {
                status = "Active"; // If usertype is 1, account status is "Active"
            } 
            else if (user.getType() == 3) {
                status = "Suspended"; // If usertype is 3, account status is "Suspended"
            } else {
                status = "Unknown"; // For any other usertype
            }
            
            return new SimpleStringProperty(status);
        });

        // Add the columns to the usertable
        usertable.getColumns().addAll(userIDColumn, usernameColumn, accountStatusColumn);

        // Create the filtered user list based on the parameter
        List<User> filteredUsers;
        if (userID == -1) {
            // Display all users
            filteredUsers = FlightManagementSystem.users;
        } else {
            // Display only the user with the given ID
            filteredUsers = FlightManagementSystem.users.stream()
                    .filter(user -> user.getID() == userID) // Use getID() instead of directly accessing the field
                    .collect(Collectors.toList());
        }

        // Set the items of the usertable to the filtered list of users
        ObservableList<User> userList = FXCollections.observableArrayList(filteredUsers);
        usertable.setItems(userList);
    }


    
    
    ////////////////////////////////airlines
    private void removeAirline()
    {
    	FlightManagementSystem.database.removeAirline(aID);
    	populateAirlineTable();
      clearlabels();	
    }
    
    
    private void hideremovebuttons()
    {
    	cancelairlineremove.setVisible(false);
   	 confirmairlineremove.setVisible(false);
   	 warninglabel.setVisible(false);
    }
    
     private void handleRemoveAirline()
     {
    	 cancelairlineremove.setVisible(true);
    	 confirmairlineremove.setVisible(true);
    	 warninglabel.setVisible(true);
    	 
    	 
     }
    
    
    // when the set button is pressed
    private void verifyairline() {
        // Check if the TextField is blank
        if (airlineID.getText().equals("")) {
        	populateUsersTable(-1);
            airlineiderror.setText("Field is empty");
            remove.setVisible(false);  // Hide remove button if the field is empty
        } 
        
        else {
            try {
                // Parse the entered airline ID from the TextField
         	   aID = Integer.parseInt(airlineID.getText());

                
                // Assume that 'found' is initially false
                boolean found = false;

                // Iterate through the airlines ArrayList to check if the entered ID exists
                for (Airline airline : FlightManagementSystem.airlines) {
                    if (airline.getAirlineID() == aID) {
                        found = true;
                        break;  // Stop searching once the ID is found
                    }
                }

                // Show/hide elements based on whether the airline ID was found
                if (found) {
                    remove.setVisible(true);  // Show the remove button if the airline ID is found
                    airlineiderror.setText("");  // Clear error message if ID is found
                } 
                
                else {
                    remove.setVisible(false);  // Hide the remove button if the ID is not found
                    airlineiderror.setText("Incorrect ID");  // Show an error message for incorrect ID
                    populateUsersTable(-1);
                }
            } 
            
            catch (NumberFormatException e) {
                // Handle invalid number format
                airlineiderror.setText("Invalid ID format");
                remove.setVisible(false);  // Hide remove button if the format is incorrect
            }
        }
    }

    
    private void registerairline() {
        String airlineNameText = airlinename.getText().trim();  // Get the trimmed text
        
        // Check if the text is empty first
        if (airlineNameText.isEmpty()) {
            addairlineprompt.setText("");
            addairlineerror.setText("Field is empty.");
            addairlineerror.setVisible(true);
        } 
        
        else {
            // Call the addAirline method to register the airline
            boolean status = FlightManagementSystem.database.addAirline(airlineNameText);
            populateAirlineTable(); 
            if (status) {
                addairlineprompt.setText("Airline registered.");
                addairlineerror.setText("");
                addairlineerror.setVisible(false);
                
                airlinename.setText("");  // Clear the text field after success
            } 
            
            else {
                addairlineprompt.setText("");
                addairlineerror.setText("Airline already exists.");
                
            }
        }
        
      // Update the airline table after registration attempt
    }

    private void enableaddairlinepanel()
    {
    	addairlinepanel.setVisible(true);
    	removeairlinepanel.setVisible(false);
    }
    
    private void enableremoveairlinepanel()
    {
    	addairlinepanel.setVisible(false);
    	removeairlinepanel.setVisible(true);
    }
    
    private void populateAirlineTable() {
        // Clear any existing columns to prevent duplicates
        airlinetable.getColumns().clear();

        // Define the columns for the airlinetable
        TableColumn<Airline, Integer> airlineIDColumn = new TableColumn<>("Airline ID");
        airlineIDColumn.setCellValueFactory(new PropertyValueFactory<>("airlineID"));

        TableColumn<Airline, String> airlineNameColumn = new TableColumn<>("Airline Name");
        airlineNameColumn.setCellValueFactory(new PropertyValueFactory<>("airlineName"));

        TableColumn<Airline, Integer> activeFlightsColumn = new TableColumn<>("Active Flights");
        activeFlightsColumn.setCellValueFactory(cellData -> {
            Airline airline = cellData.getValue();
            // Calculate the total number of active flights for this airline
            long activeFlightsCount = FlightManagementSystem.flights.stream()
                    .filter(flight -> flight.getAirlineName().equals(airline.getAirlineName()))
                    .count();
            return new SimpleIntegerProperty((int) activeFlightsCount).asObject();
        });

        // Add the columns to the airlinetable
        airlinetable.getColumns().addAll(airlineIDColumn, airlineNameColumn, activeFlightsColumn);

        // Set the items of the airlinetable to the list of airlines
        ObservableList<Airline> airlineList = FXCollections.observableArrayList(FlightManagementSystem.airlines);
        airlinetable.setItems(airlineList);
    }

    
    
    private void enableairlinespanel()
    {    
    	disableallpanels();
    	populateAirlineTable();
    	viewairlinespanel.setVisible(true);
    	viewairlines.setStyle("-fx-background-color: red; -fx-text-fill: white;");
         
    }
    
  
    
    
        private void handleLogout() {
        	//red
        	logoutbutton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
           
            confirmbutton.setVisible(true);
            cancelbutton.setVisible(true);
        }

        private void cancelLogout() {
        	//teal
        	logoutbutton.setStyle("-fx-background-color: #008080; -fx-text-fill: white;");
            confirmbutton.setVisible(false);
            cancelbutton.setVisible(false);
        }

        private void confirmLogout() {
          //teal         	logoutbutton.setStyle("-fx-background-color: #008080; -fx-text-fill: white;");

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("loginsignup.fxml"));
                Parent loginRoot = loader.load();
                stage.setScene(new Scene(loginRoot, 700, 500));
                new LoginController(loginRoot, stage);  // Initialize LoginController for login view
            } catch (IOException e) {
                System.out.println("Failed to load loginsignup FXML: " + e.getMessage());
            }
        }
      
    
    
    
    
}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //Business Layer Classes
    
    
    
    
    
    
    
    public static class User {
        private int ID;
        private int type;
        private String username;

        // Constructor
        public User(int id, int type, String username) {
            this.ID = id;
            this.type = type;
            this.username = username;
        }

        // Getter and Setter for ID
        public int getID() {
            return ID;
        }

        public void setID(int ID) {
            this.ID = ID;
        }

        // Getter and Setter for Type
        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        // Getter and Setter for Username
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public String toString() {
            return "User{" +
                   "ID=" + ID +
                   ", type=" + type +
                   ", username='" + username + '\'' +
                   '}';
        }
    }
    
    
    public static class Flight {
        private int flightID;
        private String date;  // Changed from DATE to String
        private String time;
        private String sourceLocation;
        private String destinationLocation;
        private String airlineName;
        private int availableSeats;
        private float price;

        // Constructor
        public Flight(int flightID, String date, String time, String sourceLocation, 
                      String destinationLocation, String airlineName, int availableSeats, float price) {
            this.flightID = flightID;
            this.date = date;
            this.time = time;
            this.sourceLocation = sourceLocation;
            this.destinationLocation = destinationLocation;
            this.airlineName = airlineName;
            this.availableSeats = availableSeats;
            this.price = price;
        }

        // Getters
        public int getFlightID() {
            return flightID;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getSourceLocation() {
            return sourceLocation;
        }

        public String getDestinationLocation() {
            return destinationLocation;
        }

        public String getAirlineName() {
            return airlineName;
        }

        public int getAvailableSeats() {
            return availableSeats;
        }

        public float getPrice() {
            return price;
        }
        
        public void setDate(String a) {
            date =a;
        }

        public void setTime(String a) {
            time = a;
        }
        
        public void setAirlineName(String a) {
            airlineName = a;
        }

        
        public void setAvailableSeats(int seats) {
            this.availableSeats = seats;
        }

    }

    public static class Booking {
        private int bookingID;
        private int passengerID;
        private int flightID;
        String seattype;

        // Constructor
        public Booking(int bookingID, int passengerID, int flightID, String st) {
            this.bookingID = bookingID;
            this.passengerID = passengerID;
            this.flightID = flightID;
            this.seattype = st;
        }

        // Getters
        public int getBookingID() {
            return bookingID;
        }

        public int getPassengerID() {
            return passengerID;
        }

        public int getFlightID() {
            return flightID;
        }
        
        public String getSeatType()
        {
        	return seattype;
        }
    }
    
    
    
    public static class BoardingPass {
    
        
    	public int passID;
        public int passengerID;
        public int flightID;
       
   
      
        // Setters
        
        public void setPassID(int id) {
            this.passID = id;
        }
        
        
        public void setPassengerID(int passengerid) {
            this.passengerID = passengerid;
        }

        public void setFlightID(int id) {
            this.flightID = id;
        }

       
        // Getters (optional if needed elsewhere)
        public int getPassengerID() {
            return passengerID;
        }

        public int getFlightID() {
            return flightID;
        }
        
        public int getPassID() {
            return passID;
        }

      
    }
    
    public static class EconomyPass extends BoardingPass
    {
    	String mealtype;
    	String passtype;
    	
    	EconomyPass()
    	{
    		mealtype = "standard";
    		passtype = "Economy Class";
    	}
    	
       
    }
    
    public static class BusinessPass extends BoardingPass
    {
    	String mealtype;
    	String passtype;
    	String amenities;
    	BusinessPass()
    	{
    		mealtype = "Two-meal Course";
    		passtype = "Business Class";
    		amenities = "Leg rest, Hot towel";
    	}
    	
       
    }
    
    public static class FirstclassPass extends BoardingPass
    {
    	String mealtype;
    	String passtype;
    	String amenities;
    	FirstclassPass()
    	{
    		mealtype = "Choice bouffette";
    		passtype = "First Class";
    		amenities = "Sleeping Bed, Vip lounge, Leg rest, Hot towel";
    	}
    	
       
    }
    
    
    
    
    
    public static class Airline {
        private int airlineID;
        private String airlineName;

        // Constructor
        public Airline(int airlineID, String airlineName) {
            this.airlineID = airlineID;
            this.airlineName = airlineName;
        }

        // Getters
        public int getAirlineID() {
            return airlineID;
        }

        public String getAirlineName() {
            return airlineName;
        }
    }
    
    
    
    
    // Inner class for database operations
    public static class DB {
        private String url;

        DB() {
            url = "jdbc:sqlserver://DESKTOP-IFDKF36\\SQLEXPRESS:1433;databaseName=AirportManagementSystem;integratedSecurity=true;encrypt=false;";
            try (Connection conn = DriverManager.getConnection(url)) {
                System.out.println("Database connection established successfully.");
            } catch (SQLException e) {
                System.out.println("Database connection failed: " + e.getMessage());
            }
        }

        public boolean checkCredentials(String username, String password)
        {
            String query = "SELECT * FROM Users WHERE username = ? AND password = ?";

            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, username);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();
                return rs.next(); // If a row is returned, credentials match
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
                return false;
            }
        }
         
             
        public void UserInfoExtract(String username)
        {
            String query = "SELECT userid, usertype FROM Users WHERE username = ?";
            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int userID = rs.getInt("userid");
                    int userType = rs.getInt("usertype");

                    // Update the static User variable
                    FlightManagementSystem.user = new User(userID, userType, username);
                    System.out.println("User info extracted: " + FlightManagementSystem.user);
                } else {
                    System.out.println("No user found with username: " + username);
                }
            } catch (SQLException e) {
                System.out.println("Error querying user information: " + e.getMessage());
            }
        }

        public Flight addNewFlight(String date, String time, String sourceLocation, String destinationLocation,
                String airlineName, int availableSeats, float price) {
			// First, query to get the maximum Flight_ID from the database
			String maxIdQuery = "SELECT MAX(Flight_ID) FROM Flight";
			int newFlightID = 1; // Default ID if no flights exist
			
			try (Connection conn = DriverManager.getConnection(url);
			PreparedStatement maxStmt = conn.prepareStatement(maxIdQuery);
			ResultSet rs = maxStmt.executeQuery()) {
			
			if (rs.next()) {
			// If there's at least one flight in the database, get the max Flight_ID and increment by 1
			newFlightID = rs.getInt(1) + 1;
			}
			
			// Now insert the new flight with the calculated Flight_ID
			String insertQuery = "INSERT INTO Flight (Flight_ID, date, time, Source_Location, Destination_Location, Airline_Name, Available_Seats, price) "
			               + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
			// Set the parameters for the insert query
			stmt.setInt(1, newFlightID); // Set the generated Flight_ID
			stmt.setString(2, date);
			stmt.setString(3, time);
			stmt.setString(4, sourceLocation);
			stmt.setString(5, destinationLocation);
			stmt.setString(6, airlineName);
			stmt.setInt(7, availableSeats);
			stmt.setFloat(8, price);
			
			// Execute the insert query
			stmt.executeUpdate();
			
			// Create a new Flight object with the generated ID
			Flight newFlight = new Flight(newFlightID, date, time, sourceLocation, destinationLocation, airlineName, availableSeats, price);
			
			// Add the newly created flight to the flightList
			FlightManagementSystem.flights.add(newFlight);
			
			// Return the newly created Flight object
			return newFlight;
			} catch (SQLException e) {
			e.printStackTrace();
			}
			} catch (SQLException e) {
			e.printStackTrace();
			}
			
			return null; // Return null if an error occurs
			}


        private void updateFlightSchedule(int flightID, String formattedDate, String formattedTime, String selectedAirline) {
            try (Connection conn = DriverManager.getConnection(url)) {
                // Retrieve original flight details from the database
                String query = "SELECT date, time, Airline_Name FROM Flight WHERE Flight_ID = ?";
                String originalDate = null, originalTime = null, originalAirline = null;

                try (PreparedStatement selectStmt = conn.prepareStatement(query)) {
                    selectStmt.setInt(1, flightID);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            originalDate = rs.getString("date");
                            originalTime = rs.getString("time");
                            originalAirline = rs.getString("Airline_Name");
                        } else {
                            System.out.println("Flight not found in database!");
                            return;
                        }
                    }
                }

                // Determine final values for database and ArrayList
                String finalDate = (formattedDate != null) ? formattedDate : originalDate;
                String finalTime = (formattedTime != null) ? formattedTime : originalTime;
                String finalAirline = (!"Unchanged".equals(selectedAirline)) ? selectedAirline : originalAirline;

                // Check if any changes are necessary
                if (finalDate.equals(originalDate) && finalTime.equals(originalTime) && finalAirline.equals(originalAirline)) {
                    System.out.println("No changes detected. Database and ArrayList remain unchanged.");
                    return;
                }

                // Update the database only if necessary
                String updateQuery = "UPDATE Flight SET date = ?, time = ?, Airline_Name = ? WHERE Flight_ID = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, finalDate);
                    updateStmt.setString(2, finalTime);
                    updateStmt.setString(3, finalAirline);
                    updateStmt.setInt(4, flightID);

                    int rowsUpdated = updateStmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        System.out.println("Database updated successfully.");
                    } else {
                        System.out.println("Error updating the database.");
                    }
                }

                // Update the ArrayList to match the database
                for (Flight flight : FlightManagementSystem.flights) {
                    if (flight.getFlightID() == flightID) {
                        flight.setDate(finalDate);    // Update date
                        flight.setTime(finalTime);    // Update time
                        flight.setAirlineName(finalAirline); // Update airline name
                        System.out.println("ArrayList updated successfully.");
                        break;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error updating database or ArrayList!");
            }
        }

        
        
        
        boolean removeAirline(int airlineID) {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                // Establish a connection to the database
                conn = DriverManager.getConnection(FlightManagementSystem.database.url);

                // Fetch the airline name associated with the given ID
                String getAirlineQuery = "SELECT airlinename FROM Airline WHERE airlineid = ?";
                stmt = conn.prepareStatement(getAirlineQuery);
                stmt.setInt(1, airlineID);
                rs = stmt.executeQuery();

                String airlineName = null;
                if (rs.next()) {
                    airlineName = rs.getString("airlinename");
                } else {
                    // Airline not found
                    System.out.println("Airline not found for ID: " + airlineID);
                    return false;
                }
                rs.close();
                stmt.close();

                // Ensure that airlineName is not modified after its initial assignment
                final String finalAirlineName = airlineName;

                // Find all flight IDs associated with this airline name
                String getFlightIDsQuery = "SELECT Flight_ID FROM Flight WHERE Airline_Name = ?";
                stmt = conn.prepareStatement(getFlightIDsQuery);
                stmt.setString(1, finalAirlineName);
                rs = stmt.executeQuery();

                ArrayList<Integer> flightIDs = new ArrayList<>();
                while (rs.next()) {
                    flightIDs.add(rs.getInt("Flight_ID"));
                }
                rs.close();
                stmt.close();

                // Delete all boarding passes associated with the flight IDs
                String deleteBoardingPassQuery = "DELETE FROM BoardingPass WHERE flightID = ?";
                stmt = conn.prepareStatement(deleteBoardingPassQuery);
                for (int flightID : flightIDs) {
                    stmt.setInt(1, flightID);
                    stmt.executeUpdate();

                    // Remove boarding passes from the in-memory list
                    FlightManagementSystem.boardingpasses.removeIf(pass -> pass.getFlightID() == flightID);
                }
                stmt.close();

                // Delete all flights associated with the airline
                String deleteFlightsQuery = "DELETE FROM Flight WHERE Airline_Name = ?";
                stmt = conn.prepareStatement(deleteFlightsQuery);
                stmt.setString(1, finalAirlineName);
                stmt.executeUpdate();
                stmt.close();

                // Remove flights from the in-memory list
                FlightManagementSystem.flights.removeIf(flight -> flight.getAirlineName().equals(finalAirlineName));

                // Delete the airline from the Airline table
                String deleteAirlineQuery = "DELETE FROM Airline WHERE airlineid = ?";
                stmt = conn.prepareStatement(deleteAirlineQuery);
                stmt.setInt(1, airlineID);
                stmt.executeUpdate();
                stmt.close();

                // Remove the airline from the airlines ArrayList
                FlightManagementSystem.airlines.removeIf(airline -> airline.getAirlineID() == airlineID);

                return true;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        
        //users manage
        boolean removeUser(int userID) {
            boolean userRemoved = false;

            try (Connection conn = DriverManager.getConnection(url)) {
                // Delete associated bookings from the Booking table
                String deleteBookingQuery = "DELETE FROM Booking WHERE passengerid = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteBookingQuery)) {
                    stmt.setInt(1, userID);
                    stmt.executeUpdate();
                }

                // Delete associated boarding passes from the BoardingPass table
                String deleteBoardingPassQuery = "DELETE FROM BoardingPass WHERE passengerID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteBoardingPassQuery)) {
                    stmt.setInt(1, userID);
                    stmt.executeUpdate();
                }

                // Remove the user from the database
                String deleteUserQuery = "DELETE FROM Users WHERE userid = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteUserQuery)) {
                    stmt.setInt(1, userID);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        // Remove the user from the FlightManagementSystem.users list
                        FlightManagementSystem.users.removeIf(user -> user.getID() == userID);
                        System.out.println("User with ID " + userID + " has been removed from the system.");
                        userRemoved = true;
                    } else {
                        System.out.println("User with ID " + userID + " not found in the database.");
                    }
                }

                // Delete associated bookings from the FlightManagementSystem.bookings list
                FlightManagementSystem.bookings.removeIf(booking -> booking.getPassengerID() == userID);

                // Delete associated boarding passes from the FlightManagementSystem.boardingpasses list
                FlightManagementSystem.boardingpasses.removeIf(boardingPass -> boardingPass.getPassengerID() == userID);

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return userRemoved;
        }

        
        
        void suspend(int userID) {
            try (Connection conn = DriverManager.getConnection(url)) {
                // Update the usertype in the database to 3 (Suspended)
                String updateQuery = "UPDATE Users SET usertype = 3 WHERE userid = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    stmt.setInt(1, userID);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        // Update the usertype in the FlightManagementSystem.users list to 3
                        for (User user : FlightManagementSystem.users) {
                            if (user.getID() == userID) {
                                user.setType(3);  // Set usertype to 3 (Suspended)
                                break;
                            }
                        }
                        System.out.println("User with ID " + userID + " has been suspended.");
                    } else {
                        System.out.println("User not found in the database.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Function to unsuspend a user (set usertype to 1)
        void unsuspend(int userID) {
            try (Connection conn = DriverManager.getConnection(url)) {
                // Update the usertype in the database to 1 (Active)
                String updateQuery = "UPDATE Users SET usertype = 1 WHERE userid = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    stmt.setInt(1, userID);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        // Update the usertype in the FlightManagementSystem.users list to 1
                        for (User user : FlightManagementSystem.users) {
                            if (user.getID() == userID) {
                                user.setType(1);  // Set usertype to 1 (Active)
                                break;
                            }
                        }
                        System.out.println("User with ID " + userID + " has been unsuspended.");
                    } else {
                        System.out.println("User not found in the database.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        
        
        
        boolean addAirline(String name) {
         

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                // Establish a connection to the database
                conn = DriverManager.getConnection(FlightManagementSystem.database.url);

                // Check if the airline with the same name already exists
                String checkIfExistsQuery = "SELECT COUNT(*) FROM Airline WHERE airlinename = ?";
                stmt = conn.prepareStatement(checkIfExistsQuery);
                stmt.setString(1, name);
                rs = stmt.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    // Airline with the same name already exists
                    return false;
                }

                // Determine the next airline ID
                String getMaxIDQuery = "SELECT MAX(airlineid) AS maxID FROM Airline";
                stmt = conn.prepareStatement(getMaxIDQuery);
                rs = stmt.executeQuery();

                int newID = 1; // Default ID if the table is empty
                if (rs.next() && rs.getInt("maxID") > 0) {
                    newID = rs.getInt("maxID") + 1;
                }
                rs.close();
                stmt.close();

                // Insert the new airline into the database
                String insertQuery = "INSERT INTO Airline (airlineid, airlinename) VALUES (?, ?)";
                stmt = conn.prepareStatement(insertQuery);
                stmt.setInt(1, newID);
                stmt.setString(2, name);

                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    // Successfully added to the database; update the airlines ArrayList
                    Airline newAirline = new Airline(newID, name);
                    FlightManagementSystem.airlines.add(newAirline);
                    return true;
                }
                
          

            }
            catch (SQLException e) {
                e.printStackTrace();
            } 
            finally {
                // Close resources
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return false; // Return false if something went wrong
        }


        
        boolean deleteBooking(int flightID) {
            // Initialize the connection
            try (Connection conn = DriverManager.getConnection(url)) {
                
                // Check if the booking exists for the user with the given flight ID
                String checkBookingQuery = "SELECT COUNT(*) FROM Booking WHERE passengerid = ? AND flightid = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkBookingQuery)) {
                    checkStmt.setInt(1, FlightManagementSystem.user.ID);
                    checkStmt.setInt(2, flightID);
                    ResultSet resultSet = checkStmt.executeQuery();

                    if (resultSet.next() && resultSet.getInt(1) == 0) {
                        // No booking found for the user and flight ID
                        return false;
                    }
                }

                // Delete the booking from the Booking table
                String deleteBookingQuery = "DELETE FROM Booking WHERE passengerid = ? AND flightid = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteBookingQuery)) {
                    deleteStmt.setInt(1, FlightManagementSystem.user.ID);
                    deleteStmt.setInt(2, flightID);
                    deleteStmt.executeUpdate();
                }

                // Update the available seats for the flight
                String updateFlightQuery = "UPDATE Flight SET Available_Seats = Available_Seats + 1 WHERE Flight_ID = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateFlightQuery)) {
                    updateStmt.setInt(1, flightID);
                    updateStmt.executeUpdate();
                }

                // Remove booking from the array list
                FlightManagementSystem.bookings.removeIf(
                    booking -> booking.getFlightID() == flightID && booking.getPassengerID() == FlightManagementSystem.user.ID
                );

                // Update available seats in the Flight object
                for (Flight flight : FlightManagementSystem.flights) {
                    if (flight.getFlightID() == flightID) {
                        flight.setAvailableSeats(flight.getAvailableSeats() + 1);
                        break;
                    }
                }

                // Check if a corresponding boarding pass exists
                BoardingPass matchingPass = FlightManagementSystem.boardingpasses.stream()
                    .filter(pass -> pass.getFlightID() == flightID && pass.getPassengerID() == FlightManagementSystem.user.ID)
                    .findFirst()
                    .orElse(null);

                if (matchingPass != null) {
                    // Delete the boarding pass from the database
                    String deletePassQuery = "DELETE FROM BoardingPass WHERE passengerid = ? AND flightid = ?";
                    try (PreparedStatement deletePassStmt = conn.prepareStatement(deletePassQuery)) {
                        deletePassStmt.setInt(1, FlightManagementSystem.user.ID);
                        deletePassStmt.setInt(2, flightID);
                        deletePassStmt.executeUpdate();
                    }

                    // Remove the boarding pass from the array list
                    FlightManagementSystem.boardingpasses.remove(matchingPass);
                }

                // Booking and boarding pass deleted successfully
                return true;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        
        
        // adds a new booking
        boolean createBooking(int flightID, String seatType) {
          
            // Initialize the connection
            try (Connection conn = DriverManager.getConnection(url)) {
                
                // Verify that a booking doesn't already exist for the user with the same flight ID
                String checkBookingQuery = "SELECT COUNT(*) FROM Booking WHERE passengerid = ? AND flightid = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkBookingQuery)) {
                    checkStmt.setInt(1, FlightManagementSystem.user.ID);
                    checkStmt.setInt(2, flightID);
                    ResultSet resultSet = checkStmt.executeQuery();

                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        // Booking already exists
                        return false;
                    }
                }

                // Get the next booking ID (max + 1) or set to 1 if table is empty
                int newBookingID = 1;
                String getMaxBookingIDQuery = "SELECT MAX(bookingid) FROM Booking";
                try (PreparedStatement maxIDStmt = conn.prepareStatement(getMaxBookingIDQuery);
                     ResultSet maxIDResultSet = maxIDStmt.executeQuery()) {
                    if (maxIDResultSet.next() && maxIDResultSet.getInt(1) > 0) {
                        newBookingID = maxIDResultSet.getInt(1) + 1;
                    }
                }

                // Insert the new booking into the database
                String insertBookingQuery = "INSERT INTO Booking (bookingid, passengerid, flightid, seattype) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertBookingQuery)) {
                    insertStmt.setInt(1, newBookingID);
                    insertStmt.setInt(2, FlightManagementSystem.user.ID);
                    insertStmt.setInt(3, flightID);
                    insertStmt.setString(4, seatType);
                    insertStmt.executeUpdate();
                }

                // Update the available seats for the flight
                String updateFlightQuery = "UPDATE Flight SET Available_Seats = Available_Seats - 1 WHERE Flight_ID = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateFlightQuery)) {
                    updateStmt.setInt(1, flightID);
                    updateStmt.executeUpdate();
                }

                //updating flightcount
                for (Flight flight : FlightManagementSystem.flights) {
    	            if (flight.getFlightID() == flightID) {
    	                flight.setAvailableSeats(flight.getAvailableSeats() - 1);
    	                break;
    	            }
    	        }
                
   //adding booking
     		   Booking newBooking = new Booking(FlightManagementSystem.bookings.size()+1, FlightManagementSystem.user.ID, flightID, seatType);
     	        FlightManagementSystem.bookings.add(newBooking);
                // Booking created successfully
                return true;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        
        


        //reads data from database into arraylists
        public void readuserdata() {
            // Initialize array lists
            FlightManagementSystem.airlines = new ArrayList<>();
            FlightManagementSystem.flights = new ArrayList<>();
            FlightManagementSystem.bookings = new ArrayList<>();
            FlightManagementSystem.boardingpasses = new ArrayList<>();
            FlightManagementSystem.users = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection(url)) {
                // Read Airline data
                String airlineQuery = "SELECT airlineid, airlinename FROM Airline";
                try (PreparedStatement stmt = conn.prepareStatement(airlineQuery);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("airlineid");
                        String name = rs.getString("airlinename");
                        Airline airline = new Airline(id, name);
                        FlightManagementSystem.airlines.add(airline);
                    }
                }

                // Read Flight data
                String flightQuery = "SELECT Flight_ID, date, time, Source_Location, Destination_Location, Airline_Name, Available_Seats, price FROM Flight";
                try (PreparedStatement stmt = conn.prepareStatement(flightQuery);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("Flight_ID");
                        String date = rs.getString("date");
                        String time = rs.getString("time");
                        String source = rs.getString("Source_Location");
                        String destination = rs.getString("Destination_Location");
                        String airlineName = rs.getString("Airline_Name");
                        int seats = rs.getInt("Available_Seats");
                        float price = rs.getFloat("price");

                        Flight flight = new Flight(id, date, time, source, destination, airlineName, seats, price);
                        FlightManagementSystem.flights.add(flight);
                    }
                }

                // Read Booking data for the current user
                String bookingQuery = "SELECT bookingid, passengerid, flightid, seattype FROM Booking WHERE passengerid = ?";
                try (PreparedStatement stmt = conn.prepareStatement(bookingQuery)) {
                    stmt.setInt(1, FlightManagementSystem.user.ID);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            int bookingId = rs.getInt("bookingid");
                            int passengerId = rs.getInt("passengerid");
                            int flightId = rs.getInt("flightid");
                            String seattype = rs.getString("seattype");
                            Booking booking = new Booking(bookingId, passengerId, flightId, seattype);
                            FlightManagementSystem.bookings.add(booking);
                        }
                    }
                }

                // Read Boarding Pass data for the current user
                String boardingPassQuery = "SELECT id, passengerID, flightID FROM BoardingPass WHERE passengerID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(boardingPassQuery)) {
                    stmt.setInt(1, FlightManagementSystem.user.ID);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            int passID = rs.getInt("id");
                            int passengerID = rs.getInt("passengerID");
                            int flightID = rs.getInt("flightID");

                            Booking associatedBooking = FlightManagementSystem.bookings.stream()
                                .filter(booking -> booking.getFlightID() == flightID && booking.getPassengerID() == passengerID)
                                .findFirst()
                                .orElse(null);

                            if (associatedBooking != null) {
                                String seatType = associatedBooking.seattype;
                                BoardingPass pass;

                                switch (seatType) {
                                    case "First Class":
                                        pass = new FirstclassPass();
                                        break;
                                    case "Business Class":
                                        pass = new BusinessPass();
                                        break;
                                    case "Economy Class":
                                        pass = new EconomyPass();
                                        break;
                                    default:
                                        System.out.println("Unknown seat type: " + seatType);
                                        continue;
                                }

                                pass.setPassID(passID);
                                pass.setPassengerID(passengerID);
                                pass.setFlightID(flightID);
                                FlightManagementSystem.boardingpasses.add(pass);
                            }
                        }
                    }
                }

                // Read User data (exclude type = 2)
                String userQuery = "SELECT userid, username, usertype FROM Users WHERE usertype != 2";
                try (PreparedStatement stmt = conn.prepareStatement(userQuery);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("userid");
                        String username = rs.getString("username");
                        int userType = rs.getInt("usertype");

                        User user = new User(id, userType, username);
                        FlightManagementSystem.users.add(user);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void readAdminData() {
            // Initialize array lists
            FlightManagementSystem.airlines = new ArrayList<>();
            FlightManagementSystem.flights = new ArrayList<>();
            FlightManagementSystem.bookings = new ArrayList<>();
            FlightManagementSystem.boardingpasses = new ArrayList<>();
            FlightManagementSystem.users = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection(url)) {
                // Read Airline data
                String airlineQuery = "SELECT airlineid, airlinename FROM Airline";
                try (PreparedStatement stmt = conn.prepareStatement(airlineQuery);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("airlineid");
                        String name = rs.getString("airlinename");
                        Airline airline = new Airline(id, name);
                        FlightManagementSystem.airlines.add(airline);
                    }
                }

                // Read Flight data
                String flightQuery = "SELECT Flight_ID, date, time, Source_Location, Destination_Location, Airline_Name, Available_Seats, price FROM Flight";
                try (PreparedStatement stmt = conn.prepareStatement(flightQuery);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("Flight_ID");
                        String date = rs.getString("date");
                        String time = rs.getString("time");
                        String source = rs.getString("Source_Location");
                        String destination = rs.getString("Destination_Location");
                        String airlineName = rs.getString("Airline_Name");
                        int seats = rs.getInt("Available_Seats");
                        float price = rs.getFloat("price");

                        Flight flight = new Flight(id, date, time, source, destination, airlineName, seats, price);
                        FlightManagementSystem.flights.add(flight);
                    }
                }

                // Read all Booking data (no user filtering)
                String bookingQuery = "SELECT bookingid, passengerid, flightid, seattype FROM Booking";
                try (PreparedStatement stmt = conn.prepareStatement(bookingQuery);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int bookingId = rs.getInt("bookingid");
                        int passengerId = rs.getInt("passengerid");
                        int flightId = rs.getInt("flightid");
                        String seattype = rs.getString("seattype");
                        Booking booking = new Booking(bookingId, passengerId, flightId, seattype);
                        FlightManagementSystem.bookings.add(booking);
                    }
                }

                // Read all Boarding Pass data (no user filtering)
                String boardingPassQuery = "SELECT id, passengerID, flightID FROM BoardingPass";
                try (PreparedStatement stmt = conn.prepareStatement(boardingPassQuery);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int passID = rs.getInt("id");
                        int passengerID = rs.getInt("passengerID");
                        int flightID = rs.getInt("flightID");

                        Booking associatedBooking = FlightManagementSystem.bookings.stream()
                                .filter(booking -> booking.getFlightID() == flightID && booking.getPassengerID() == passengerID)
                                .findFirst()
                                .orElse(null);

                        if (associatedBooking != null) {
                            String seatType = associatedBooking.seattype;
                            BoardingPass pass;

                            switch (seatType) {
                                case "First Class":
                                    pass = new FirstclassPass();
                                    break;
                                case "Business Class":
                                    pass = new BusinessPass();
                                    break;
                                case "Economy Class":
                                    pass = new EconomyPass();
                                    break;
                                default:
                                    System.out.println("Unknown seat type: " + seatType);
                                    continue;
                            }

                            pass.setPassID(passID);
                            pass.setPassengerID(passengerID);
                            pass.setFlightID(flightID);
                            FlightManagementSystem.boardingpasses.add(pass);
                        }
                    }
                }
             // Read User data (exclude usertype = 2)
                String userQuery = "SELECT userid, username, usertype FROM Users WHERE usertype != 2";
                try (PreparedStatement stmt = conn.prepareStatement(userQuery);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("userid");
                        String username = rs.getString("username");
                        int userType = rs.getInt("usertype");

                        User user = new User(id, userType, username);
                        FlightManagementSystem.users.add(user);
                    }
                }

            } 
            catch (SQLException e) {
                e.printStackTrace();
                
            }
        }

        public boolean createPass(int flightid) {
            // Query to get the maximum ID and insert the new boarding pass
            String getMaxIdQuery = "SELECT COALESCE(MAX(id), 0) AS maxId FROM BoardingPass";
            String insertPassQuery = "INSERT INTO BoardingPass (id, passengerID, flightID) VALUES (?, ?, ?)";
            String checkExistingPassQuery = "SELECT COUNT(*) FROM BoardingPass WHERE passengerID = ? AND flightID = ?";

            try (Connection conn = DriverManager.getConnection(url)) {
                // Step 1: Check if a boarding pass already exists for this passenger and flight
                try (PreparedStatement checkStmt = conn.prepareStatement(checkExistingPassQuery)) {
                    checkStmt.setInt(1, FlightManagementSystem.user.ID); // Set passenger ID
                    checkStmt.setInt(2, flightid); // Set flight ID

                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            // A boarding pass already exists for this passenger and flight, return false
                            System.out.println("Boarding pass already exists for this passenger and flight.");
                            return false;
                        }
                    }
                }

                // Step 2: Get the maximum ID for the new boarding pass
                int newId = 1; // Default ID
                try (PreparedStatement maxIdStmt = conn.prepareStatement(getMaxIdQuery)) {
                    try (ResultSet rs = maxIdStmt.executeQuery()) {
                        if (rs.next()) {
                            newId = rs.getInt("maxId") + 1; // Increment max ID
                        }
                    }
                }

                // Step 3: Insert the new boarding pass into the database
                try (PreparedStatement insertPassStmt = conn.prepareStatement(insertPassQuery)) {
                    insertPassStmt.setInt(1, newId); // Set new ID
                    insertPassStmt.setInt(2, FlightManagementSystem.user.ID); // Set passenger ID (user ID)
                    insertPassStmt.setInt(3, flightid); // Set flight ID

                    int rowsAffected = insertPassStmt.executeUpdate();

                    if (rowsAffected > 0) {
                        // Step 4: Determine the seat type from the booking
                        String seatType = null;
                        for (Booking booking : FlightManagementSystem.bookings) {
                            if (booking.getFlightID() == flightid && booking.getPassengerID() == FlightManagementSystem.user.ID) {
                                seatType = booking.seattype; // Retrieve the seat type
                                break;
                            }
                        }

                        // Step 5: Create the appropriate subclass of BoardingPass
                        BoardingPass newPass;
                        switch (seatType) {
                            case "First Class":
                                newPass = new FirstclassPass();
                                break;
                            case "Business Class":
                                newPass = new BusinessPass();
                                break;
                            case "Economy Class":
                                newPass = new EconomyPass();
                                break;
                            default:
                                throw new IllegalArgumentException("Unknown seat type: " + seatType);
                        }

                        // Set the common properties for the boarding pass
                        newPass.setPassID(newId); // Assign ID
                        newPass.setPassengerID(FlightManagementSystem.user.ID); // Set passenger ID (user ID)
                        newPass.setFlightID(flightid); // Set flight ID

                        // Add the new BoardingPass to the ArrayList
                        FlightManagementSystem.boardingpasses.add(newPass);

                        return true; // Return true if the boarding pass was successfully created
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Log the error for debugging
            }

            return false; // Return false if an error occurred or the boarding pass already exists
        }


        
        
    }
    
  
    
    // Manages the flight management system by initializing controllers as needed
    public static class FlightManagementSystem {
    	static User user;
    	static ArrayList<User> users ;
        static ArrayList<Flight> flights;
        static ArrayList<Airline> airlines;
        static ArrayList<Booking> bookings;
        static ArrayList<BoardingPass> boardingpasses;
        
        
        static DB database;

    	
    	//= new ArrayList<>();
        public FlightManagementSystem(Parent root, Stage stage) {
            database = new DB();
            new LoginController(root, stage);  // Initializing LoginController for login view
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("loginsignup.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Flight Management System");
        primaryStage.setScene(new Scene(root, 700, 500));
        primaryStage.show();

        new FlightManagementSystem(root, primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
