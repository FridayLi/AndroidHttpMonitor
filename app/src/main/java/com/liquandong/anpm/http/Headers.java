package com.liquandong.anpm.http;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by liquandong on 2018/4/20.
 */
public final class Headers {
    private final String[] namesAndValues;

    Headers(Builder builder) {
        this.namesAndValues = builder.namesAndValues.toArray(new String[builder.namesAndValues.size()]);
    }

    private Headers(String[] namesAndValues) {
        this.namesAndValues = namesAndValues;
    }

    public Builder newBuilder() {
        Builder result = new Builder();
        Collections.addAll(result.namesAndValues, namesAndValues);
        return result;
    }



    /** Returns the last value corresponding to the specified field, or null. */
    @Nullable
    public String get(String name) {
        return get(namesAndValues, name);
    }

    private static String get(String[] namesAndValues, String name) {
        for (int i = namesAndValues.length - 2; i >= 0; i -= 2) {
            if (name.equalsIgnoreCase(namesAndValues[i])) {
                return namesAndValues[i + 1];
            }
        }
        return null;
    }

    /**
     * Returns the last value corresponding to the specified field parsed as an HTTP date, or null if
     * either the field is absent or cannot be parsed as a date.
     */
    @Nullable
    public Date getDate(String name) {
        String value = get(name);
        return value != null ? HttpDate.parse(value) : null;
    }

    /** Returns the number of field values. */
    public int size() {
        return namesAndValues.length / 2;
    }

    /** Returns the field at {@code position}. */
    public String name(int index) {
        return namesAndValues[index * 2];
    }

    /** Returns the value at {@code index}. */
    public String value(int index) {
        return namesAndValues[index * 2 + 1];
    }

    /** Returns an immutable case-insensitive set of header names. */
    public Set<String> names() {
        TreeSet<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0, size = size(); i < size; i++) {
            result.add(name(i));
        }
        return Collections.unmodifiableSet(result);
    }

    /** Returns an immutable list of the header values for {@code name}. */
    public List<String> values(String name) {
        List<String> result = null;
        for (int i = 0, size = size(); i < size; i++) {
            if (name.equalsIgnoreCase(name(i))) {
                if (result == null) result = new ArrayList<>(2);
                result.add(value(i));
            }
        }
        return result != null
                ? Collections.unmodifiableList(result)
                : Collections.<String>emptyList();
    }

    public static final class Builder {
        final List<String> namesAndValues = new ArrayList<>(20);

        public Headers build() {
            return new Headers(this);
        }

        public Builder removeAll(String name) {
            for (int i = 0; i < namesAndValues.size(); i += 2) {
                if (name.equalsIgnoreCase(namesAndValues.get(i))) {
                    namesAndValues.remove(i); // name
                    namesAndValues.remove(i); // value
                    i -= 2;
                }
            }
            return this;
        }

        public Builder add(String line) {
            int index = line.indexOf(":");
            if (index == -1) {
                throw new IllegalArgumentException("Unexpected header: " + line);
            }
            return add(line.substring(0, index).trim(), line.substring(index + 1));
        }

        public Builder add(String name, String value) {
            checkNameAndValue(name, value);
            return addLenient(name, value);
        }

        Builder addLenient(String name, String value) {
            namesAndValues.add(name);
            namesAndValues.add(value.trim());
            return this;
        }

        private void checkNameAndValue(String name, String value) {
            if (name == null) throw new NullPointerException("name == null");
            if (name.isEmpty()) throw new IllegalArgumentException("name is empty");
            for (int i = 0, length = name.length(); i < length; i++) {
                char c = name.charAt(i);
                if (c <= '\u0020' || c >= '\u007f') {
                    throw new IllegalArgumentException(String.format(
                            "Unexpected char %#04x at %d in header name: %s", (int) c, i, name));
                }
            }
            if (value == null) throw new NullPointerException("value for name " + name + " == null");
            for (int i = 0, length = value.length(); i < length; i++) {
                char c = value.charAt(i);
                if ((c <= '\u001f' && c != '\t') || c >= '\u007f') {
                    throw new IllegalArgumentException(String.format(
                            "Unexpected char %#04x at %d in %s value: %s", (int) c, i, name, value));
                }
            }
        }
    }
}
