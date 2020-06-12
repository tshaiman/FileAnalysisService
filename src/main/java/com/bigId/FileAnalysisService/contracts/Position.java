package com.bigId.FileAnalysisService.contracts;

import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Position implements Comparable<Position>{
    private int lineNumber;
    private int charOffset;

    @Override
    public int compareTo(Position o) {
        if ( this.lineNumber != o.lineNumber )
            return this.lineNumber - o.lineNumber;
        return this.charOffset - o.charOffset;
    }
}
