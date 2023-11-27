package org.example.exceptions;

import java.lang.annotation.Annotation;

public class MissingAnnotationException extends RuntimeException {

    public MissingAnnotationException(Class<? extends Annotation> a, String missingOn) {
        super(String.format("Expected annotation of type %s on %s", a.getName(), missingOn));
    }
}
