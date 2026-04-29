package za.co.ultronsport.config.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import za.co.ultronsport.domain.AccountStatus;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.domain.UserRole;

public class SecurityUser implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final AccountStatus status;

    private SecurityUser(Long id, String email, String passwordHash, UserRole role, AccountStatus status) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
    }

    public static SecurityUser from(User user) {
        return new SecurityUser(user.getId(), user.getEmail(), user.getPasswordHash(), user.getRole(),
                user.getStatus());
    }

    public Long getId() {
        return id;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return status != AccountStatus.DELETED;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != AccountStatus.SUSPENDED && status != AccountStatus.DELETED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == AccountStatus.ACTIVE;
    }
}
