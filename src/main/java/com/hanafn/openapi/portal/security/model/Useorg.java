package com.hanafn.openapi.portal.security.model;

import com.hanafn.openapi.portal.security.model.audit.UserDateAudit;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "PORTAL_USEORG_INFO")
@Data
@EqualsAndHashCode(callSuper=false)
public class Useorg {

    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Size(max = 30)
	@Column(name="USER_KEY")
    private String userKey;

    @Size(max = 10)
    @Column(name="USEORG_ID")
    private String useorgId;

	@NotBlank
    @Size(max = 50)
    @Column(name="USEORG_NM")
    private String useorgNm;

    @NotBlank
    @Size(max = 10)
    @Column(name="BRN")
    private String brn;

    @NotBlank
    @Size(max = 256)
    @Column(name="ENC_KEY")
    private String encKey;

    @NotBlank
    @Size(max = 256)
    @Column(name="USEORG_PWD")
    private String password;

    @NotBlank
    @Size(max = 10)
    @Column(name="USEORG_STAT_CD")
    private String useorgStatCd;

    @NotBlank
    @Size(max = 10)
    @Column(name="ENTR_CD")
    private String entrCd;

    @NotBlank
    @Size(max = 10)
    @Column(name="USEORG_GB")
    private String useorgGb;

    @NotBlank
    @Size(max = 10)
    @Column(name="USEORG_USER_NM")
    private String useorgUserNm;

    @NotBlank
    @Size(max = 30)
    @Column(name="USEORG_USER_EMAIL")
    private String useorgUserEmail;

    @NotBlank
    @Size(max = 15)
    @Column(name="USEORG_USER_TEL")
    private String useorgUserTel;

    @Size(max = 10)
    @Column(name="USEORG_SEL_API")
    private String useorgSelApi;

    @Size(max = 10)
    @Column(name="USEORG_DOMAIN")
    private String useorgDomain;

    @Size(max = 10)
    @Column(name="USEORG_BANK")
    private String useorgBank;

    @Size(max = 14)
    @Column(name="USEORG_BANK_NO")
    private String useorgBankNo;

    @Size(max = 50)
    @Column(name="USEORG_OWN_NM")
    private String useorgOwnNm;

    @Size(max = 15)
    @Column(name="USEORG_NO")
    private String useorgNo;

    @Size(max = 10)
    @Column(name="USEORG_TEL")
    private String useorgTel;

    @Size(max = 50)
    @Column(name="USEORG_BUSS_NM")
    private String useorgBussNm;

    @Size(max = 200)
    @Column(name="USEORG_ADDR")
    private String useorgAddr;

    @Size(max = 10)
    @Column(name="USEORG_HOMEPAGE")
    private String useorgHomepage;

    @Size(max = 1000)
    @Column(name="USEORG_CTNT")
    private String useorgCtnt;

    @Size(max = 1000)
    @Column(name="USEORG_UPLOAD")
    private String useorgUpload;

    @Size(max = 2000)
    @Column(name="ERROR_MSG")
    private String errorMsg;

    @Column(name="HBN_USE_YN")
    private String hbnUseYn;

    @Column(name="HNW_USE_YN")
    private String hnwUseYn;

    @Column(name="HLF_USE_YN")
    private String hlfUseYn;

    @Column(name="HCP_USE_YN")
    private String hcpUseYn;

    @Column(name="HCD_USE_YN")
    private String hcdUseYn;

    @Column(name="HSV_USE_YN")
    private String hsvUseYn;

    @Column(name="HMB_USE_YN")
    private String hmbUseYn;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "PORTAL_USER_ROLE_INFO",
            joinColumns = @JoinColumn(name = "USER_KEY"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_CD"))
    private Set<Role> roles = new HashSet<>();


    public Useorg() {

    }

    public Useorg(String userKey, String useorgNm, String password, String useorgStatCd, String entrCd, String useorgCtnt, String errorMsg) {
        this.userKey = userKey;
        this.useorgNm = useorgNm;
        this.password = password;
        this.useorgStatCd = useorgStatCd;
        this.entrCd = entrCd;
        this.useorgCtnt = useorgCtnt;
        this.errorMsg = errorMsg;
    }
}