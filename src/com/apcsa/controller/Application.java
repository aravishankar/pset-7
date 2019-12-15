package com.apcsa.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Scanner;
import com.apcsa.data.PowerSchool;
import com.apcsa.data.QueryUtils;
import com.apcsa.model.User;

public class Application {

    private Scanner in;
    private User activeUser;

    /**
     * Creates an instance of the Application class, which is responsible for interacting
     * with the user via the command line interface.
     */

    public Application() {
        this.in = new Scanner(System.in);

        try {
            PowerSchool.initialize(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the PowerSchool application.
     */

    public void startup() {
        System.out.println("PowerSchool -- now for students, teachers, and school administrators!");

        // continuously prompt for login credentials and attempt to login

        while (true) {
            System.out.print("\nUsername: ");
            String username = in.next();

            System.out.print("Password: ");
            String password = in.next();

            // if login is successful, update generic user to administrator, teacher, or student

            if (login(username, password)) {
                activeUser = activeUser.isAdministrator()
                    ? PowerSchool.getAdministrator(activeUser) : activeUser.isTeacher()
                    ? PowerSchool.getTeacher(activeUser) : activeUser.isStudent()
                    ? PowerSchool.getStudent(activeUser) : activeUser.isRoot()
                    ? activeUser : null;
                    
                if (isFirstLogin()) {
                	System.out.println("first login");
                }
                    
                if (isFirstLogin() && !activeUser.isRoot()) {
                    // first-time users need to change their passwords from the default provided
                	System.out.print("Enter new password: ");
                    String newPassword = in.next();
         
                    PowerSchool.createPassword(username, newPassword);
                    
                }

                // create and show the user interface
                //
                // remember, the interface will be difference depending on the type
                // of user that is logged in (root, administrator, teacher, student)
                
                System.out.println("I am logged in");
                System.out.println(username);
                System.out.println(password);
                
            } else {
                System.out.println("\nInvalid username and/or password.");
            }
        }
    }

    /**
     * Logs in with the provided credentials.
     *
     * @param username the username for the requested account
     * @param password the password for the requested account
     * @return true if the credentials were valid; false otherwise
     */

    public boolean login(String username, String password) {
        activeUser = PowerSchool.login(username, password);

        return activeUser != null;
    }

    /**
     * Determines whether or not the user has logged in before.
     *
     * @return true if the user has never logged in; false otherwise
     */

    public boolean isFirstLogin() {
        return activeUser.getLastLogin().equals("0000-00-00 00:00:00.000");
    }

    /////// MAIN METHOD ///////////////////////////////////////////////////////////////////

    /*
     * Starts the PowerSchool application.
     *
     * @param args unused command line argument list
     */

    public static void main(String[] args) {
        Application app = new Application();

        app.startup();
    }
    
    public int getSelectionStudent() {
    	System.out.println("[1] View course grades.");
    	System.out.println("[2] View assignment grades by course.");
    	System.out.println("[3] Change password.");
    	System.out.println("[4] Logout.");
    	
    	return in.next();

    }
    
    
    public int getSelectionTeacher() {
    	System.out.println("[1] View enrollment by course.");
    	System.out.println("[2] Add assignment.");
    	System.out.println("[3] Delete assignment.");
    	System.out.println("[4] Enter grade.");
    	System.out.println("[5] Change password.");
    	System.out.println("[6] Logout.");
    	
    	return in.next();

    }
    
    public int getselectionAdministrator() {
    	System.out.println("[1] View faculty.");
    	System.out.println("[2] View faculty by department.");
    	System.out.println("[3] View student enrollment.");
    	System.out.println("[4] View student enrollment by grade.");
    	System.out.println("[5] View student enrollment by course.");
    	System.out.println("[6] Change password.");
    	System.out.println("[7] Logout.");

    	return in.next();
    	
    }
    
    public int getSelectionRoot() {
    	System.out.println("[1] Reset user password.");
    	System.out.println("[2] Factory reset database.");
    	System.out.println("[3] Logout.");
    	System.out.println("[4] Shutdown.");
    	
    	return in.next();

    }
}