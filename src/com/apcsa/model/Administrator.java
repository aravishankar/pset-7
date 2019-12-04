package com.apcsa.model;

import com.apcsa.model.User;

public class Administrator extends User {

    private int administratorId;
    private String firstName;
    private String lastName;
    private String jobTitle;
    
    
    
    public Administrator(int administratorId, String firstName, String lastName, String jobTitle) {
        super(administratorId, jobTitle, User.getUsername(), User.getPassword(), User.getLastLogin());
        this.administratorId = administratorId;
    	this.firstName = firstName;
        this.lastName = lastName;
        this.jobTitle = jobTitle;
    }
    
    

}