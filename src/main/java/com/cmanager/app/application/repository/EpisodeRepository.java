package com.cmanager.app.application.repository;

import com.cmanager.app.application.data.SeasonRatingProjection;
import com.cmanager.app.application.domain.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, String> {

    @Query("""
            SELECT new com.cmanager.app.application.data.SeasonRatingProjection(
                e.season,
                AVG(e.rating)
            )
            FROM Episode e
            WHERE e.show.id = :showId
            GROUP BY e.season
            ORDER BY e.season
            """)
    List<SeasonRatingProjection> findAverageRatingBySeasonAndShowId(@Param("showId") String showId);
}
