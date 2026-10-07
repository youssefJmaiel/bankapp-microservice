package com.bankapp.hr.converter;

import com.bankapp.hr.dto.DepartmentRequest;
import com.bankapp.hr.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentRequestConverter {

    public Department convert(DepartmentRequest request) {

        if (request == null) {
            return null;
        }

        return Department.builder()
                .name(request.getName())
                .build();
    }
}
