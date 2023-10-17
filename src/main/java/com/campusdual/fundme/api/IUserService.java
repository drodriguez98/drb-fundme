package com.campusdual.fundme.api;

// Esta interfaz define métodos que deben ser implementados por un servicio que maneja operaciones relacionadas con usuarios.
// Incluye operaciones para consultar, insertar, actualizar y eliminar usuarios.

import com.campusdual.fundme.model.dto.UserDTO;
import java.util.List;

public interface IUserService {

    UserDTO getUser(UserDTO userDTO);
    List<UserDTO> getAllUsers();
    int insertUser (UserDTO userDTO);
    int updateUser (UserDTO userDTO);
    int deleteUser (UserDTO userDTO);

}