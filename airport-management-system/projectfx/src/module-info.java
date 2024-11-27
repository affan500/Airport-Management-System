module projectfx {
	requires javafx.controls;
	requires java.sql;  
	    requires javafx.fxml;
	    requires java.base;

	    exports application;
	
	opens application to javafx.graphics, javafx.fxml;
}
