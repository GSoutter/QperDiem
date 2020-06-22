package io.gogz.qperdiem.room_db;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import java.util.List;

import io.gogz.qperdiem.room_db.Question;
import io.gogz.qperdiem.room_db.Rating;

public class QuestionWithRatings {
    @Embedded public Question question;

    @Relation(
            parentColumn = "id",
            entityColumn = "questionId",
            entity = Rating.class
    )
    public List<Rating> ratings;

}
