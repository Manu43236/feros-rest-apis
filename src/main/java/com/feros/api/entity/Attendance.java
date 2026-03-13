package com.feros.api.entity;

import com.feros.api.entity.master.AttendanceType;
import com.feros.api.entity.master.LeaveType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_type_id", nullable = false)
    private AttendanceType attendanceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id")
    private LeaveType leaveType;

    @Column(name = "leave_reason", columnDefinition = "TEXT")
    private String leaveReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by", nullable = false)
    private User markedBy;

    @Column(name = "marked_at", nullable = false)
    private LocalDateTime markedAt;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive = true;
}