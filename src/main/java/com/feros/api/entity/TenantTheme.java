package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tenant_themes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantTheme extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, unique = true)
    private Tenant tenant;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "favicon_url")
    private String faviconUrl;

    @Column(name = "primary_color")
    private String primaryColor = "#1E40AF";

    @Column(name = "secondary_color")
    private String secondaryColor = "#64748B";

    @Column(name = "accent_color")
    private String accentColor = "#F59E0B";

    @Column(name = "sidebar_color")
    private String sidebarColor = "#1E293B";

    @Column(name = "text_color")
    private String textColor = "#0F172A";

    @Column(name = "font_family")
    private String fontFamily = "Inter";

    @Column(name = "company_tagline")
    private String companyTagline;

    @Column(name = "is_active")
    private Boolean isActive = true;
}