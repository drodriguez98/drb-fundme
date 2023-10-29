package com.campusdual.fundme.model.repository;

import com.campusdual.fundme.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUsername(String username);

    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.userId = :userId")
    Integer getTotalDonationsByUser(@Param("userId") User user);

}