package com.aritra.d.riad.CoWork.repository;

import com.aritra.d.riad.CoWork.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {
    Optional<Users> findByEmail(String email);
    Optional<Users> findByGoogleId(String googleId);
}
