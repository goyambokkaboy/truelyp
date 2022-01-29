package com.test.recorders;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.recorders.*;
import com.ulyp.storage.CallRecord;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ObjectArrayRecorderTest extends AbstractInstrumentationTest {

    public static class TakesEmptyObjectArray {

        public static void main(String[] args) {
            new TakesEmptyObjectArray().accept(new Object[]{});
        }

        public void accept(Object[] array) {
        }
    }

    @Test
    public void shouldProvideArgumentTypes() {
        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TakesEmptyObjectArray.class)
                        .setMethodToRecord("accept")
        );


        ObjectArrayRecord objectRepresentation = (ObjectArrayRecord) root.getArgs().get(0);


        assertThat(objectRepresentation.getLength(), is(0));
        assertThat(objectRepresentation.getRecordedItems(), Matchers.empty());
    }

    public static class TakesStringArrayWithSomeString {

        public static void main(String[] args) {
            new TakesStringArrayWithSomeString().accept(new String[]{
                    "sddsad",
                    "zx",
                    "sdsd"
            });
        }

        public void accept(String[] array) {
        }
    }

    @Test
    public void shouldRecordSimpleArrayWithString() {
        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TakesStringArrayWithSomeString.class)
                        .setMethodToRecord("accept")
        );


        ObjectArrayRecord objectRepresentation = (ObjectArrayRecord) root.getArgs().get(0);


        assertThat(objectRepresentation.getLength(), is(3));

        List<ObjectRecord> items = objectRepresentation.getRecordedItems();

        assertThat(items, Matchers.hasSize(3));

        StringObjectRecord str0 = (StringObjectRecord) items.get(0);
        assertEquals(str0.value(), "sddsad");

        StringObjectRecord str1 = (StringObjectRecord) items.get(1);
        assertEquals(str1.value(), "zx");

        StringObjectRecord str2 = (StringObjectRecord) items.get(2);
        assertEquals(str2.value(), "sdsd");
    }

    public static class TakesVariousItemsArray {

        public static void main(String[] args) {
            new TakesVariousItemsArray().accept(new Object[]{
                    new X(),
                    664,
                    Object.class,
                    "asdd",
                    new X()
            });
        }

        public void accept(Object[] array) {
            System.out.println(array);
        }
    }

    private static class X {
        public X() {
        }
    }

    @Test
    public void testUserDefinedClassArrayWith3Elements() {
        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TakesVariousItemsArray.class)
                        .setMethodToRecord("accept")
        );


        ObjectArrayRecord objectRepresentation = (ObjectArrayRecord) root.getArgs().get(0);

        assertThat(objectRepresentation.getLength(), is(5));

        List<ObjectRecord> items = objectRepresentation.getRecordedItems();

        IdentityObjectRecord arg0 = (IdentityObjectRecord) items.get(0);
        assertThat(arg0.getType().getName(), Matchers.is(X.class.getName()));

        NumberRecord arg1 = (NumberRecord) items.get(1);
        assertThat(arg1.getNumberPrintedText(), is("664"));

        ClassObjectRecord arg4 = (ClassObjectRecord) items.get(2);
        assertThat(arg4.getCarriedType().getName(), is(Object.class.getName()));
    }

    public static class VaragsTestCase {

        public static void main(String[] args) {
            takeVararg(new Object[] {Byte[].class, String.class, Integer.class, int[].class, int.class});
        }

        private static void takeVararg(Object[] commonClasses) {
            for (Object b : commonClasses) {
                System.out.println(b);
            }
        }
    }

    @Test
    public void testVarargs() {
        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(VaragsTestCase.class)
                        .setMethodToRecord("takeVararg")
        );


        ObjectArrayRecord arrayRecord = (ObjectArrayRecord) root.getArgs().get(0);

        Assert.assertThat(arrayRecord.getRecordedItems().get(0), Matchers.instanceOf(ClassObjectRecord.class));
        Assert.assertThat(arrayRecord.getRecordedItems().get(1), Matchers.instanceOf(ClassObjectRecord.class));
        Assert.assertThat(arrayRecord.getRecordedItems().get(2), Matchers.instanceOf(ClassObjectRecord.class));
    }
}
