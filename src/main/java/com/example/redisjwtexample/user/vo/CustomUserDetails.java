package com.example.redisjwtexample.user.vo;

import com.example.redisjwtexample.user.entity.UserEntity;
import lombok.*;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomUserDetails implements UserDetails, CredentialsContainer {

    private String username;
    private String password;

    public static CustomUserDetails fromEntity(@NonNull UserEntity userEntity) {
        return new CustomUserDetails(userEntity.getUserId(), userEntity.getPassword());
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

}
