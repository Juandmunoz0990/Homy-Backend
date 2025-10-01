package co.edu.uniquindio.application.Services;

import java.util.Optional;

import co.edu.uniquindio.application.Dtos.User.Requests.SetUserProfileRequest;
import co.edu.uniquindio.application.Dtos.User.Responses.SetUserProfileResponse;
import co.edu.uniquindio.application.Models.User;

public interface UserService {
    User register(User u);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    SetUserProfileResponse setUserProfile (Long id, SetUserProfileRequest request);

}