package com.feros.api.dto.request;

import com.feros.api.enums.DeviceType;
import com.feros.api.enums.RoleName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RbacLoginAccessRequest {

    private List<Entry> entries;

    @Getter
    @Setter
    public static class Entry {
        private RoleName role;
        private DeviceType platform;
        private Boolean allowed;
    }
}
