package com.campusdual.fundme.service;

// Esta clase implementa la interfaz IUserService y proporciona la lógica de negocio para manejar operaciones relacionadas con usuarios.
// Se encarga de transformar objetos UserDTO en objetos User y viceversa.
// Utiliza un servicio para codificar contraseñas antes de almacenarlas en la base de datos.

import com.campusdual.fundme.api.IUserService;
import com.campusdual.fundme.model.dao.UserRepository;
import com.campusdual.fundme.model.dto.UserDTO;
import com.campusdual.fundme.model.User;
import com.campusdual.fundme.model.dto.dtopmapper.UserMapper;
import com.campusdual.fundme.util.PasswordEncoderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("UserService")
@Lazy
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoderUtil passwordEncoderUtil;

    // Obtiene el nombre de usuario del usuario autenticado desde el contexto de seguridad
    // Utiliza el nombre de usuario para buscar al usuario autenticado en el repositorio
    // Convierte el usuario en UserDTO y lo devuelve

    @Override
    public UserDTO getUser(UserDTO userDTO) {

        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(authenticatedUsername);

        return UserMapper.INSTANCE.toDTO(user);
    }


    @Override
    public List<UserDTO> getAllUsers() { return UserMapper.INSTANCE.toDTOList(userRepository.findAll()); }

    // Genera un hash BCrypt para la contraseña antes de almacenarla.
    // Establece la hora y fecha actual.
    // Establece los campos active a 1 y admin a 0 por defecto

    @Override
    public int insertUser (UserDTO userDTO) {

        User user = UserMapper.INSTANCE.toEntity(userDTO);

        user.setDate_added(new Date());

        user.setActive(true);
        user.setAdmin(false);

        String hashedPassword = passwordEncoderUtil.encodePassword(user.getPassword());
        user.setPassword(hashedPassword);

        this.userRepository.saveAndFlush(user);

        return user.getUser_id();

    }

    @Override
    public int updateUser (UserDTO userDTO) { return this.insertUser(userDTO); }

    @Override
    public int deleteUser (UserDTO userDTO) {

        int id = userDTO.getUser_id();
        User user = UserMapper.INSTANCE.toEntity(userDTO);
        this.userRepository.delete(user);
        return id;

    }

}