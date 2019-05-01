package test;

import groovy.lang.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import tech.comfortheart.demo.DemoJava11WebfluxApplication;
import tech.comfortheart.utils.Timer;

import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DemoJava11WebfluxApplication.class})
public class TestGroovy {
    @Value("classpath:formula/MyFormula.groovy")
    Resource myFormulaGroovy;
    @Test
    public void testGroovy() throws IOException, IllegalAccessException, InstantiationException {

        GroovyClassLoader classLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
        File sourceFile = myFormulaGroovy.getFile();
        Class testGroovyClass = classLoader.parseClass(new GroovyCodeSource(sourceFile));
        GroovyObject instance = (GroovyObject)testGroovyClass.newInstance();//proxy
        long start = System.nanoTime();
        var result = instance.invokeMethod("twice", 21);
        long end = System.nanoTime();
        System.out.println("Got result: " + result + ", calculate in " + (end-start)/1000000.0 + " milliseconds");
    }

    @Test
    public void testGroovyScript() throws IOException {
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding);
        var scriptStr = new String(myFormulaGroovy.getInputStream().readAllBytes()).trim();
        long start = System.nanoTime();
        binding.setVariable("x", new Integer(21));
        shell.evaluate(scriptStr);
        var myAmount = shell.getVariable("y");
        var z = shell.getVariable("z");
        long end = System.nanoTime();
        System.out.println("Got result: " + myAmount + ", calculate in " + (end-start)/1000000.0 + " milliseconds");
        System.out.println("Got z: " + z + ", calculate in " + (end-start)/1000000.0 + " milliseconds");

        start = System.nanoTime();
        binding.setVariable("x", new Integer(21));
        shell.evaluate("y=x*2");
        myAmount = shell.getVariable("y");
        end = System.nanoTime();
        System.out.println("Got result: " + myAmount + ", calculate in " + (end-start)/1000000.0 + " milliseconds");

    }

    @Test
    public void testGroovyScript1(){
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding);

        var timer = new Timer();
        binding.setVariable("x", 21);
        shell.evaluate("y=x*2");
        var myAmount = shell.getVariable("y");
        var timeElapsed = timer.elapsedMillisecs();
        System.out.println("Got result: " + myAmount + ", calculate in " + timeElapsed + " milliseconds");

    }
}
