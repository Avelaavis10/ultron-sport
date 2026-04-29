package za.co.ultronsport.service;

import java.util.List;
import za.co.ultronsport.domain.User;

public interface UserService {
    User getById(Long id);

    List<User> listAll();
}
