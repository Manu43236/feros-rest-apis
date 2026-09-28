package com.feros.api.dto.response.report;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffDirectoryRow {
    private Long staffId;
    private String name;
    private String role;
    private String designation;
    private String joiningDate;
}
