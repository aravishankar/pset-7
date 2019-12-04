package com.apcsa.model;

import com.apcsa.model.User;

public class Teacher extends User {

    private int teacherId;
    private int departmentId;
    private String firstName;
    private String lastName;
    
    public teacher(int teacherId, int departmentId, String firstName, String lastName) {
        this.teacherId = teacherId;
        this.departmentId = departmentId;
    	this.firstName = firstName;
        this.lastName = lastName;
    }
    
    public void addAssignment() {
    	return null;
    }
    
}