### mimic  <img src='https://raw.githubusercontent.com/stephanenicolas/mimic/master/assets/mimic_logo.jpg' alt='mimic logo' width='150px'/> 

[![Coverage Status](https://img.shields.io/coveralls/stephanenicolas/mimic.svg)](https://coveralls.io/r/stephanenicolas/mimic?branch=master)
[![Travis Build](https://travis-ci.org/stephanenicolas/mimic.svg?branch=master)](https://travis-ci.org/stephanenicolas/mimic)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.stephanenicolas.mimic/mimic/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.stephanenicolas.mimic/mimic)


Mimicing is, indeed, kind of way to bypass java  single inheritance paradigm. It allows to copy all declared fields,  constructors and methods in a given class into another class.

Mutliple plugins can be used to trigger AfterBurner on maven and gradle : 

* [for gradle](https://github.com/darylteo/gradle-plugins)
* [for maven](https://github.com/icon-Systemhaus-GmbH/javassist-maven-plugin)

An imperative equivalent of Mimic is available on GitHub : [AfterBurner](https://github.com/stephanenicolas/afterburner).


## Example usage

You can find a sample in our repo.

Let's say we got a class `Example` and its base class `ExampleAncestor` : 

```java
public class ExampleAncestor {

    public void doStuff() {
    }
}

@Mimic(sourceClass = ExampleTemplate.class,
	mimicMethods = {@MimicMethod(methodName="doOtherStuff",mode=MimicMode.AT_BEGINNING)}
)
public class Example extends ExampleAncestor {

    @Override
    public void doStuff() {
        super.doStuff();
    }

    public void doOtherStuff() {
    }
}
```

As you can see, without Mimic, this class would basically do nothing.

BUT, as we used the `Mimic` annotations, it will receive code from the following template : 

```java
public class ExampleTemplate {

    public ExampleTemplate() {
        System.out.println("Inside constructor");
    }

    public void doStuff() {
        System.out.println("Inside doStuff");
    }

    public void doOtherStuff() {
    	System.out.println("Inside doOtherStuff");
    }
}
```

This means that : 

1. the constructor of `Example` will receive the code of the constructor of `ExampleTemplate`
2. the method `doStuff` of `Example` will receive the code of the method `doStuff` of `ExampleTemplate`, after its call to `super.doStuff` (default mimic mode for methods).
3. the method `doOtherStuff` of `Example` will receive the code of the method `doOtherStuff` of `ExampleTemplate`, at the beginning of its body.

The resulting `Example` class will print : 

```bash
Inside constructor
Inside doStuff
Inside doOtherStuff
```

## Mimicing constructors and fields

Currently mimic supports : 

* copying the code of each constructor of the source class into all constructors of the target class.
* copying all fields of the source class to the target class. In case a field exists in both class, an exception will be thrown.
* there are multiple modes to copy methods. See `MimicMode` class.

## Enable Mimic in maven builds 

Simply add the following to your maven build : 

```xml
	<dependencies>
		...
		<dependency>
			<groupId>com.github.stephanenicolas.mimic</groupId>
			<artifactId>mimic-annotations</artifactId>
			<version>${mimic-library.version}</version>
			<scope>provided</scope>			
		</dependency>
		...
	</dependencies>
	
	<build>
		<plugins>
      ...
			<plugin>
				<groupId>de.icongmbh.oss.maven.plugins</groupId>
				<artifactId>javassist-maven-plugin</artifactId>
				<version>${javassist-maven-plugin.version}</version>
				<configuration>
					<includeTestClasses>false</includeTestClasses>
					<transformerClasses>
						<transformerClass>
							<className>com.github.stephanenicolas.mimic.MimicProcessor</className>
						</transformerClass>
					</transformerClasses>
				</configuration>
				<executions>
					<execution>
						<phase>process-classes</phase>
						<goals>
							<goal>javassist</goal>
						</goals>
					</execution>
				</executions>
				<dependencies>
					<dependency>
						<groupId>com.github.stephanenicolas.mimic</groupId>
						<artifactId>mimic-library</artifactId>
						<version>${mimic-library.version}</version>
					</dependency>
				</dependencies>
			</plugin>
			...
			</plugins>
	</build>			
```

Mimic will not add any byte to your app, but it will cut down boiler plate.
