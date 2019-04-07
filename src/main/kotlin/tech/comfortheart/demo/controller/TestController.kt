package tech.comfortheart.demo.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.comfortheart.demo.service.JwtGeneratorService

@RestController
class TestController {
    @Autowired
    lateinit var jwtGeneratorService: JwtGeneratorService

    @RequestMapping("/api/test")
    fun test(): String {
        return "hello world"
    }

    @RequestMapping("/jwt")
    fun generateJwt(): String {
        return jwtGeneratorService.generateJwt();
    }
}