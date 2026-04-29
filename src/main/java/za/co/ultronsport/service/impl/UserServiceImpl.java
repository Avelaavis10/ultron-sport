package za.co.ultronsport.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import za.co.ultronsport.common.error.ResourceNotFoundException;
import za.co.ultronsport.domain.User;
import za.co.ultronsport.repository.UserRepository;
import za.co.ultronsport.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Override
    public List<User> listAll() {
        return userRepository.findAll();
    }
}
