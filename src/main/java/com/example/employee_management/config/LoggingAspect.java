package com.example.employee_management.config;


import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Log before creating or updating an employee
    @Before("execution(* com.example.employee_management.service.EmployeeService.save(..)) && args(employee)")
    public void logBeforeSave(Object employee) {
    	System.out.printf("Aspect Truggered : Preparing to save or update employee: {}", employee);
        logger.info("Preparing to save or update employee: {}", employee);
    }

    // Log after successfully creating or updating an employee
    @AfterReturning(pointcut = "execution(* com.example.employee_management.service.EmployeeService.save(..))", returning = "result")
    public void logAfterSave(Object result) {
        logger.info("Employee saved or updated successfully: {}", result);
    }

    // Log before deleting an employee
    @Before("execution(* com.example.employee_management.service.EmployeeService.delete(..)) && args(id)")
    public void logBeforeDelete(Long id) {
    	System.out.printf("Aspect Truggered : Preparing to Delete employee: {}", id);
        logger.info("Preparing to delete employee with ID: {}", id);
    }

    // Log after applying a hike
    @AfterReturning(pointcut = "execution(* com.example.employee_management.service.EmployeeService.applyHike(..))", returning = "result")
    public void logAfterHike(Object result) {
        if (result != null) {
        	System.out.printf("Aspect Truggered : Hike applied successfully. Updated employee: : {}", result);
            logger.info("Hike applied successfully. Updated employee: {}", result);
        } else {
            logger.error("Failed to apply hike. Employee not found.");
        }
    }
}
