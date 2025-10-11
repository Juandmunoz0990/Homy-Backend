package co.edu.uniquindio.application.Services;


import co.edu.uniquindio.application.Dtos.User.Requests.SetUserProfileRequest;
import co.edu.uniquindio.application.Dtos.User.Responses.SetUserProfileResponse;
import co.edu.uniquindio.application.Dtos.auth.RegisterRequest;
import co.edu.uniquindio.application.Models.User;

public interface UserService {
    User register(RegisterRequest u);

    User findByEmail(String email);

    User findById(Long id);

    SetUserProfileResponse setUserProfile (Long id, SetUserProfileRequest request);

}