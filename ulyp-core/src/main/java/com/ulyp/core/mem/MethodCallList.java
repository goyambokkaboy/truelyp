package com.ulyp.core.mem;

import com.ulyp.core.*;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.RecorderType;
import com.ulyp.core.recorders.bytes.BinaryOutputForEnterRecordImpl2;
import com.ulyp.core.recorders.bytes.BinaryOutputForExitRecordImpl2;
import com.ulyp.transport.*;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MethodCallList implements Iterable<MethodCall> {

    private final BinaryOutputForEnterRecordImpl2 enterRecordBinaryOutput = new BinaryOutputForEnterRecordImpl2();
    private final BinaryOutputForExitRecordImpl2 exitRecordBinaryOutput = new BinaryOutputForExitRecordImpl2();
    private final BinaryEnterMethodCallEncoder enterMethodCallEncoder = new BinaryEnterMethodCallEncoder();
    private final BinaryExitMethodCallEncoder exitMethodCallEncoder = new BinaryExitMethodCallEncoder();
    private final BinaryList bytes;

    public MethodCallList() {
        bytes = new BinaryList();
    }

    public MethodCallList(BinaryList bytes) {
        this.bytes = bytes;
    }

    public void addExitMethodCall(
            long callId,
            Method method,
            TypeResolver typeResolver,
            boolean thrown,
            Object returnValue)
    {
        bytes.add(
                encoder -> {
                    MutableDirectBuffer wrappedBuffer = encoder.buffer();
                    encoder.id(BinaryExitMethodCallEncoder.TEMPLATE_ID);

                    int headerLength = 4;
                    int limit = encoder.limit();

                    exitMethodCallEncoder.wrap(wrappedBuffer, limit + headerLength);

                    exitMethodCallEncoder.callId(callId);
                    exitMethodCallEncoder.methodId(method.getId());
                    exitMethodCallEncoder.thrown(thrown ? BooleanType.T : BooleanType.F);
                    Type classDescription = typeResolver.get(returnValue);
                    exitMethodCallEncoder.returnValueTypeId(classDescription.getId());

                    ObjectRecorder printer = returnValue != null ?
                            method.getReturnValueRecorder() :
                            RecorderType.NULL_RECORDER.getInstance();

                    exitMethodCallEncoder.returnValueRecorderId(printer.getId());
                    exitRecordBinaryOutput.wrap(exitMethodCallEncoder);
                    try {
                        printer.write(returnValue, exitRecordBinaryOutput, typeResolver);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    int typeSerializedLength = exitMethodCallEncoder.encodedLength();
                    encoder.limit(limit + headerLength + typeSerializedLength);
                    wrappedBuffer.putInt(limit, typeSerializedLength, java.nio.ByteOrder.LITTLE_ENDIAN);
                }
        );
    }

    public void addEnterMethodCall(
            long callId,
            Method method,
            TypeResolver typeResolver,
            Object callee,
            Object[] args)
    {
        bytes.add(
                encoder -> {
                    MutableDirectBuffer wrappedBuffer = encoder.buffer();
                    encoder.id(BinaryEnterMethodCallEncoder.TEMPLATE_ID);

                    int headerLength = 4;
                    int limit = encoder.limit();

                    enterMethodCallEncoder.wrap(wrappedBuffer, limit + headerLength);

                    enterMethodCallEncoder.callId(callId);
                    enterMethodCallEncoder.methodId(method.getId());
                    ObjectRecorder[] paramPrinters = method.getParameterRecorders();

                    BinaryEnterMethodCallEncoder.ArgumentsEncoder argumentsEncoder = enterMethodCallEncoder.argumentsCount(args.length);

                    for (int i = 0; i < args.length; i++) {
                        ObjectRecorder recorder = args[i] != null ? paramPrinters[i] : RecorderType.NULL_RECORDER.getInstance();

                        Type argType = typeResolver.get(args[i]);

                        argumentsEncoder = argumentsEncoder.next();
                        argumentsEncoder.typeId(argType.getId());
                        argumentsEncoder.recorderId(recorder.getId());
                        enterRecordBinaryOutput.wrap(enterMethodCallEncoder);
                        try {
                            recorder.write(args[i], enterRecordBinaryOutput, typeResolver);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    ObjectRecorder recorder = callee != null ? RecorderType.IDENTITY_RECORDER.getInstance() : RecorderType.NULL_RECORDER.getInstance();

                    enterMethodCallEncoder.calleeTypeId(typeResolver.get(callee).getId());
                    enterMethodCallEncoder.calleeRecorderId(recorder.getId());
                    enterRecordBinaryOutput.wrap(enterMethodCallEncoder);
                    try {
                        recorder.write(callee, enterRecordBinaryOutput, typeResolver);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    int typeSerializedLength = enterMethodCallEncoder.encodedLength();
                    encoder.limit(limit + headerLength + typeSerializedLength);
                    wrappedBuffer.putInt(limit, typeSerializedLength, java.nio.ByteOrder.LITTLE_ENDIAN);
                }
        );
    }

    public int size() {
        return bytes.size();
    }

    public BinaryList getRawBytes() {
        return bytes;
    }

    public AddressableItemIterator<BinaryDataDecoder> binaryIterator() {
        return bytes.iterator();
    }

    public Stream<MethodCall> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

    @NotNull
    @Override
    public AddressableItemIterator<MethodCall> iterator() {
        AddressableItemIterator<BinaryDataDecoder> iterator = bytes.iterator();
        return new AddressableItemIterator<MethodCall>() {
            @Override
            public long address() {
                return iterator.address();
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public MethodCall next() {
                BinaryDataDecoder decoder = iterator.next();
                UnsafeBuffer buffer = new UnsafeBuffer();
                decoder.wrapValue(buffer);
                if (decoder.id() == BinaryEnterMethodCallEncoder.TEMPLATE_ID) {
                    BinaryEnterMethodCallDecoder enterMethodCallDecoder = new BinaryEnterMethodCallDecoder();
                    enterMethodCallDecoder.wrap(buffer, 0, BinaryEnterMethodCallEncoder.BLOCK_LENGTH, 0);
                    return EnterMethodCall.deserialize(enterMethodCallDecoder);
                } else {
                    BinaryExitMethodCallDecoder exitMethodCallDecoder = new BinaryExitMethodCallDecoder();
                    exitMethodCallDecoder.wrap(buffer, 0, BinaryExitMethodCallDecoder.BLOCK_LENGTH, 0);
                    return ExitMethodCall.deserialize(exitMethodCallDecoder);
                }
            }
        };
    }
}
