package com.bankapp.hr.converter;

import com.bankapp.hr.dto.DepartmentDto;
import com.bankapp.hr.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentDtoConverter {

    public DepartmentDto convert(Department department) {

        if (department == null) {
            return null;
        }

        return DepartmentDto.builder()
                .id(department.getId())
                .name(department.getName())
                .build();
    }
}
