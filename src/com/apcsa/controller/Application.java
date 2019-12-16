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
	
	//Root Selection Constants
	
	public static final int ROOT_RESET_PASSWORD = 1;
	public static final int ROOT_FACTORY_RESET = 2;
	public static final int ROOT_LOGOUT = 3;
	public static final int ROOT_SHUTDOWN = 4;
	
	//Administrator Selection Constants
	
	public static final int ADMIN_FACULTY = 1;
	public static final int ADMIN_FACULTY_DEPARTMENT = 2;
	public static final int ADMIN_ENROLLMENT = 3;
	public static final int ADMIN_ENROLLMENT_GRADE = 4;
	public static final int ADMIN_ENROLLMENT_COURSE = 5;
	public static final int ADMIN_CHANGE_PASSWORD = 6;
	public static final int ADMIN_LOGOUT = 7;
	
	//Teacher Selection Constants
	
	public static final int TEACHER_ENROLLMENT = 1;
	public static final int TEACHER_ADD_ASSIGNMENT = 2;
	public static final int TEACHER_DELETE_ASSIGNMENT = 3;
	public static final int TEACHER_ENTER_GRADE = 4;
	public static final int TEACHER_CHANGE_PASSWORD = 5;
	public static final int TEACHER_LOGOUT = 6;
	
	//Student Selection Constants
	
	public static final int STUDENT_VIEW_COURSE_GRADES = 1;
	public static final int STUDENT_VIEW_ASSIGNMENT_GRADES = 2;
	public static final int STUDENT_CHANGE_PASSWORD = 3;
	public static final int STUDENT_LOGOUT = 4;
	
	//End Selection Constants

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
         
                    PowerSchool.changePassword(username, newPassword);
                    
                }

                // create and show the user interface
                //
                // remember, the interface will be difference depending on the type
                // of user that is logged in (root, administrator, teacher, student)
                
                if (activeUser.isRoot()) {
                	boolean validLogin = true;
                	
                	while (validLogin) {
                        switch (getSelectionRoot()) {
                        
                            case ROOT_RESET_PASSWORD: System.out.print("\nroot change password\n"); break;
                            case ROOT_FACTORY_RESET: System.out.print("\nroot reset database\n"); /*PowerSchool.reset();*/ break;
                            case ROOT_SHUTDOWN: System.out.println("\nroot shutdown\n");/*rootShutdown()*/; break;
                            case ROOT_LOGOUT: System.out.print("\nlogout\n")/*validLogin = logoutConfirm(); in.nextLine()*/; break;
                            default: System.out.print("\nInvalid selection.\n"); break;
                            
                        }
                	}
                	
                } else if (activeUser.isAdministrator()) {
                	boolean validLogin = true;
                	
                	while (validLogin) {
                        switch (getselectionAdministrator()) {
                         
	                        case ADMIN_FACULTY: System.out.print("\nview by faculty\n"); break;
	                        case ADMIN_FACULTY_DEPARTMENT: System.out.print("\nview by dept\n"); break;
	                        case ADMIN_ENROLLMENT: System.out.print("\nview enrollment\n"); break;
	                        case ADMIN_ENROLLMENT_GRADE: System.out.print("\nview by grade\n"); break;
	                        case ADMIN_ENROLLMENT_COURSE: System.out.print("\nview by course\n"); break;
	                        case ADMIN_CHANGE_PASSWORD: System.out.print("\nadmin change password\n"); break;
	                        case ADMIN_LOGOUT: System.out.print("\nlogout\n")/*validLogin = logoutConfirm(); in.nextLine()*/; break;
	                        default: System.out.print("\nInvalid selection.\n"); break;
	                        
                        }
                	}
                	
                } else if (activeUser.isTeacher()) {
                	boolean validLogin = true;
                	
                	while (validLogin) {
                        switch (getSelectionTeacher()) {
                            
	                        case TEACHER_ENROLLMENT: System.out.print("\nview enrollment by course\n"); break;
	                        case TEACHER_ADD_ASSIGNMENT: System.out.print("\nadd assignment\n"); break;
	                        case TEACHER_DELETE_ASSIGNMENT: System.out.print("\ndelete assignment\n"); break;
	                        case TEACHER_ENTER_GRADE: System.out.print("\nenter grade\n"); break;
	                        case TEACHER_CHANGE_PASSWORD: System.out.print("\nteacher change password\n"); break;
	                        case TEACHER_LOGOUT: System.out.print("\nlogout\n")/*validLogin = logoutConfirm(); in.nextLine()*/; break;
	                        default: System.out.print("\nInvalid selection.\n"); break;
                        
                        }
                	}
                	
                } else if (activeUser.isStudent()) {
                	boolean validLogin = true;
                	
                	while (validLogin) {
                        switch (getSelectionStudent()) {
                            
	                        case STUDENT_VIEW_COURSE_GRADES: System.out.print("\nview course grades\n"); break;
	                        case STUDENT_VIEW_ASSIGNMENT_GRADES: System.out.print("\nview asgn grades by course\n"); break;
	                        case STUDENT_CHANGE_PASSWORD: System.out.print("\nstudent change password\n"); break;
	                        case STUDENT_LOGOUT: System.out.print("\nlogout\n")/*validLogin = logoutConfirm(); in.nextLine()*/; break;
	                        default: System.out.print("\nInvalid selection.\n"); break;
                        
                        }
                	}
                	
                }
                
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
    
    public int changePassword() {
    	System.out.print("Enter current password: ");
    	String checkPassword = in.next();
    	if (checkPassword.equals("bruh")) {
    		//shit
    	}
    	
    	return 1;
    	
    }
    
    public int getSelectionStudent() {
    	System.out.println();
    	System.out.println("[1] View course grades.");
    	System.out.println("[2] View assignment grades by course.");
    	System.out.println("[3] Change password.");
    	System.out.println("[4] Logout.");
    	
    	return in.nextInt();

    }
    
    
    public int getSelectionTeacher() {
    	System.out.println();
    	System.out.println("[1] View enrollment by course.");
    	System.out.println("[2] Add assignment.");
    	System.out.println("[3] Delete assignment.");
    	System.out.println("[4] Enter grade.");
    	System.out.println("[5] Change password.");
    	System.out.println("[6] Logout.");
    	
    	return in.nextInt();

    }
    
    public int getselectionAdministrator() {
    	System.out.println();
    	System.out.println("[1] View faculty.");
    	System.out.println("[2] View faculty by department.");
    	System.out.println("[3] View student enrollment.");
    	System.out.println("[4] View student enrollment by grade.");
    	System.out.println("[5] View student enrollment by course.");
    	System.out.println("[6] Change password.");
    	System.out.println("[7] Logout.");

    	return in.nextInt();
    	
    }
    
    public int getSelectionRoot() {
    	System.out.println();
    	System.out.println("[1] Reset user password.");
    	System.out.println("[2] Factory reset database.");
    	System.out.println("[3] Logout.");
    	System.out.println("[4] Shutdown.");
    	
    	return in.nextInt();

    }
}