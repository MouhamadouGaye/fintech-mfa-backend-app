// package com.mgaye.banking_application.service;

// import com.mgaye.banking_application.entity.User;
// import com.mgaye.banking_application.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.util.Collection;
// import java.util.Collections;

// @Service
// @RequiredArgsConstructor
// public class UserDetailsServiceImpl implements UserDetailsService {

//     private final UserRepository userRepository;

//     @Override
//     @Transactional(readOnly = true)
//     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//         User user = userRepository.findByUsername(username)
//                 .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

//         return new UserDetailsImpl(user);
//     }

//     public static class UserDetailsImpl implements UserDetails {
//         private final User user;

//         public UserDetailsImpl(User user) {
//             this.user = user;
//         }

//         public Long getId() {
//             return user.getId();
//         }

//         @Override
//         public Collection<? extends GrantedAuthority> getAuthorities() {
//             return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
//         }

//         @Override
//         public String getPassword() {
//             return user.getPassword();
//         }

//         @Override
//         public String getUsername() {
//             return user.getUsername();
//         }

//         @Override
//         public boolean isAccountNonExpired() {
//             return true;
//         }

//         @Override
//         public boolean isAccountNonLocked() {
//             return user.getAccountLockedUntil() == null ||
//                     user.getAccountLockedUntil().isBefore(LocalDateTime.now());
//         }

//         @Override
//         public boolean isCredentialsNonExpired() {
//             return true;
//         }

//         @Override
//         public boolean isEnabled() {
//             return user.getIsActive() && user.getIsVerified();
//         }

//         public User getUser() {
//             return user;
//         }
//     }
// }

package com.mgaye.banking_application.service;

import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new UserDetailsImpl(user);
    }

    public static class UserDetailsImpl implements UserDetails {
        private final User user;

        public UserDetailsImpl(User user) {
            this.user = user;
        }

        public Long getId() {
            return user.getId();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return user.getAccountLockedUntil() == null ||
                    user.getAccountLockedUntil().isBefore(LocalDateTime.now());
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.getIsActive() && user.getIsVerified();
        }

        public User getUser() {
            return user;
        }
    }
}