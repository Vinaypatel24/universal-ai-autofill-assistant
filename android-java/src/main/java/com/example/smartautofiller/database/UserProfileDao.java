package com.example.smartautofiller.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.smartautofiller.model.UserProfile;

import java.util.List;

/**
 * Room Data Access Object (DAO) for UserProfile entities.
 */
@Dao
public interface UserProfileDao {

    @Query("SELECT * FROM user_profiles ORDER BY id ASC")
    LiveData<List<UserProfile>> getAllProfilesLive();

    @Query("SELECT * FROM user_profiles ORDER BY id ASC")
    List<UserProfile> getAllProfilesList();

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    UserProfile getProfileById(int id);

    @Query("SELECT * FROM user_profiles WHERE profile_name = :profileName LIMIT 1")
    UserProfile getProfileByName(String profileName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProfile(UserProfile profile);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UserProfile> profiles);

    @Update
    void updateProfile(UserProfile profile);

    @Delete
    void deleteProfile(UserProfile profile);

    @Query("DELETE FROM user_profiles")
    void deleteAllProfiles();
}
