package com.hanafn.openapi.portal.security;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.hanafn.openapi.portal.security.model.HfnUser;
import com.hanafn.openapi.portal.security.model.Useorg;
import com.hanafn.openapi.portal.security.model.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class UserPrincipal implements UserDetails {
	private static final long serialVersionUID = -2717071117846187144L;

	// 사용자 정보들의 멤버변수 선언
	private String userKey;

    private String userId;

    private String username;

    @JsonIgnore
    private String password;

    private String tmpPasswordYn;

    private String pwdChangeDt;

    private String portalTosYn;

    private String privacyTosYn;

    private String siteCd;

    private String hfnCd;

    private String userType;

    private String entrCd;

    private String useorgGb;

    private String useorgKey;

    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String userKey, String userId, String userNm, String password, String siteCd, String userType,
                         String entrCd, String useorgGb, Collection<? extends GrantedAuthority> authorities, String tmpPasswordYn, String pwdChangeDt,
                         String portalTosYn, String privacyTosYn, String hfnCd, String useorgKey) {
        this.userKey = userKey;
        this.userId = userId;
        this.username = userNm;
        this.password = password;
        this.siteCd = siteCd;
        this.userType = userType;
        this.entrCd = entrCd;
        this.useorgGb = useorgGb;
        this.authorities = authorities;
        this.tmpPasswordYn = tmpPasswordYn;
        this.pwdChangeDt = pwdChangeDt;
        this.portalTosYn = portalTosYn;
        this.privacyTosYn = privacyTosYn;
        this.hfnCd = hfnCd;
        this.useorgKey = useorgKey;
    }

    public UserPrincipal() {

    }

    public static UserPrincipal create(User user, String pwHisDate) {

        List<GrantedAuthority> authorities = user.getRoles().stream().map(role ->
                new SimpleGrantedAuthority(role.getName().name())
        ).collect(Collectors.toList());

        String userType = "USER";
        if(StringUtils.isNotEmpty(user.getEntrCd())) userType = "ORGD";

        return new UserPrincipal(
            user.getUserKey(),
            user.getUserId(),
            user.getUserNm(),
            user.getPassword(),
            "userPortal",
            userType,
            user.getEntrCd(),
            "",
            authorities,
            "",
            pwHisDate,
            user.getPortalTosYn(),
            user.getPrivacyTosYn(),
            "",
            user.getEntrCd()    // USEORG_INFO TABLE의 USERKEY를 바라보므로 useorgKey로 세팅
        );
    }

    public static UserPrincipal create(Useorg useorg, String pwHisDate) {

        List<GrantedAuthority> authorities = useorg.getRoles().stream().map(role ->
                new SimpleGrantedAuthority(role.getName().name())
        ).collect(Collectors.toList());

        return new UserPrincipal(
            useorg.getUserKey(),
            useorg.getUseorgId(),
            useorg.getUseorgNm(),
            useorg.getPassword(),
            "userPortal",
            "ORGM",
            useorg.getEntrCd(),
            useorg.getUseorgGb(),
            authorities,
            "",
            pwHisDate,
            "",
            "",
            "",
            useorg.getUserKey()
        );
    }

    public static UserPrincipal create(HfnUser hfnUser) {

        List<GrantedAuthority> authorities = hfnUser.getRoles().stream().map(role ->
                new SimpleGrantedAuthority(role.getName().name())
        ).collect(Collectors.toList());

        return new UserPrincipal(
            hfnUser.getUserKey(),
            hfnUser.getHfnId(),
            hfnUser.getUserNm(),
            hfnUser.getPassword(),
            "adminPortal",
            "Hfn",
            "",
            "",
            authorities,
            hfnUser.getTmpPasswordYn(),
            hfnUser.getPwdChangeDt(),
            hfnUser.getPortalTosYn(),
            hfnUser.getPrivacyTosYn() ,
            hfnUser.getHfnCd(),
            hfnUser.getUserKey()
        );
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    //public boolean isEnabled() { return ENABLED; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(userKey, that.userKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userKey);
    }
}
