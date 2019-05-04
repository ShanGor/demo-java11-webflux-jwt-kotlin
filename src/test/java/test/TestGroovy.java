package test;

import groovy.lang.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import tech.comfortheart.demo.DemoJava11WebfluxApplication;
import tech.comfortheart.utils.Timer;

import javax.script.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DemoJava11WebfluxApplication.class})
public class TestGroovy {
    @Value("classpath:formula/MyFormula.groovy")
    Resource myFormulaGroovy;
    GroovyClassLoader classLoader;
    File sourceFile;
    Class testGroovyClass;

    @Value("classpath:formula/PreRun.groovy")
    Resource preRunGroovy;

    @Value("classpath:formula/Decimal.groovy")
    Resource decimalGroovy;

    // 获取脚本引擎
    final ScriptEngineManager factory = new ScriptEngineManager();
    final Compilable groovyEngine = (Compilable) factory.getEngineByName("groovy");

    @Before
    public void init() throws IOException, IllegalAccessException, InstantiationException, ScriptException {
        classLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());


        sourceFile = myFormulaGroovy.getFile();
        testGroovyClass = classLoader.parseClass(new GroovyCodeSource(sourceFile));
        GroovyObject instance = (GroovyObject)testGroovyClass.newInstance();

        instance.invokeMethod("twice", 21);


        // Load the Decimal.groovy into the engine.
        getCompiledScript(decimalGroovy, groovyEngine).eval();

    }

    @Test
    public void testGroovy() throws IOException, IllegalAccessException, InstantiationException {
        Timer timer = new Timer();
        GroovyObject instance = (GroovyObject)testGroovyClass.newInstance();//proxy

        var result = instance.invokeMethod("twice", 21);
        var elapsed = timer.elapsedMillisecs();
        System.out.println("Got result: " + result + ", calculate in " + elapsed + " milliseconds");

        timer.reset();
        result = instance.invokeMethod("twice", 31);
        elapsed = timer.elapsedMillisecs();
        System.out.println("Got result: " + result + ", calculate in " + elapsed + " milliseconds");

        timer.reset();
        result = instance.invokeMethod("triple", 31);
        elapsed = timer.elapsedMillisecs();
        System.out.println("Got result: " + result + ", calculate in " + elapsed + " milliseconds");
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

    @Test
    public void testJSR223Compiled() throws ScriptException, InterruptedException {
        final CompiledScript script = getCompiledScript(myFormulaGroovy, groovyEngine);
        final CompiledScript scriptInitVariables = getCompiledScript(preRunGroovy, groovyEngine);

        final Bindings bindings = new SimpleBindings();
        // Set default variables
        scriptInitVariables.eval(bindings);
        // Dry run once. to warm up the script.
        Timer timer = new Timer();
        script.eval(bindings);
        System.out.println("Warm up time " + timer.elapsedMillisecs() + " milliseconds");

        CountDownLatch latch = new CountDownLatch(4);
        for (int i=1; i<5; i++) {
            final BigDecimal threadName = new BigDecimal(i);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        forJSR223Compiled(script, threadName);
                        forJSR223Compiled(script, threadName.add(BigDecimal.TEN));
                    } catch (ScriptException e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            }).start();
        }

        latch.await();
    }

    private void forJSR223Compiled(CompiledScript script, BigDecimal name) throws ScriptException {
        Timer timer = new Timer();
        final Bindings bindings = new SimpleBindings();
        bindings.put("x", name);
        script.eval(bindings);
        var z = bindings.get("z");
        var f = bindings.get("f");
        var elapsed = timer.elapsedMillisecs();
        System.out.println("Thread " + name + ", with z=" + z + ", f=" + f + " in " + elapsed + " milliseconds!");
    }

    public static final CompiledScript getCompiledScript(final Resource resource, final Compilable compilable) throws ScriptException {
        try(InputStream inputStream = resource.getInputStream();
            Reader reader = new InputStreamReader(inputStream)) {
            return compilable.compile(reader);
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }
}
