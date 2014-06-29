package com.github.stephanenicolas.mimic.sample;

import com.github.stephanenicolas.mimic.annotations.Mimic;

@Mimic(sourceClass = ExampleTemplate.class)
public class Example extends ExampleAncestor {

    public Example() {
    }

    @Override
    public void doStuff() {
        super.doStuff();
    }
}
