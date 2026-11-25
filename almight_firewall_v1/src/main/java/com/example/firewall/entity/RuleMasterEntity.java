package com.example.firewall.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="rule")
public class RuleMasterEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String ruleName;

    @Column(unique = true)
    public String pattern;

    public String pattern_type;

    public String field;

    public String action;

    @ManyToOne
    @JoinColumn(name = "threat_type_id")
    public ThreatTypeMasterEntity threat;

    @ManyToOne
    @JoinColumn(name = "user_id")
    public UserMasterEntity user;

    public String status;


}