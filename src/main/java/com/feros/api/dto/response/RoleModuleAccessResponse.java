package com.feros.api.dto.response;

import com.feros.api.enums.ModuleKey;
import com.feros.api.enums.RoleName;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleModuleAccessResponse {

    private List<Entry> entries;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Entry {
        private RoleName role;
        private ModuleKey moduleKey;
        private Boolean enabled;
    }
}
