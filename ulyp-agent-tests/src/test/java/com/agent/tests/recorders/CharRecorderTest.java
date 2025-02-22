package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.CharObjectRecord;
import com.ulyp.storage.CallRecord;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CharRecorderTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordFileObject() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnChar")
        );

        CharObjectRecord value = (CharObjectRecord) root.getReturnValue();

        assertEquals('A', value.getValue());
    }

    public static class TestCase {

        public static void main(String[] args) {
            System.out.println(returnChar());
        }

        public static char returnChar() {
            return 'A';
        }
    }
}
