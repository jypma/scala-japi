package com.tradeshift.scalajapi.test;

import org.junit.Assert;

public class ScalaTestExampleTest extends ScalaTestBase {{
    describe("A test system", it -> {
        it.should("run individual specs", () -> {
            Assert.fail();
        });
        
        it.should("run another spec", () -> {
            Assert.fail();
        });
    });
}}
