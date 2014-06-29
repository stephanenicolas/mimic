package com.github.stephanenicolas.mimic.sample;

import com.github.stephanenicolas.mimic.annotations.Mimic;
import com.github.stephanenicolas.mimic.annotations.MimicMethod;
import com.github.stephanenicolas.mimic.annotations.MimicMode;

@Mimic(sourceClass = ExampleTemplate.class,
	mimicMethods = {@MimicMethod(methodName="doOtherStuff",mode=MimicMode.AT_BEGINNING)}
)
public class Example extends ExampleAncestor {

    public Example() {
    }

    @Override
    public void doStuff() {
        super.doStuff();
    }

    public void doOtherStuff() {
    }
}
