package com.example.cypher00;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MatchDao {

    @Query("SELECT * FROM `match`")
    List<Match> getAll();

    @Insert
    void insertAll(Match... matches);

    @Update
    void updateAll(Match... matches);

    @Delete
    void delete(Match match);

    @Query("DELETE FROM 'match'")
    void deleteAll();

}
