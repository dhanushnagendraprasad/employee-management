package com.example.employee_management.config;

import com.example.employee_management.model.AppUser;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // 1. Intercept the "applySalaryHike" method in EmployeeService
    //    We use 'returning' to capture the updated AppUser object returned by the method
    @AfterReturning(
        pointcut = "execution(* com.example.employee_management.service.EmployeeService.applySalaryHike(..))", 
        returning = "result"
    )
    public void logSalaryChange(JoinPoint joinPoint, Object result) {
        
        // Get the User object that was returned by the service
        AppUser updatedUser = (AppUser) result;

        // Get the arguments passed to the method (ID and Percentage)
        Object[] args = joinPoint.getArgs();
        Double percentage = (Double) args[1];

        // Get the CURRENTLY LOGGED IN user (The Admin/Manager doing the change)
        String currentAdmin = getCurrentUsername();

        // Log the Audit Trail
        logger.info("AUDIT EVENT: Manager '{}' applied a {}% salary hike to Employee '{}'. New Salary: {}", 
                    currentAdmin, 
                    percentage, 
                    updatedUser.getUsername(), 
                    updatedUser.getSalary());
    }

    // Helper to get the logged-in user from Spring Security
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getName();
        }
        return "SYSTEM";
    }
}