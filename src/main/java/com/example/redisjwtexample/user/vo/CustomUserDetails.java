package com.example.redisjwtexample.user.vo;

import com.example.redisjwtexample.user.entity.UserEntity;
import lombok.*;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Stream;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomUserDetails implements UserDetails, CredentialsContainer {

    private String username;
    private String password;
    private String role;

    public static CustomUserDetails fromEntity(@NonNull UserEntity userEntity) {
        return new CustomUserDetails(userEntity.getUserId(), userEntity.getPassword(), userEntity.getRole());
    }

    public static CustomUserDetails of(String userId, String role) {
        return new CustomUserDetails(userId, null, role);
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return Stream.of(this.role)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }


}
