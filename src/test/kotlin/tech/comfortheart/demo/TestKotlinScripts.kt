package tech.comfortheart.demo

import org.jruby.RubyInstanceConfig
import org.jruby.embed.LocalContextScope
import org.jruby.embed.LocalVariableBehavior
import org.jruby.embed.PathType
import org.jruby.embed.ScriptingContainer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.luaj.vm2.lib.jse.JsePlatform
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource
import org.springframework.test.context.junit4.SpringRunner
import java.io.InputStreamReader
import java.math.BigDecimal
import javax.script.Invocable
import javax.script.ScriptContext
import javax.script.ScriptEngineManager
import org.mariuszgromada.math.mxparser.*;
import java.util.*
import org.luaj.vm2.LuaValue
import tech.comfortheart.utils.Timer
import javax.script.ScriptEngine
import org.luaj.vm2.lib.jse.LuajavaLib
import org.luaj.vm2.lib.PackageLib
import org.luaj.vm2.lib.jse.JseBaseLib
import org.luaj.vm2.Globals
import org.mariuszgromada.math.mxparser.Function
import java.util.concurrent.CountDownLatch


@RunWith(SpringRunner::class)
@SpringBootTest(classes = [DemoJava11WebfluxApplication::class])
class TestKotlinScripts {
    @Value("classpath:formula/MyFormula.kts")
    lateinit var myFormulaResource: Resource

    @Value("classpath:formula/MyFormula.rb")
    lateinit var rubyFormulaResource: Resource

    val predefinedRuby = "formula/PredefinedRuby.rb"
    val rubyFormulaPath = "formula/MyFormula.rb"

    @Autowired
    lateinit var context: ApplicationContext

    @Before
    fun init() {
        val container = ScriptingContainer(LocalContextScope.CONCURRENT, LocalVariableBehavior.PERSISTENT)
        container.compileMode = RubyInstanceConfig.CompileMode.JIT
        container.runScriptlet(PathType.CLASSPATH, predefinedRuby);
    }

    @Test
    fun testScript(){
//        val factory = ScriptEngineManager().getEngineByExtension("kts").factory
        val engine = ScriptEngineManager().getEngineByExtension("kts")!!


//        val myClass=KotlinToJVMBytecodeCompiler.compileScript(KotlinCoreEnvironment.createForProduction(configFiles = EnvironmentConfigFiles.JVM_CONFIG_FILES,
//                configuration = CompilerConfiguration.EMPTY,
//                parentDisposable = Disposable {  }),
//                context.classLoader)
//        myClass.

        engine.getBindings(ScriptContext.ENGINE_SCOPE).apply {
            put("amount", 33)
        }
        engine.eval(InputStreamReader(myFormulaResource.inputStream))
        val invocator = engine as? Invocable
        val startTime = System.nanoTime()
        var result = invocator!!.invokeFunction("twice", InsDec(12))
        result = invocator!!.invokeFunction("twice", result)
        val endTime = System.nanoTime()
        println("result is $result, calc in ${(endTime-startTime)/1000_000.0} milliseconds!")
    }

    @Test
    fun testMxParser() {
        val timer = Timer()
        var expression = Expression("x*2")
        expression.addArguments(Argument("x", 21.0))
        var res = expression.calculate()
        var elapsedTime = timer.elapsedMillisecs()
        println("Result is $res with $elapsedTime milliseconds")

        timer.reset()
        expression = Expression("y*2")
        expression.addArguments(Argument("y", 11.0))
        res = expression.calculate()
        elapsedTime = timer.elapsedMillisecs()
        println("Result is $res with $elapsedTime milliseconds")

        timer.reset()
        expression = Expression("y*2")
        expression.addArguments(Argument("y", 13.0))
        res = expression.calculate()
        elapsedTime = timer.elapsedMillisecs()
        println("Result is $res with $elapsedTime milliseconds")

        timer.reset()
        val f = Function("fact(x)=if(x>1, fact(x-1)*x, x)")
        expression = Expression("fact(10)", f)
        res = expression.calculate()
        elapsedTime = timer.elapsedMillisecs()
        println("Result is $res with $elapsedTime milliseconds")

        timer.reset()
        res = f.calculate(100.0)
        elapsedTime = timer.elapsedMillisecs()
        println("Result is $res with $elapsedTime milliseconds")


    }

    @Test
    fun testLua(){
        val mgr = ScriptEngineManager()
        val e = mgr.getEngineByName("luaj")
        e.eval("x=2")
        val timer = Timer()
        e.put("x", 25)
        e.eval("y = x*2")
        var y = e.get("y")
        var timeElapsed = timer.elapsedMillisecs()
        println("y=$y, in $timeElapsed milliseconds")

        timer.reset()
        e.put("x", 21)
        e.eval("y = x*2")
        y = e.get("y")
        timeElapsed = timer.elapsedMillisecs()
        println("y=$y, in $timeElapsed milliseconds")

        timer.reset()
        e.put("x", 13)
        e.eval("y = x*2")
        y = e.get("y")
        timeElapsed = timer.elapsedMillisecs()
        println("y=$y, in $timeElapsed milliseconds")

        val globals = JsePlatform.standardGlobals();
        globals.load(JseBaseLib())
        globals.load(PackageLib())
        globals.load(LuajavaLib())
        globals.load(
                "sys = luajava.bindClass('java.lang.System')\n" + "print ( sys:currentTimeMillis() )\n", "main.lua").call()
        globals.load("dec = luajava.bindClass('java.math.BigDecimal')\n" + "print( dec.new(12):pow(2))\n", "main.lua").call()
    }

    @Test
    fun testRuby() {
        System.setProperty("org.jruby.embed.compilemode", "jit")
        val mgr = ScriptEngineManager()
        val e = mgr.getEngineByName("jruby")
        e.eval("1+2")
        val timer = Timer()
        e.put("x", 25)
        e.eval("y = x*2")
        var y = e.get("y")
        var timeElapsed = timer.elapsedMillisecs()
        println("y=$y, in $timeElapsed milliseconds")

        val sourceStr = String(rubyFormulaResource.inputStream.readAllBytes())
        timer.reset()
        e.eval(sourceStr)
        y = e.get("res")
        timeElapsed = timer.elapsedMillisecs()
        println("y=$y, in $timeElapsed milliseconds")

        timer.reset()
        e.eval(sourceStr)
        y = e.get("res")
        timeElapsed = timer.elapsedMillisecs()
        println("y=$y, in $timeElapsed milliseconds")
    }

    /**
     * Concurrent: Avoid to update global variables/constants. Can use local variables freely.
     * This model isolates variable maps but shares Ruby runtime. A single ScriptingContainer created only one runtime and thread local variable maps. Global variables and top level variables/constants are not thread safe, but variables/constants tied to receiver objects are thread local.
     * +------------------+ +------------------+ +------------------+
     * |   Variable Map   | |   Variable Map   | |   Variable Map   |
     * +------------------+ +------------------+ +------------------+
     * +------------------------------------------------------------+
     * |                        Ruby runtime                        |
     * +------------------------------------------------------------+
     * +------------------------------------------------------------+
     * |                     ScriptingContainer                     |
     * +------------------------------------------------------------+
     * +------------------+ +------------------+ +------------------+
     * |   Java Thread    | |   Java Thread    | |   Java Thread    |
     * +------------------+ +------------------+ +------------------+
     * +------------------------------------------------------------+
     * |                         JVM                                |
     * +------------------------------------------------------------+
 *
     * Concurrent Local Context Type
     * (Per Thread Isolated Variable Map and Singleton Runtime)
     */
    @Test
    fun testRubyRedBridge() {
        val hey = { name: String ->
            val timerTt = Timer()
            val container = ScriptingContainer(LocalContextScope.CONCURRENT, LocalVariableBehavior.PERSISTENT)
            container.compileMode = RubyInstanceConfig.CompileMode.JIT
            container.runScriptlet(PathType.CLASSPATH, predefinedRuby);

            var timer = Timer()
            container.put("x", 21)
            var res = container.runScriptlet("2*x");
            var timeElapsed = timer.elapsedMillisecs()
            println("Thread $name, result is $res, in $timeElapsed milliseconds")

            timer.reset()
            container.put("x", 25)
            res = container.runScriptlet("2*x");
            timeElapsed = timer.elapsedMillisecs()
            println("Thread $name, result is $res, in $timeElapsed milliseconds")

            timer.reset()
            container.put("x", 53)
            res = container.runScriptlet("2*x");
            timeElapsed = timer.elapsedMillisecs()
            println("Thread $name, result is $res, in $timeElapsed milliseconds")


            timer.reset()
            container.put("givenAmount", "10")
            container.runScriptlet(PathType.CLASSPATH, rubyFormulaPath)
            var y = container.get("res")
            timeElapsed = timer.elapsedMillisecs()
            println("Thread $name, y1=$y, in $timeElapsed milliseconds")
            timer.reset()
            container.put("givenAmount", "11")
            container.runScriptlet(PathType.CLASSPATH, rubyFormulaPath)
            y = container.get("res")
            timeElapsed = timer.elapsedMillisecs()
            println("Thread $name, y2=$y, in $timeElapsed milliseconds")
            timer.reset()
            container.put("givenAmount", "12")
            container.runScriptlet(PathType.CLASSPATH, rubyFormulaPath)
            y = container.get("res")
            timeElapsed = timer.elapsedMillisecs()
            println("Thread $name, y3=$y, in $timeElapsed milliseconds")

            val totalTime = timerTt.elapsedMillisecs()
            println("Thread $name, Total $totalTime milliseconds elapsed!")
        }

        val countDownLatch = CountDownLatch(4)
        Thread {
            hey("1")
            countDownLatch.countDown()
        }.start()

        Thread {
            hey("2")
            countDownLatch.countDown()
        }.start()

        Thread {
            hey("3")
            countDownLatch.countDown()
        }.start()

        Thread {
            hey("4")
            countDownLatch.countDown()
        }.start()

        countDownLatch.await()
    }
}

class InsDec{
    var value:BigDecimal?

    constructor(i: Int) {
        value = BigDecimal(i)
    }

    constructor(i: BigDecimal) {
        value = i
    }

    operator fun times(i: Int): InsDec{
        return InsDec(value!!.multiply(BigDecimal(i)))
    }

    override fun toString(): String {
        return value.toString()
    }
}