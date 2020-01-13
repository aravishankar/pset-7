package com.apcsa.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import com.apcsa.data.PowerSchool;
import com.apcsa.model.Student;
import com.apcsa.model.Teacher;
import com.apcsa.model.User;

public class Application {

    private Scanner in;
    private User activeUser;
    
    enum RootAction { PASSWORD, DATABASE, LOGOUT, SHUTDOWN }
    enum AdministratorAction { FACULTY, DEPARTMENT, STUDENTS, GRADE, COURSE, PASSWORD, LOGOUT }
    enum TeacherAction { ENROLLMENT, ADD, DELETE, GRADE, PASSWORD, LOGOUT }
    enum StudentAction { COURSE, ASSIGNMENT, PASSWORD, LOGOUT }

    
    /**
     * Creates an instance of the Application class, which is responsible for interacting with the
     * user via the command line interface.
     */

    public Application() {
        this.in = new Scanner(System.in);

        try {
            PowerSchool.initialize(false);
        } catch (Exception e) {
            shutdown(e);
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

            try {
                if (login(username, password)) {
                    activeUser = activeUser.isAdministrator()
                        ? PowerSchool.getAdministrator(activeUser) : activeUser.isTeacher()
                        ? PowerSchool.getTeacher(activeUser) : activeUser.isStudent()
                        ? PowerSchool.getStudent(activeUser) : activeUser.isRoot()
                        ? activeUser : null;
    
                    if (isFirstLogin() && !activeUser.isRoot()) {
                        // first-time users need to change their passwords from the default provided
                    	System.out.print("\nEnter new password: ");
                        String newPassword = in.next();
                        PowerSchool.changePassword(username, newPassword);
                        System.out.println("\nSuccessfully changed password.");

                    }
    
                    createAndShowUI();
                } else {
                    System.out.println("\nInvalid username and/or password.");
                }
            } catch (Exception e) {
                shutdown(e);
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
    
    /**
     * Displays an user type-specific menu with which the user
     * navigates and interacts with the application.
     */
    
    public void createAndShowUI() {
        System.out.println("\nHello, again, " + activeUser.getFirstName() + "!");

        if (activeUser.isRoot()) {
            showRootUI();
        } else if (activeUser.isAdministrator()) {
            showAdministratorUI();
        } else if (activeUser.isTeacher()) {
            showTeacherUI();
        } else if (activeUser.isStudent()) {
            showStudentUI();
        } else {
            // TODO - add cases for teacher, student, and unknown
        }
    }
    
    /////// ROOT METHODS //////////////////////////////////////////////////////////////////
    
    /*
     * Displays an interface for root users.
     */
    
    private void showRootUI() {
        while (activeUser != null) {
            switch (getRootMenuSelection()) {
                case PASSWORD: resetPassword(); break;
                case DATABASE: factoryReset(); break;
                case LOGOUT: logout(); break;
                case SHUTDOWN: shutdown(); break;
                default: System.out.println("\nInvalid selection."); break;
            }
        }
    }
    
    /*
     * Retrieves a root user's menu selection.
     * 
     * @return the menu selection
     */
    
    private RootAction getRootMenuSelection() {
        System.out.println();
        
        System.out.println("[1] Reset user password.");
        System.out.println("[2] Factory reset database.");
        System.out.println("[3] Logout.");
        System.out.println("[4] Shutdown.");
        System.out.print("\n::: ");
        
        switch (Utils.getInt(in, -1)) {
            case 1: return RootAction.PASSWORD;
            case 2: return RootAction.DATABASE;
            case 3: return RootAction.LOGOUT;
            case 4: return RootAction.SHUTDOWN;
            default: return null;
        }
    }
    
    /*
     * Allows a root user to reset another user's password.
     */
    
    private void resetPassword() {
        //
        // prompt root user to enter username of user whose password needs to be reset
        //
        // ask root user to confirm intent to reset the password for that username
        //
        // if confirmed...
        //      call database method to reset password for username
        //      print success message
        //
    	
    	System.out.print("\nUsername: ");
        String username = in.next();
        
        if (Utils.confirm(in, "\nAre you sure you want to reset the password for " + username + "?  (y/n) ")) {
            if (in != null) {
                if (PowerSchool.resetPassword(username)) {
                    PowerSchool.resetLastLogin(username);
                    System.out.println("\nSuccessfully reset password for " + username + ".");
                } else {
                    System.out.println("\nPassword reset failed");
                }
            }
        }
    	
    }
    
    /*
     * Resets the database to its factory settings.
     */
    
    private void factoryReset() {
        //
        // ask root user to confirm intent to reset the database
        //
        // if confirmed...
        //      call database initialize method with parameter of true
        //      print success message
        //
    	
    	if (Utils.confirm(in, "\nAre you sure you want to reset all settings and data? (y/n) ")) {
    		try {
   	            PowerSchool.initialize(true);
   	            System.out.println("\nSuccessfully reset database.");
   	        } catch (Exception e) {
   	            e.printStackTrace();
   	        }
    	}
    	
    }
    
    /////// ADMINISTRATOR METHODS /////////////////////////////////////////////////////////
    
    /*
     * Displays an interface for admin users.
     */
    
    private void showAdministratorUI() {
        while (activeUser != null) {
            switch (getAdministratorMenuSelection()) {
                case FACULTY: viewFaculty(); break;
                case DEPARTMENT: viewFacultyByDepartment(); break;
                case STUDENTS: viewStudents(); break;
                case GRADE: viewStudentsByGrade(); break;
                case COURSE: viewStudentsByCourse(); break;
                case PASSWORD: changePassword(false); break;
                case LOGOUT: logout(); break;
                default: System.out.println("\nInvalid selection."); break;
            }
        }
    }
    
    /*
     * Retrieves an administrator user's menu selection.
     * 
     * @return the menu selection
     */
    
    private AdministratorAction getAdministratorMenuSelection() {
        System.out.println();
        
        System.out.println("[1] View faculty.");
        System.out.println("[2] View faculty by department.");
        System.out.println("[3] View student enrollment.");
        System.out.println("[4] View student enrollment by grade.");
        System.out.println("[5] View student enrollment by course.");
        System.out.println("[6] Change password.");
        System.out.println("[7] Logout.");
        System.out.print("\n::: ");
        
        switch (Utils.getInt(in, -1)) {
            case 1: return AdministratorAction.FACULTY;
            case 2: return AdministratorAction.DEPARTMENT;
            case 3: return AdministratorAction.STUDENTS;
            case 4: return AdministratorAction.GRADE;
            case 5: return AdministratorAction.COURSE;
            case 6: return AdministratorAction.PASSWORD;
            case 7: return AdministratorAction.LOGOUT;
        }
        
        return null;
    }
    
    /*
     * Displays all faculty members.
     */
    
    private void viewFaculty() {        
        ArrayList<Teacher> teachers = PowerSchool.getTeachers();
        
        if (teachers.isEmpty()) {
            System.out.println("\nNo teachers to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Teacher teacher : teachers) {
                System.out.println(i++ + ". " + teacher.getName() + " / " + teacher.getDepartmentName());
            }
        }
    }
    
    /*
     * Displays all faculty members by department.
     */
    
    private void viewFacultyByDepartment() {
        //
        // get a list of teachers by department (this requires a database call)
        //      to do this, you'll need to prompt the user to choose a department (more on this later)
        //
        // if list of teachers is empty...
        //      print a message saying exactly that
        // otherwise...
        //      print the list of teachers by name an department (just like last time)
        //
    	
    	int selection = getDepartmentSelection();
    	
    	ArrayList<Teacher> teachers = PowerSchool.getTeachersByDepartment(selection);
        
        if (teachers.isEmpty()) {
            System.out.println("\nNo teachers to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Teacher teacher : teachers) {
                System.out.println(i++ + ". " + teacher.getName() + " / " + teacher.getDepartmentName());
            }
        }
    	
    }
    
    /*
     * Displays all students.
     */
    
    private void viewStudents() {
        //
        // get a list of students
        //
        // if list of students is empty...
        //      print a message saying exactly that
        // otherwise...
        //      print the list of students by name and graduation year
        //
    	
    	ArrayList<Student> students = PowerSchool.getStudents();
        
        if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName() + " / " + student.getGraduationYear());
            } 
        }
    }
    
    /*
     * Displays all students by grade.
     */
    
    private void viewStudentsByGrade() {
        //
        // get list of students by grade
        //      to do this, you'll need to prompt the user to choose a grade level (more on this later)
        //
        // if the list of students is empty...
        //      print a message saying exactly that
        // otherwise...
        //      print the list of students by name and class rank
        //
    	
    	ArrayList<Student> students = Utils.updateRanks(PowerSchool.getStudentsByGrade(getGradeSelection()));
    	
    	if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName() + " / " + "#" + student.getClassRank());
            } 
        }
    	
    }
    
    /*
     * Displays all students by course.
     */
    
    private void viewStudentsByCourse() {
        //
        // get a list of students by course
        //      to do this, you'll need to prompt the user to choose a course (more on this later)
        //
        // if the list of students is empty...
        //      print a message saying exactly that
        // otherwise...
        //      print the list of students by name and grade point average
        //
    	
    	String courseNo = "";
    	
		try {
			courseNo = getCourseSelection();
		} catch(SQLException e) {
		}
		
		ArrayList<Student> students = PowerSchool.getStudentsByCourse(courseNo);
    	
    	if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName() + " / " + standardizeGPA(student));
            } 
            
            
        }
    	
    }
    
    private String standardizeGPA(Student student) {
		double GPA = student.getGpa();
		if(GPA == -1) {
			return "--";
		}else {
			return String.valueOf(GPA);
		}
	}
    
    /////// TEACHER METHODS /////////////////////////////////////////////////////////

    /*
     * Displays an interface for admin users.
     */
    
    private void showTeacherUI() {
        while (activeUser != null) {
            switch (getTeacherMenuSelection()) {
                case ENROLLMENT: viewEnrollmentByCourse(); break;
                case ADD: addAssignment(); break;
                case DELETE: deleteAssignment(); break;
                case GRADE: enterGrade(); break;
                case PASSWORD: changePassword(false); break;
                case LOGOUT: logout(); break;
                default: System.out.println("\nInvalid selection."); break;
            }
        }
    }
    
    /*
     * Retrieves an administrator user's menu selection.
     * 
     * @return the menu selection
     */
    
    private TeacherAction getTeacherMenuSelection() {
        System.out.println();
        
        System.out.println("[1] View enrollment by course.");
        System.out.println("[2] Add assignment.");
        System.out.println("[3] Delete assignment.");
        System.out.println("[4] Enter grade.");
        System.out.println("[5] Change password.");
        System.out.println("[6] Logout.");
        System.out.print("\n::: ");
        
        switch (Utils.getInt(in, -1)) {
            case 1: return TeacherAction.ENROLLMENT;
            case 2: return TeacherAction.ADD;
            case 3: return TeacherAction.DELETE;
            case 4: return TeacherAction.GRADE;
            case 5: return TeacherAction.PASSWORD;
            case 6: return TeacherAction.LOGOUT;
        }
        
        return null;
    }
    
    private void viewEnrollmentByCourse() {
    	String courseNumber = getCourseSelectionTeacher();
		ArrayList<Student> students = PowerSchool.getStudentsByCourse(courseNumber);
    	
    	if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName() + " / " + standardizeGPA(student));
            } 
        }
		
	}
    
    public void addAssignment() {
    	
    	System.out.println("\nChoose a course.\n");
    	
    	int departmentId = ((Teacher) activeUser).getDepartmentId();
    	
    	ArrayList<String> courses = PowerSchool.getCourses(departmentId);
    	
    	for (int i = 0; i <= courses.size()-1; i++) {
    		System.out.println("[" + (i + 1) + "] " + courses.get(i));
    		
    	}
    	
    	System.out.print("\n::: ");
    	int courseSelection = in.nextInt();
    	
    	while (courseSelection > courses.size() || courseSelection < 1) {
    		
    		System.out.println("\nInvalid selection.");
    		System.out.println("\nChoose a course.\n");
    		
        	for (int i = 0; i <= courses.size()-1; i++) {
        		System.out.println("[" + (i + 1) + "] " + courses.get(i));
        		
        	}
        	
        	System.out.print("\n::: ");
        	courseSelection = in.nextInt();
    	}
    	
    	String courseNo = courses.get(courseSelection-1);
    	int courseId = PowerSchool.getCourseIdFromCourseNo(courseNo);
    	
    	printMarkingPeriods();
    	int markingPeriod = in.nextInt();
    	
    	while (markingPeriod > 6 || markingPeriod < 1) {
    		
    		System.out.println("\nInvalid selection.");
    		printMarkingPeriods();
        	markingPeriod = in.nextInt();
    	}
    	
    	int isMidterm = 0;
    	int isFinal = 0;
    	
    	if (markingPeriod == 5) {
    		isMidterm = 1;
    		
    	} else if (markingPeriod == 6) {
    		
    		isFinal = 1;
    	}
    	
    	in.nextLine();
    	System.out.print("\nAssignment Title: ");
    	String title = in.nextLine();
    	
    	System.out.print("Point Value: ");
		int pointValue = in.nextInt();
		
		while (pointValue > 100 || pointValue < 1) {
			
			System.out.println("Point values must be between 1 and 100.");
			System.out.print("Point Value: ");
			pointValue = in.nextInt();
			
		}
		
    	int assignmentId = 0;
    	in.nextLine();
    	String wantTo = "you want to create this assignment?";
    	System.out.print("Are you sure you want to create this assignment? (y/n) ");
    	String yesNo = in.nextLine();
    	
    	yesNo = yesNo.toLowerCase();
    	int checked = checkYesNo(yesNo, wantTo);
    	
    	if(checked == -1) {
    		
			System.out.println("");
			
    	} else if (checked == 1) {
    		
    		int rows = PowerSchool.assignmentRows();
    		if(rows == 0) {
    			
    			assignmentId = 1;
    			
    		} else {
    			
    			ArrayList<String> assignmentIds = PowerSchool.acquireAssignmentIds();
    			String lastAssignmentId = assignmentIds.get(assignmentIds.size() - 1);
    			assignmentId = Integer.parseInt(lastAssignmentId) + 1;
    		}
    		
    		PowerSchool.addAssignment(courseId, assignmentId, markingPeriod, isMidterm, isFinal, title, pointValue);

    		
    		System.out.println("\nSuccessfully created assignment.\n");
    	}
    }

	private int getCourseId() {
		String courseNumber = getCourseSelectionTeacher();
		return PowerSchool.getCourseId(courseNumber);
	} 

//	private int getAssignmentId() {		
//		if (PowerSchool.getNumberOfAssignemnts() == 0) {
//			return 1;
//		} else {
//			return PowerSchool.getlastAssignmentId() + 1;
//		}
//	}
    
	private void deleteAssignment() {
		int courseId = getCourseId();
		System.out.println("\nChoose a marking period or exam status.\n");
		System.out.println("[1] MP1 assignment.");
        System.out.println("[2] MP2 assignment.");
        System.out.println("[3] MP3 assignment.");
        System.out.println("[4] MP4 assignment.");
        System.out.println("[5] Midterm exam.");
        System.out.println("[6] Final exam.");
        System.out.print("\n::: ");
        int markingPeriod = Utils.getInt(in, -1);
        while(markingPeriod <= 0 || markingPeriod > 6) {
        	if(markingPeriod <= 0 || markingPeriod > 6) {
            	System.out.println("\nInvalid Selection.");
       	 	}
        	System.out.println("\nChoose a marking period or exam status.\n");
    		System.out.println("[1] MP1 assignment.");
            System.out.println("[2] MP2 assignment.");
            System.out.println("[3] MP3 assignment.");
            System.out.println("[4] MP4 assignment.");
            System.out.println("[5] Midterm exam.");
            System.out.println("[6] Final exam.");
            System.out.print("\n::: ");
            markingPeriod = Utils.getInt(in, -1);                 
        }
		 ArrayList<String> assignments = PowerSchool.getAssignments(courseId, markingPeriod);
		 ArrayList<String> pointValues = PowerSchool.getPointValues(courseId, markingPeriod);
		 
		 System.out.println();
		 if(!assignments.isEmpty()) {
			 int assignmentSelection = -1;
		        while(assignmentSelection <= 0 || assignmentSelection > assignments.size()) {
		       	 int j = 1;
		            for (String i: assignments) {
		                System.out.println("["+ j++ + "] " + i + " (" + pointValues.get(j-2) + " pts)");
		            }
		       	 System.out.print("\n::: ");
		       	assignmentSelection = Utils.getInt(in, -1);
		       	 if(assignmentSelection <= 0 || assignmentSelection > assignments.size()) {
		       		 System.out.println("\nInvalid Selection.\n");
		       	 }
		        }
		        String title = assignments.get(assignmentSelection-1);
		        int assignemntId = PowerSchool.getAssignmentId(courseId, markingPeriod, title);
		        if(Utils.confirm(in, "\nAre you sure you want to create this assignment? (y/n) ")) {
		        	if(PowerSchool.deleteAssignment(courseId, markingPeriod, title) == 1 && PowerSchool.deleteAssignmentGrades(assignemntId, courseId) ==1 ) {
		        		System.out.println("\nSuccessfully deleted " + title + ".");		        		
		        	}else {
		        		System.out.println("\nError deleting assignment.");
		        	}
		        }
		 }else {
			 System.out.println("No assignments.");
		 }
        
	}
    
	public void enterGrade() {
    	boolean hasAssignment = true;    	
   		System.out.println("\nChoose a course.\n");
       	int departmentId = ((Teacher) activeUser).getDepartmentId(); //department id of the teacher
       	ArrayList<String> courses = PowerSchool.getCourses(departmentId); //all courses
       	for (int i = 0; i <= courses.size()-1; i++) {
       		System.out.println("[" + (i + 1) + "] " + courses.get(i)); //printing out courses
       	}
       	System.out.print("\n::: ");
       	int courseSelection = in.nextInt();
       	
       	if(courseSelection < 1 || courseSelection > courses.size()) {
       		while(courseSelection < 1 || courseSelection > courses.size()){
       			System.out.println("\nInvalid selection.");
       			System.out.println("\nChoose a course.\n");
       			for(int i = 0; i <= courses.size()-1; i++) {
       	       		System.out.println("[" + (i + 1) + "] " + courses.get(i)); //printing out courses
       	       	}
       	     System.out.print("\n::: ");
       	     courseSelection = in.nextInt();
       		}
       	}
        String courseNo = courses.get(courseSelection-1); 
       	int courseId = PowerSchool.getCourseIdFromCourseNo(courseNo); //courseId
       	printMarkingPeriods();
       	int markingPeriod = in.nextInt();//Selected Marking Period
       	if(markingPeriod < 1 || markingPeriod > 6) {
       		while(markingPeriod < 1 || markingPeriod > 6) {
       			System.out.println("\nInvalid selection.");
           		printMarkingPeriods();
           		markingPeriod = in.nextInt();
       		}
       	}
        	
       	ArrayList<String> assignments = PowerSchool.getAssignments(courseId, markingPeriod);//get courses by courseId and markingPeriod
       	if(assignments.isEmpty()) {
       		System.out.println("\nThere are no assignments here.\n");
       		hasAssignment = false;
        }
        while(hasAssignment) {
        	System.out.println("\nChoose an assignment.\n");
        	for(int i = 0; i <= assignments.size()-1; i++) {
        		System.out.println("[" + (i + 1) + "] " + assignments.get(i) + " (" + PowerSchool.getPointValue(assignments.get(i)) + " pts)");
        	}
        	System.out.print("\n::: ");
        	int assignmentSelection = in.nextInt();
        	if(assignmentSelection < 1 || assignmentSelection > assignments.size()) {
        		while(assignmentSelection < 1 || assignmentSelection > assignments.size()) {
        			System.out.println("\nInvlaid selection.");
        			System.out.println("\nChoose an assignment.\n");
                	for(int i = 0; i <= assignments.size()-1; i++) {
                		System.out.println("[" + (i + 1) + "] " + assignments.get(i) + " (" + PowerSchool.getPointValue(assignments.get(i)) + " pts)");
                	}
                	System.out.print("\n::: ");
                	assignmentSelection = in.nextInt();
        		}
        	}
        	String title = assignments.get(assignmentSelection-1); //title of the assignment
        	
        	String StringCourseId = PowerSchool.getCourseIdFromCourseNo2(courseNo);//courseId
        	ArrayList<String> studentIds = PowerSchool.getStudentId(StringCourseId);//getting studentIds enrolled in a course with courseId
        	ArrayList<String> students = new ArrayList<String>();
        	for(int i = 0; i < studentIds.size(); i++) {
        		students.addAll(PowerSchool.getStudentsByStudentId(studentIds.get(i))); //getting students firstname, lastname, and gpa
        	}
        	ArrayList<String> studentsFirstNames = new ArrayList<String>();
        	for(int i = 0; i < studentIds.size(); i++) {
        		studentsFirstNames.addAll(PowerSchool.getStudentsFirstNames(studentIds.get(i)));
        	}
        	ArrayList<String> studentLastNames = new ArrayList<String>();
        	for(int i = 0; i < studentIds.size(); i++) {
        		studentLastNames.addAll(PowerSchool.getStudentsLastNames(studentIds.get(i)));
        	}
        	System.out.println("\nChoose a student.\n");
        	for(int i = 0, x = 1; i < students.size(); i = i + 3) {
        		System.out.println("[" + (x) + "] " + students.get(i+1) + ", " + students.get(i));
        		x++;
        	}
        	System.out.print("\n::: ");
        	int studentSelection = in.nextInt();
        	if(studentSelection < 1 || studentSelection > students.size()/3) {
        		while(studentSelection < 1 || studentSelection > students.size()/3) {
        			System.out.println("\nInvalid selection.");
        			System.out.println("\nChoose a student.\n");
                	for(int i = 0, x = 1; i < students.size(); i = i + 3) {
                		System.out.println("[" + (x) + "] " + students.get(i+1) + ", " + students.get(i));
                		x++;
                	}
                	System.out.print("\n::: ");
                	studentSelection = in.nextInt();
        		}
        	}
        	String studentLastName = studentLastNames.get(studentSelection-1);
        	String studentFirstName = studentsFirstNames.get(studentSelection-1);
        	int rows = PowerSchool.assignmentGradesRows();
        	System.out.println("\nAssignment: " + title + " (" + PowerSchool.getPointValue(title) + " pts)");
        	System.out.println("Student: " + studentLastName + ", " + studentFirstName);
        	int assignmentId = PowerSchool.getAssignmentIdFromTitlePlus(title, courseId, markingPeriod);
        	int studentId = Integer.parseInt(studentIds.get(studentSelection-1));
        	if(rows == 0) {
        		System.out.println("Current Grade: --");
        	} else {
        		if(PowerSchool.previousGrade(courseId, assignmentId, studentId) == -1) {
        			System.out.println("Current Grade: --");
        		} else {
        			System.out.println("Current Grade: " + PowerSchool.previousGrade(courseId, assignmentId, studentId));
        		}
        	}
        	System.out.print("\nNew Grade: ");
        	double pointsEarned = in.nextDouble();
        	if(pointsEarned < 0 || pointsEarned > PowerSchool.getPointValue(title)) {
        		while(pointsEarned < 0 || pointsEarned > PowerSchool.getPointValue(title)) {
        			System.out.print("\nChoose a grade between 0 and the possible number of points.\n\nNew Grade: ");
                	pointsEarned = in.nextDouble();
        		}
        	}
        	in.nextLine();
        	String wantTo = "you want to enter this grade?";
        	System.out.print("Are you sure you want to enter this grade? (y/n) ");
        	String yesNo = in.nextLine();
        	yesNo = yesNo.toLowerCase();
        	int checked = checkYesNo(yesNo, wantTo);
        	
        	if(checked == -1) {
    			System.out.println("");
        	} else if (checked == 1) {

        			PowerSchool.addAssignmentGrade(courseId, assignmentId, studentId, pointsEarned, PowerSchool.getPointValue(title));
            		System.out.println("\nSuccesfully entered grade.\n");

        		ArrayList<Integer> assignmentIds = PowerSchool.getAssignmentIdByMP(markingPeriod);
        		ArrayList<Double> grades = new ArrayList<Double>();
        		
        		for(int i = 0; i < assignmentIds.size(); i++) {
        			grades.addAll(PowerSchool.getGrades(courseId, assignmentIds.get(i),studentId));
        		}
        		ArrayList<Double> percent = new ArrayList<Double>();
        		for(int i = 0; i < grades.size(); i+=2) {
        			percent.add((grades.get(i)/grades.get(i+1))*100);
        		}
        		double total = 0;
        		for(int i = 0; i < percent.size(); i++) {
        			total += percent.get(i);
        		}
        		double average = total/percent.size();      		
        		switch (markingPeriod) {
                case 1: PowerSchool.updateCourseGradesMP1(courseId, studentId, average); break;
                case 2: PowerSchool.updateCourseGradesMP2(courseId, studentId, average); break;
                case 3: PowerSchool.updateCourseGradesMP3(courseId, studentId, average); break;
                case 4: PowerSchool.updateCourseGradesMP4(courseId, studentId, average); break;
                case 5: PowerSchool.updateCourseGradesMidterm(courseId, studentId, average); break;
                case 6: PowerSchool.updateCourseGradesFinal(courseId, studentId, average); break;
                default: System.out.println("\nInvalid selection.\n"); break;
            	}
        	}
        	ArrayList<Double> grades = new ArrayList<Double>();
        	if(PowerSchool.getMP1Grade(courseId, studentId) == null){
        		grades.add(-1.0);
        	} else {
        		grades.add((Double) PowerSchool.getMP1Grade(courseId, studentId));
        	}
        	if(PowerSchool.getMP2Grade(courseId, studentId) == null){
        		grades.add(-1.0);
        	} else {
        		grades.add((Double) PowerSchool.getMP2Grade(courseId, studentId));
        	}
        	if(PowerSchool.getMP3Grade(courseId, studentId) == null){
        		grades.add(-1.0);
        	} else {
        		grades.add((Double) PowerSchool.getMP3Grade(courseId, studentId));
        	}
        	if(PowerSchool.getMP4Grade(courseId, studentId) == null){
        		grades.add(-1.0);
        	} else {
        		grades.add((Double) PowerSchool.getMP4Grade(courseId, studentId));
        	}
        	if(PowerSchool.getMidtermGrade(courseId, studentId) == null){
        		grades.add(-1.0);
        	} else {
        		grades.add((Double) PowerSchool.getMidtermGrade(courseId, studentId));
        	}

        	double grade = Utils.getGrade(grades);
        	PowerSchool.updateCourseGrade(courseId, studentId, grade);
        	PowerSchool.getCourseGrades(studentId);
        	
        	ArrayList<Object> courseGrades = PowerSchool.getCourseGrades(studentId);
        	ArrayList<Double> fourScale = new ArrayList<Double>();
        	for(int i = 0; i < courseGrades.size(); i++) {
        		if((Double) courseGrades.get(i) == -1.0) {
        			
        		} else if ((Double) courseGrades.get(i) >= 93 && (Double) courseGrades.get(i) <= 100) {
        			fourScale.add(4.0);
        		} else if ((Double) courseGrades.get(i) >= 90 && (Double) courseGrades.get(i) <= 92) {
        			fourScale.add(3.7);
        		} else if ((Double) courseGrades.get(i) >= 87 && (Double) courseGrades.get(i) <= 89) {
        			fourScale.add(3.3);
        		} else if ((Double) courseGrades.get(i) >= 83 && (Double) courseGrades.get(i) <= 86) {
        			fourScale.add(3.0);
        		} else if ((Double) courseGrades.get(i) >= 80 && (Double) courseGrades.get(i) <= 82) {
        			fourScale.add(2.7);
        		} else if ((Double) courseGrades.get(i) >= 77 && (Double) courseGrades.get(i) <= 79) {
        			fourScale.add(2.3);
        		} else if ((Double) courseGrades.get(i) >= 73 && (Double) courseGrades.get(i) <= 76) {
        			fourScale.add(2.0);
        		} else if ((Double) courseGrades.get(i) >= 70 && (Double) courseGrades.get(i) <= 72) {
        			fourScale.add(1.7);
        		} else if ((Double) courseGrades.get(i) >= 67 && (Double) courseGrades.get(i) <= 69) {
        			fourScale.add(1.3);
        		} else if ((Double) courseGrades.get(i) >= 65 && (Double) courseGrades.get(i) <= 66) {
        			fourScale.add(1.0);
        		} else if ((Double) courseGrades.get(i) > 65) {
        			fourScale.add(0.0);
        		}
        	}
        	ArrayList<Integer> courseIds = PowerSchool.getCourseIdWithStudentId(studentId);
        	ArrayList<Integer> creditHours = PowerSchool.getCreditHours(courseIds);
        	int totalGradePoints = 0;
        	int hours = 0;
        	for(int i = 0; i < fourScale.size(); i++) {
        		totalGradePoints += fourScale.get(i)*creditHours.get(i);
        		hours += creditHours.get(i);
        	}
        	double gpa = (double) (totalGradePoints)/ (double) hours;
        	double roundedGpa = Math.round(gpa * 100.0) / 100.0;
        	PowerSchool.updateGPA(roundedGpa, studentId);
        	hasAssignment = false;
    	}
    }
    
    /////// STUDENT METHODS /////////////////////////////////////////////////////////

    
    private void showStudentUI() {
        while (activeUser != null) {
            switch (getStudentMenuSelection()) {
                case COURSE: viewCourseGrades(); break;
                case ASSIGNMENT: viewAssignmentGradesByCourse(); break;
                case PASSWORD: changePassword(false); break;
                case LOGOUT: logout(); break;
                default: System.out.println("\nInvalid selection."); break;
            }
        }
    }
    
    /*
     * Retrieves a teacher's menu selection.
     * 
     * @return the menu selection
     */

	private StudentAction getStudentMenuSelection() {
        System.out.println();
        
        System.out.println("[1] View course grades.");
        System.out.println("[2] View assignment grades by course.");
        System.out.println("[3] Change password.");
        System.out.println("[4] Logout.");
        System.out.print("\n::: ");

        switch (Utils.getInt(in, -1)) {
            case 1: return StudentAction.COURSE;
            case 2: return StudentAction.ASSIGNMENT;
            case 3: return StudentAction.PASSWORD;
            case 4: return StudentAction.LOGOUT;
        }
        
        return null;
    }
	
	public void viewCourseGrades() {
    	int studentId = PowerSchool.getStudentIdByUserId(activeUser);
    	ArrayList<Integer> courseIds = PowerSchool.getCourseIdWithStudentId(studentId);
    	ArrayList<String> courses = PowerSchool.getCourseName(activeUser, courseIds);
    	ArrayList<String> courseGrades = PowerSchool.getCourseGrade(PowerSchool.getStudentIdByUserId(activeUser), courseIds);
    	System.out.println("");
    	
    	for(int i = 0; i <= courseGrades.size()-1; i++) {
    		System.out.println((i + 1) + ". " + courses.get(i) + " / " + courseGrades.get(i));
    	}
    	System.out.println("");
    }

	public void viewAssignmentGradesByCourse() {
    	System.out.println("\nChoose a course.\n");
    	int studentId = PowerSchool.getStudentIdByUserId(activeUser);
    	ArrayList<Integer> courseIds = PowerSchool.getCourseIdWithStudentId(studentId);
    	ArrayList<String> courses = PowerSchool.getCourseNumberWithCourseId(activeUser, courseIds);
    	for(int i = 0; i <= courses.size()-1; i++) {
    		System.out.println("[" + (i + 1) + "] " + courses.get(i));
    	}
    	System.out.print("\n::: ");
    	int courseSelection = in.nextInt();
    	if(courseSelection < 1 || courseSelection > courses.size()) {
    		while(courseSelection < 1 || courseSelection > courses.size()) {
    			System.out.println("\nInvalid selection.\n");
    			System.out.println("Choose a course.\n");
    			for(int i = 0; i <= courses.size()-1; i++) {
    	    		System.out.println("[" + (i + 1) + "] " + courses.get(i));
    	    	}
    	    	System.out.print("\n::: ");
    	    	courseSelection = in.nextInt();
    		}
    	}
    	int courseId = courseIds.get(courseSelection-1);
    	printMarkingPeriods();
    	int markingPeriod = in.nextInt();
    	if(markingPeriod < 1 || markingPeriod > 6) {
    		while(markingPeriod < 1 || markingPeriod > 6) {
    			System.out.println("\nInvalid selection.");
    			printMarkingPeriods();
    	    	markingPeriod = in.nextInt();
    		}
    	}
    	
    	ArrayList<String> titles = PowerSchool.getAssignmentTitle(courseId, markingPeriod);
    	if(titles.isEmpty()) {
    		System.out.println("\nThere are no assignments in this class and marking period.\n");
    	} else {
    		
    		System.out.println("");
    		String currentGrade = "";
    		for(int i = 0; i <= titles.size()-1; i++) {
    			int assignmentId = PowerSchool.getAssignmentIdFromTitlePlus(titles.get(i), courseId, markingPeriod);
    			if(PowerSchool.previousGrade(courseId, assignmentId, studentId) == -1) {
        			currentGrade = "--";
        		} else {
        			currentGrade = String.valueOf(PowerSchool.previousGrade(courseId, assignmentId, studentId));
        		}
    			System.out.println((i + 1) + ". " + titles.get(i) + " / " + currentGrade + " (out of " + PowerSchool.getPointValue(titles.get(i)) + " pts)");
        	}
        	System.out.println("");
    	}
    }
    
    /////// SECONDARY MENUS ///////////////////////////////////////////////////////////////
    
    /*
     * Retrieves the user's department selection.
     * 
     * @return the selected department
     */
    
    private int getDepartmentSelection() {
        int selection = -1;
        System.out.println("\nChoose a department.");
        
        while (selection < 1 || selection > 6) {
            System.out.println("\n[1] Computer Science.");
            System.out.println("[2] English.");
            System.out.println("[3] History.");
            System.out.println("[4] Mathematics.");
            System.out.println("[5] Physical Education.");
            System.out.println("[6] Science.");
            System.out.print("\n::: ");
            
            selection = Utils.getInt(in, -1);
        }
        
        return selection;
    }
    
    /*
     * Retrieves a user's grade selection.
     * 
     * @return the selected grade
     */
    
    private int getGradeSelection() {
        int selection = -1;
        System.out.println("\nChoose a grade level.");
        
        while (selection < 1 || selection > 4) {
            System.out.println("\n[1] Freshman.");
            System.out.println("[2] Sophomore.");
            System.out.println("[3] Junior.");
            System.out.println("[4] Senior.");
            System.out.print("\n::: ");
            
            selection = Utils.getInt(in, -1);
        }
        
        return selection + 8;   // +8 because you want a value between 9 and 12
    }
    
    /*
     * Retrieves a user's course selection.
     * 
     * @return the selected course
     */
    
    private String getCourseSelection() throws SQLException {
        boolean valid = false;
        String courseNo = null;
        
        while (!valid) {
            System.out.print("\nCourse No.: ");
            courseNo = in.next();
            
            if (validateCourse(courseNo)) {
                valid = true;
            } else {
                System.out.println("\nCourse not found.");
            }
        }
        
        return courseNo;
    }
    
    private boolean validateCourse(String courseId) {
    	boolean isValid = false;
		for(int i=1; i <  PowerSchool.getNumberOfCourses(); i++) {
			if(PowerSchool.getCourseNumber(i).equals(courseId)) {
				isValid = true;
			}
		}
		return isValid;
	}
    
    private String getCourseSelectionTeacher() {
    	Teacher teacher = PowerSchool.getTeacher(activeUser);
		ArrayList<String> courses = PowerSchool.getCourses(teacher.getDepartmentId());
		System.out.println();		 
		System.out.println("Choose a course.\n");		 
        int courseSelection = -1;
        
        while(courseSelection <= 0 || courseSelection > courses.size()) {
        	int j = 1;
        	for (String i: courses) {
                System.out.println("["+ j++ + "] " + i);
            }
	       	System.out.print("\n::: ");
	       	courseSelection = Utils.getInt(in, -1);
	       	
	       	if(courseSelection <= 0 || courseSelection > courses.size()) {
	       		System.out.println("\nInvalid Selection.\n");
	       	}
        }
		return courses.get(courseSelection-1);
	}
    
    public void printMarkingPeriods() {
    	System.out.println("\nChoose a marking period or exam status.\n");
    	System.out.println("[1] MP1 assignment.");
    	System.out.println("[2] MP2 assignment.");
    	System.out.println("[3] MP3 assignment.");
    	System.out.println("[4] MP4 assignment.");
    	System.out.println("[5] Midterm exam.");
    	System.out.println("[6] Final exam.");
    	System.out.print("\n::: ");
    }
    
    private int checkYesNo(String yesNo, String wantTo) {
    	if(!yesNo.equals("y") && !yesNo.equals("n")) {
    		while(!yesNo.equals("y") && !yesNo.equals("n")) {
    			System.out.println("\nInvalid selection.\n");
    			System.out.print("Are you sure " + wantTo +" (y/n) ");
    			yesNo = in.nextLine();
    			yesNo = yesNo.toLowerCase();
    		}
    		if((yesNo.equals("n"))) {
    			return -1;
    		} else if (yesNo.equals("y")) {
    			return 1;
    		}
    	} else if (yesNo.equals("n")) {
    		return -1;
    	} else if (yesNo.equals("y")) {
    		return 1;
    	}
		return 0;
    }
    
    /////// ALL USER METHODS //////////////////////////////////////////////////////////////
    
    /*
     * Allows a user to change his or her password.
     * 
     * @param firstLogin true if the user has never logged in; false otherwise
     */
    
    private void changePassword(boolean firstLogin) {
        // if it isn't the user's first login...
        //      ask the user for his or her current password
        //
        // ask all users (first login or not) to enter a new password
        //
        // change the password (this will require a call to the database)
        //      this requires three pieces of information: the username, the old password, and the new password
        //      the old password will either be something the use entered (if it isn't his or her first login) or
        //      it'll be the same as their username
    	
    	if (firstLogin == false) {
    		System.out.print("\nEnter current password: ");
    		String currentPassword = in.next();
    		System.out.print("Enter a new password: ");
            String newPassword = in.next();
            
            if (activeUser.getPassword().equals(Utils.getHash(currentPassword))) {
            	PowerSchool.changePassword(activeUser.getUsername(), newPassword);
            } else {
            	System.out.println("\nInvalid current password.");
            }
            
    	}
    	
    }
    
    /*
     * Logs out of the application.
     */
    
    private void logout() {
        //
        // ask user to confirm intent to logout
        //
        // if confirmed...
        //      set activeUser to null
        //
    	
    	System.out.println();
        
        if (Utils.confirm(in, "Are you sure? (y/n) ")) {
    		
        	activeUser = null;
            
        }
    }
    
    /////// SHUTDOWN METHODS //////////////////////////////////////////////////////////////
    
    /*
     * Shuts down the application.
     * 
     * @param e the error that initiated the shutdown sequence
     */
    
    private void shutdown(Exception e) {
        if (in != null) {
            in.close();
        }
        
        System.out.println("Encountered unrecoverable error. Shutting down...\n");
        System.out.println(e.getMessage());
                
        System.out.println("\nGoodbye!");
        System.exit(0);
    }
    
    /*
     * Releases all resources and kills the application.
     */
    
    private void shutdown() {        
        System.out.println();
            
        if (Utils.confirm(in, "Are you sure? (y/n) ")) {
            if (in != null) {
                in.close();
            }
            
            System.out.println("\nGoodbye!");
            System.exit(0);
        }
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
}