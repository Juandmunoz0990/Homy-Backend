package co.edu.uniquindio.application.Services.impl;

import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.uniquindio.application.Dtos.User.Requests.SetUserProfileRequest;
import co.edu.uniquindio.application.Dtos.User.Responses.SetUserProfileResponse;
import co.edu.uniquindio.application.Models.User;
import co.edu.uniquindio.application.Repositories.UserRepository;
import co.edu.uniquindio.application.Services.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repo;

    public UserServiceImpl(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public User register(User u) {
        // Hash de contraseña simple (NO para producción): guardar en claro aquí por
        // simplicidad
        return repo.save(u);
    }

    @Override
    public User findByEmail(String email) {
        return repo.findByEmail(email).orElseThrow(() -> new ObjectNotFoundException("User with email: " + email +" not found", User.class));
    }

    @Override
    public User findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new ObjectNotFoundException("User with id: " + id + " not found" , User.class));
    }

    @Override
    public SetUserProfileResponse setUserProfile(Long id,SetUserProfileRequest request) {
        
        User user = repo.findById(id).orElseThrow(() -> new ObjectNotFoundException("User with id: " + id + " not found", User.class));
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());

        repo.save(user);

        SetUserProfileResponse response = new SetUserProfileResponse("The user profile has been updated succesfully");
        return response;
    }
}