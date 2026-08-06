package com.taxin60sec.backend.entity;
import com.taxin60sec.backend.entity.base.BaseEntity; import com.taxin60sec.backend.entity.enums.*; import jakarta.persistence.*; import jakarta.validation.constraints.*; import lombok.Getter; import lombok.Setter; import java.time.LocalDate;
@Entity @Getter @Setter @Table(name="business_profiles")
public class BusinessProfile extends BaseEntity {
 @NotNull @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="client_profile_id",nullable=false) private ClientProfile clientProfile;
 @NotBlank @Size(max=180) @Column(nullable=false,length=180) private String businessName;
 @NotNull @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private BusinessType businessType;
 @Size(max=20) @Pattern(regexp="[A-Z]{5}[0-9]{4}[A-Z]",message="Invalid PAN format") @Column(length=20) private String panNumber;
 @Size(max=20) @Pattern(regexp="[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z][Z][0-9A-Z]",message="Invalid GSTIN format") @Column(length=20) private String gstin;
 @Size(max=20) @Column(length=20) private String tanNumber; @Size(max=30) @Column(length=30) private String cin; @Size(max=30) @Column(length=30) private String msmeNumber;
 private LocalDate incorporationDate; @NotNull @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private BusinessStatus businessStatus=BusinessStatus.ACTIVE;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="assigned_ca_id") private User assignedCA;
 @Size(max=1000) @Column(length=1000) private String address;
}
