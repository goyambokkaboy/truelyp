package com.ulyp.core.recorders;

import com.ulyp.core.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Finds {@link ObjectRecorder} that best matches for any given {@link Type}
 */
public class RecorderChooser {

    private static final RecorderChooser instance = new RecorderChooser();
    private static final ObjectRecorder[] empty = new ObjectRecorder[0];
    private static final ObjectRecorder[] allRecorders;

    static {
        allRecorders = new ObjectRecorder[ObjectRecorderRegistry.values().length];

        List<ObjectRecorderRegistry> objectRecorderTypes = new ArrayList<>();
        objectRecorderTypes.addAll(Arrays.asList(ObjectRecorderRegistry.values()));
        objectRecorderTypes.sort(Comparator.comparing(ObjectRecorderRegistry::getOrder));

        for (int i = 0; i < objectRecorderTypes.size(); i++) {
            allRecorders[i] = objectRecorderTypes.get(i).getInstance();
        }
    }

    public static RecorderChooser getInstance() {
        return instance;
    }

    public ObjectRecorder[] chooseForTypes(List<Type> paramsTypes) {
        try {
            if (paramsTypes.isEmpty()) {
                return empty;
            }
            ObjectRecorder[] convs = new ObjectRecorder[paramsTypes.size()];
            for (int i = 0; i < convs.length; i++) {
                convs[i] = chooseForType(paramsTypes.get(i));
            }
            return convs;
        } catch (Exception e) {
            throw new RuntimeException("Could not prepare converters for method params " + paramsTypes, e);
        }
    }

    public ObjectRecorder chooseForType(Type type) {
        for (ObjectRecorder recorder : allRecorders) {
            if (recorder.supports(type)) {
                return recorder;
            }
        }

        // Should never happen
        throw new RuntimeException("Could not find a suitable recorder for type " + type);
    }
}
