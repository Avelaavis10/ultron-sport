package za.co.ultronsport.config.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import za.co.ultronsport.repository.UserRepository;

@Service
public class UltronUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public UltronUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmailIgnoreCase(username)
                .map(SecurityUser::from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }
}
