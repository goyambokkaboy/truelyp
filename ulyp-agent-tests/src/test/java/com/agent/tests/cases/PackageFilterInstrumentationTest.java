package com.agent.tests.cases;

import com.agent.tests.cases.a.A;
import com.agent.tests.cases.a.c.C;
import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.storage.CallRecord;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PackageFilterInstrumentationTest extends AbstractInstrumentationTest {

    @Test
    public void shouldInstrumentAndTraceAllClasses() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(A.class)
                        .setInstrumentedPackages("com.agent.tests.cases.a")
        );

        assertThat(root.getMethodName(), is("main"));
        assertThat(root.getClassName(), is(A.class.getName()));
        assertThat(root.getChildren(), Matchers.hasSize(2));
    }

    @Test
    public void shouldExcludeInstrumentationPackage() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(A.class)
                        .setInstrumentedPackages("com.agent.tests.cases.a")
                        .setExcludedFromInstrumentationPackages("com.agent.tests.cases.a.b")
        );

        assertThat(root.getMethodName(), is("main"));
        assertThat(root.getClassName(), is("com.agent.tests.cases.a.A"));
        assertThat(root.getChildren(), Matchers.hasSize(1));

        CallRecord callRecord = root.getChildren().get(0);

        assertThat(callRecord.getClassName(), is(C.class.getName()));
        assertThat(callRecord.getMethodName(), is("c"));
    }

    @Test
    public void shouldExcludeTwoPackages() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(A.class)
                        .setInstrumentedPackages("com.agent.tests.cases.a")
                        .setExcludedFromInstrumentationPackages("com.agent.tests.cases.a.b", "com.agent.tests.cases.a.c")
        );

        assertThat(root.getMethodName(), is("main"));
        assertThat(root.getClassName(), is("com.agent.tests.cases.a.A"));
        assertThat(root.getChildren(), Matchers.empty());
    }
}
