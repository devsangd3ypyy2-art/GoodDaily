package com.sangapp.gooddaily.feature.metaphysics.domain;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertTrue;

public class AdvancedMaiHoaInterpreterTest {
    @Test public void careerReading_containsTopicTechnicalAndTimingSections() {
        DivinationResult result = MaiHoaCalculator.fromNumbers(12, 7, 5,
                QuestionTopic.CAREER, "Công việc mới có phù hợp không?", Calendar.getInstance());
        assertTrue(result.interpretation.contains("Công việc"));
        assertTrue(result.technicalDetails.contains("THỂ"));
        assertTrue(result.timing.contains("THỜI GIAN"));
    }
}
