package com.bankapp.hr;

import com.bankapp.hr.entity.Department;
import com.bankapp.hr.entity.Employee;
import com.bankapp.hr.repository.DepartmentRepository;
import com.bankapp.hr.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HrDataLoader implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public HrDataLoader(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if(employeeRepository.count() == 0) {

            Department devDept = departmentRepository.save(Department.builder().name("Development").build());
            Department hrDept = departmentRepository.save(Department.builder().name("HR").build());
            Department financeDept = departmentRepository.save(Department.builder().name("Finance").build());

            employeeRepository.saveAll(List.of(
                    Employee.builder().firstName("Ali").lastName("Ben Ahmed").email("ali.benahmed@example.com").department(devDept).build(),
                    Employee.builder().firstName("Sami").lastName("Trabelsi").email("sami.trabelsi@example.com").department(hrDept).build(),
                    Employee.builder().firstName("Mouna").lastName("Fakhfakh").email("mouna.fakhfakh@example.com").department(financeDept).build()
            ));

            System.out.println("HR-Service: Employees loaded!");
        }
    }
}