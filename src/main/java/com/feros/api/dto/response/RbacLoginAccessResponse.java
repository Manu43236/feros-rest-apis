package com.feros.api.dto.response;

import com.feros.api.enums.DeviceType;
import com.feros.api.enums.RoleName;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RbacLoginAccessResponse {

    private List<Entry> entries;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Entry {
        private RoleName role;
        private DeviceType platform;
        private Boolean allowed;
    }
}
