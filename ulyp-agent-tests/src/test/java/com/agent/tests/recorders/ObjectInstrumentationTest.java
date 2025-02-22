package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.NullObjectRecord;
import com.ulyp.core.recorders.NumberRecord;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.storage.CallRecord;
import org.junit.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class ObjectInstrumentationTest extends AbstractInstrumentationTest {

    @Test
    public void shouldPrintObjects() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(ObjectTestCases.class)
                        .withMethodToRecord("acceptsTwoObjects")
        );

        assertThat(root.getArgs(), hasSize(2));
        assertThat(root.getArgs().get(0), instanceOf(IdentityObjectRecord.class));
        assertThat(root.getArgs().get(1), instanceOf(IdentityObjectRecord.class));
    }

    @Test
    public void shouldChooseValidRecorderForJavaLangObjectAtRuntime() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(ObjectTestCases.class)
                        .withMethodToRecord("acceptsTwoObjects2")
        );

        assertThat(root.getArgs(), hasSize(2));
        assertThat(root.getArgs().get(0), instanceOf(StringObjectRecord.class));
        assertThat(root.getArgs().get(1), instanceOf(NumberRecord.class));
    }

    @Test
    public void shouldPrintNullArguments() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(ObjectTestCases.class)
                        .withMethodToRecord("acceptsTwoNulls")
        );

        assertThat(root.getArgs(), hasSize(2));
        assertThat(root.getArgs().get(0), instanceOf(NullObjectRecord.class));
        assertThat(root.getArgs().get(1), instanceOf(NullObjectRecord.class));
    }

    public static class ObjectTestCases {


        public static void main(String[] args) {
            new ObjectTestCases().acceptsTwoObjects(new Object(), new Object());
            new ObjectTestCases().acceptsTwoObjects2("asdasd", 34);
            new ObjectTestCases().acceptsTwoObjects3(new ObjectTestCases.X(), new ObjectTestCases.Y());
            new ObjectTestCases().acceptsTwoNulls(null, null);
        }

        public void acceptsTwoObjects(Object o1, Object o2) {
            System.out.println(o1);
            System.out.println(o2);
        }

        public void acceptsTwoObjects2(Object o1, Object o2) {
            System.out.println(o1);
            System.out.println(o2);
        }

        public void acceptsTwoObjects3(Object o1, Object o2) {
            System.out.println(o1);
            System.out.println(o2);
        }

        public void acceptsTwoNulls(Object o1, Object o2) {
            System.out.println(o1);
            System.out.println(o2);
        }

        private static class X {
        }

        private static class Y {
            public String toString() {
                return "Y{}";
            }
        }
    }
}
