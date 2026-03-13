package com.feros.api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantThemeRequest {
    private String logoUrl;
    private String faviconUrl;
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;
    private String sidebarColor;
    private String textColor;
    private String fontFamily;
    private String companyTagline;
}