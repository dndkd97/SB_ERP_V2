package com.sb.erp.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.sb.erp.dto.AuthUserDto;
import com.sb.erp.dto.EmpAuthDto;
import com.sb.erp.dto.EmpDto;

import lombok.Getter;

@Getter   
public class CustomUserDetails implements UserDetails{
   
   private static final long serialVersionUID = 1L;
   private EmpDto user;
   private AuthUserDto authDto;
   private Map<String,Object> attributes = new HashMap<>();
   ////////////////////////////////////////////////1.일반 로그인
   public CustomUserDetails(EmpDto user, AuthUserDto authDto) {
         super();
         this.user = user;
         this.authDto = authDto;
         this.attributes.put("empId"    , user.getEmpId());
         this.attributes.put("comId" , user.getComId());
   }

     @Override
      public Collection<? extends GrantedAuthority> getAuthorities() { 
         if( authDto ==null ||   authDto.getAuthList()  == null ||  authDto.getAuthList().isEmpty() ) {
             return List.of( new SimpleGrantedAuthority("ROLE_MEMBER") );
          }  // 권한없으면 ROLE_MEMBER 
      
         return authDto.getAuthList().stream()
                 .filter( a->a.getAutName() != null  &&  !a.getAutName().isBlank() )
                 .map(    a-> new SimpleGrantedAuthority(a.getAutName()))
                 .collect(Collectors.toList());
        }
   @Override public String getPassword() {  return user.getEmpPass(); }

   @Override public String getUsername() {  return user.getEmpEmail(); }
   
   public String getEmpNo() {return user.getEmpNo();}
   public String getEmpName() {return user.getEmpName();}
   public String getEmpEmail() {return user.getEmpEmail();}

}
