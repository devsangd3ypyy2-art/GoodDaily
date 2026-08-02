package com.sangapp.gooddaily.feature.metaphysics.domain;

import java.util.Arrays;

public final class DivinationResult {
    public final HexagramInfo base;
    public final HexagramInfo changed;
    public final HexagramInfo nuclear;
    public final int[] movingLines;
    public final int[] lineValues;
    public final String bodyUse;
    public final String elementRelation;
    public final String interpretation;
    public final String technicalDetails;
    public final String timing;
    public final String confidence;

    public DivinationResult(HexagramInfo base, HexagramInfo changed, HexagramInfo nuclear,
                            int[] movingLines, int[] lineValues, String bodyUse,
                            String elementRelation, String interpretation) {
        this(base, changed, nuclear, movingLines, lineValues, bodyUse,
                elementRelation, interpretation, "", "", "");
    }

    public DivinationResult(HexagramInfo base, HexagramInfo changed, HexagramInfo nuclear,
                            int[] movingLines, int[] lineValues, String bodyUse,
                            String elementRelation, String interpretation,
                            String technicalDetails, String timing, String confidence) {
        this.base = base;
        this.changed = changed;
        this.nuclear = nuclear;
        this.movingLines = movingLines == null ? new int[0] : Arrays.copyOf(movingLines, movingLines.length);
        this.lineValues = lineValues == null ? new int[0] : Arrays.copyOf(lineValues, lineValues.length);
        this.bodyUse = bodyUse == null ? "" : bodyUse;
        this.elementRelation = elementRelation == null ? "" : elementRelation;
        this.interpretation = interpretation == null ? "" : interpretation;
        this.technicalDetails = technicalDetails == null ? "" : technicalDetails;
        this.timing = timing == null ? "" : timing;
        this.confidence = confidence == null ? "" : confidence;
    }

    public String movingLinesText() {
        if (movingLines.length == 0) return "Không có hào động";
        StringBuilder builder = new StringBuilder("Hào động: ");
        for (int i = 0; i < movingLines.length; i++) {
            if (i > 0) builder.append(", ");
            builder.append(movingLines[i]);
        }
        return builder.toString();
    }

    public String fullInterpretation() {
        StringBuilder b = new StringBuilder(interpretation);
        if (!technicalDetails.isEmpty()) b.append("\n\n").append(technicalDetails);
        if (!timing.isEmpty()) b.append("\n\n").append(timing);
        if (!confidence.isEmpty()) b.append("\n\n").append(confidence);
        return b.toString();
    }
}
