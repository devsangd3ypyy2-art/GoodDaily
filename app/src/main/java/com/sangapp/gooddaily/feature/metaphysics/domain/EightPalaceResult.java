package com.sangapp.gooddaily.feature.metaphysics.domain;

public final class EightPalaceResult {
    public final Trigram palace;
    public final FiveElement palaceElement;
    public final int generationIndex;
    public final String generationName;
    public final int shiLine;
    public final int yingLine;

    EightPalaceResult(Trigram palace, int generationIndex, String generationName, int shiLine) {
        this.palace = palace;
        this.palaceElement = palace.element;
        this.generationIndex = generationIndex;
        this.generationName = generationName;
        this.shiLine = shiLine;
        this.yingLine = ((shiLine + 2) % 6) + 1;
    }

    public String summary() {
        return "Cung " + palace.vietnamese + " · " + palaceElement.vietnamese
                + " · " + generationName + " · Thế hào " + shiLine + " / Ứng hào " + yingLine;
    }
}
