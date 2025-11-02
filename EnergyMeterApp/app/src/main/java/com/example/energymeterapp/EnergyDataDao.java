package com.example.energymeterapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface EnergyDataDao {

    /**
     * Insert a new energy reading into the database.
     * On conflict (same primary key), the old entry is replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EnergyData data);

    /**
     * Get the last 100 energy readings in ascending order (oldest first).
     */
    @Query("SELECT * FROM EnergyData ORDER BY timestamp ASC LIMIT 100")
    List<EnergyData> getLast100Entries();

    /**
     * Optional: LiveData version to observe changes in real-time.
     */
    @Query("SELECT * FROM EnergyData ORDER BY timestamp ASC LIMIT 100")
    LiveData<List<EnergyData>> getLast100EntriesLive();

    /**
     * Keep only the latest 1000 entries in the database,
     * older entries will be deleted automatically.
     */
    @Query("DELETE FROM EnergyData WHERE id NOT IN (SELECT id FROM EnergyData ORDER BY timestamp DESC LIMIT 1000)")
    void deleteOldReadings();
}
