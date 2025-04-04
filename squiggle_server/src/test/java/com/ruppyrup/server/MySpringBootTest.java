package com.ruppyrup.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

@SpringBootTest
class MySpringBootTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void listAllBeans() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Arrays.sort(beanNames); // optional: sort alphabetically

        for (String name : beanNames) {
            System.out.println(name);
        }
    }
}
