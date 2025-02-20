package com.example.redisjwtexample.user.service;

import com.example.redisjwtexample.user.entity.UserEntity;
import com.example.redisjwtexample.user.repository.UserRepository;
import com.example.redisjwtexample.user.vo.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserEntity userInfo = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("not found loginId : " + userId));

        return CustomUserDetails.fromEntity(userInfo);
    }
}
