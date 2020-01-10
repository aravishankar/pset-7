package com.apcsa.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.apcsa.model.User;

public class Administrator extends User {

    private int administratorId;
    private String firstName;
    private String lastName;
    private String jobTitle;

    /**
     * Creates an instance of the Administrsator class.
     * 
     * @param user
     * @param rs
     * @throws SQLException
     */
    
    public Administrator(User user, ResultSet rs) throws SQLException {
        super(user);
        
        this.administratorId = rs.getInt("administrator_id");
        this.firstName = rs.getString("first_name");
        this.lastName = rs.getString("last_name");
        this.jobTitle = rs.getString("job_title");
    }
    
    //
    // you'll likely need to define and implement a combination of getters and setters. you won't
    // necessarily need a getter and setter for every instance variable, but you will need some.
    //
}