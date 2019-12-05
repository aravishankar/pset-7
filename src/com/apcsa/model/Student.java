package com.apcsa.model;

import com.apcsa.model.User;

public class Student extends User {

    private int studentId;
    private int classRank;
    private int gradeLevel;
    private int graduationYear;
    private double gpa;
    private String firstName;
    private String lastName;
    
    public Student(int studentId, int classRank, int gradeLevel, int graduationYear, double gpa, String firstName, String lastName) {
        this.studentId = studentId;
        this.classRank = classRank;
        this.gradeLevel = gradeLevel;
        this.graduationYear= graduationYear;
        this.gpa = gpa;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    
    
}