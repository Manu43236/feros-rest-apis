package com.feros.api.dto.request;

import com.feros.api.enums.ModuleKey;
import com.feros.api.enums.RoleName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoleModuleAccessRequest {

    private List<Entry> entries;

    @Getter
    @Setter
    public static class Entry {
        private RoleName role;
        private ModuleKey moduleKey;
        private Boolean enabled;
    }
}
