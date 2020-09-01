package com.qr.app.backend.Json;


import com.qr.app.backend.entity.Mark;

import java.util.List;

public class Marks {

    private List<Mark> marks;

    public List<Mark> getMarks () {
        return marks;
    }

    public void setMarks (List<Mark> marks) {
        this.marks = marks;
    }

    public Marks () {
    }

    public Marks (List<Mark> marks) {
        this.marks = marks;
    }
}
