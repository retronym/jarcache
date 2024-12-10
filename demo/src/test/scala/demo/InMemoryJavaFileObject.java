package demo;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

class InMemoryJavaFileObject extends SimpleJavaFileObject {
    private final String sourceCode;

    protected InMemoryJavaFileObject(String className, String sourceCode) {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.sourceCode = sourceCode;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return sourceCode;
    }
}
