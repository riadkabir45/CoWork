package com.aritra.d.riad.CoWork.repository;

import com.aritra.d.riad.CoWork.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    
    @Query("SELECT up FROM UserProfile up WHERE up.user.id = :userId")
    Optional<UserProfile> findByUserId(@Param("userId") String userId);
    
    @Query("SELECT up FROM UserProfile up WHERE up.user.email = :email")
    Optional<UserProfile> findByUserEmail(@Param("email") String email);
    
    @Query("SELECT up FROM UserProfile up WHERE up.isProfilePublic = true")
    java.util.List<UserProfile> findAllPublicProfiles();
}
